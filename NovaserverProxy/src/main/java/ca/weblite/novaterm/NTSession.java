/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author shannah
 */
public class NTSession implements Runnable {
    private Socket ircSocket;
    private BufferedReader input;
    private BufferedWriter output;
    private String nonce;
    private final String username;
    private final String password;
    private String realm;
    private final String serverAddress;
    
    private NTSessionHandler handler;
    
    public static interface NTSessionHandler {
        public void handleException(Throwable t);
        public void onMessage(String message);
        public void onConnect();
        public void onDisconnect();
    }
    

    public NTSession(String username, String password, String serverAddress) {
        this.username = username;
        this.password = password;
        this.serverAddress = serverAddress;
    }
    
    public void setHandler(NTSessionHandler handler) {
        this.handler = handler;
    }

   
    private void sendString(String str) throws IOException {
        output.write(str + "\r\n");
        output.flush();
    }
    
    private static String md5(String input)  {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] digest = md.digest();
            BigInteger bigInt = new BigInteger(1,digest);
            String hashtext = bigInt.toString(16);
            while(hashtext.length() < 32 ){
              hashtext = "0"+hashtext;   
            }
            return hashtext;
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Can't find md5 algorithm", ex);
        }
    }
    
    private String createDigestAuthHeader(String uri, String username, String password, String method) {
        String ha1 = md5(username + ":" + realm + ":" + password);
        String ha2 = md5(method + ":" + uri);
        String response = md5(ha1+":"+nonce+":"+ha2);
        return "AUTH :digest username=\""+username+"\",realm=\""+realm+"\",nonce=\""+nonce+"\",uri=\""+uri+"\",response=\""+response+"\"";
    }
    
    
    private void connect(String ip, String username, String password) throws IOException, AlreadyConnectedException {
        ircSocket = new Socket(ip, 194);
        input = new BufferedReader(new InputStreamReader(ircSocket.getInputStream(), "UTF-8"));
        output = new BufferedWriter(new OutputStreamWriter(ircSocket.getOutputStream(), "UTF-8"));
        sendString("NONC :user=\""+username+"\"");
        String response = input.readLine();
        if ("464".equals(response)) {
            throw new AlreadyConnectedException();
        }
        if (response.startsWith("300") && response.contains("realm=") && response.contains("nonce=")) {
            Matcher realmRegex = Pattern.compile("realm=\"(.*?)\"").matcher(response);
            Matcher nonceRegex = Pattern.compile("nonce=\"(.*?)\"").matcher(response);
            realmRegex.find();
            nonceRegex.find();
            realm = realmRegex.group(1);
            nonce = nonceRegex.group(1);
            sendString(createDigestAuthHeader("/", username, password, "irc"));
            
            response = input.readLine();
            if ("300".equals(response)) {
                // Successfully logged in.
                return;
            }
            throw new IOException("Failed to login.  Response code "+response);
            
        }
        
        
        
        
    }

    @Override
    public void run() {
        boolean connected = false;
        try {
            connect(serverAddress, username, password);
            onConnect();
            connected = true;
            while (ircSocket.isConnected() && !ircSocket.isInputShutdown() && !ircSocket.isOutputShutdown()) {
                try {
                    handleMessage(input.readLine());
                } catch (Throwable t) {
                    handleException(t);
                }
            }
            
        } catch (Throwable t) {
            handleException(t);
        } finally {
            try {
                if (ircSocket != null) {
                    ircSocket.close();
                }
            } catch (Throwable t){}
            if (connected) {
                onDisconnect();
            }
        }
    }
        
    private void handleException(Throwable t) {
        if (handler != null) {
            handler.handleException(t);
        }
    }
    
    private void handleMessage(String message) {
        if (handler != null) {
            handler.onMessage(message);
        }
    }
    
    private void onConnect() {
        if (handler != null) {
            handler.onConnect();
        }
    }
    
    private void onDisconnect() {
        if (handler != null) {
            handler.onDisconnect();
        }
    }
    
}
