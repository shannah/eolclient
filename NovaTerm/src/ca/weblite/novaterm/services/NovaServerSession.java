/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.services;

import ca.weblite.novaterm.events.NavigationEvent;
import com.codename1.html.HTMLParser;
import ca.weblite.novaterm.schemas.WelcomeWindow;
import ca.weblite.novaterm.views.WelcomeWindowView;
import com.codename1.components.WebBrowser;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.File;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.MultipartRequest;
import com.codename1.io.NetworkManager;
import com.codename1.io.Util;
import com.codename1.io.websocket.WebSocket;
import com.codename1.processing.Result;
import com.codename1.rad.controllers.ActionSupport;
import com.codename1.rad.controllers.ControllerEvent;
import com.codename1.rad.models.BooleanProperty;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityType;
import com.codename1.rad.models.IntProperty;
import com.codename1.rad.models.StringProperty;
import com.codename1.ui.CN;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.events.ActionEvent;

import com.codename1.ui.util.EventDispatcher;
import com.codename1.util.AsyncResource;
import com.codename1.util.StringUtil;
import com.codename1.util.regex.RE;
import com.codename1.xml.Element;
import com.codename1.xml.XMLWriter;
import java.io.IOException;
import java.util.LinkedList;
import javabc.BigInteger;
import org.bouncycastle.crypto.digests.MD5Digest;

/**
 *
 * @author shannah
 */
public class NovaServerSession extends Entity {
    public static BooleanProperty connected;
    public static StringProperty nonce, realm, username, password;
    public static IntProperty errorCode;
    private EventDispatcher errorListeners = new EventDispatcher();
    
    
    public class SessionEvent extends ControllerEvent {
        public NovaServerSession getSession() {
            return NovaServerSession.this;
        }
        
        public SessionEvent(Object source) {
            super(source);
        }
    }
    
    public class WelcomeViewEvent extends SessionEvent {
        private WelcomeWindowView.WelcomeWindowModel welcomeViewModel;
        private CommandEvent sourceCommand;
        public WelcomeViewEvent(Object source, WelcomeWindowView.WelcomeWindowModel model, CommandEvent sourceCommand) {
            super(source);
            this.welcomeViewModel = model;
            this.sourceCommand = sourceCommand;
        }
        
        public CommandEvent getSourceCommand() {
            return sourceCommand;
        }
        
        public WelcomeWindowView.WelcomeWindowModel getWelcomeViewModel() {
            return welcomeViewModel;
        }
    }

    
    public class CommandEvent extends SessionEvent {
        private String commandName;
        private String rawCommand;
        private String commandValue;
        
        public CommandEvent(Object source, String rawCommand) {
            super(source);
            this.rawCommand = rawCommand;
        }
        
        public String getCommandName() {
            if (commandName == null) {
                if (rawCommand.indexOf(" ") > 0) {
                    commandName = rawCommand.substring(0, rawCommand.indexOf(" "));
                } else {
                    commandName = "";
                }
            }
            return commandName;
        }
        
        public String getRawCommand() {
            return rawCommand;
        }
        
        public String getCommandValue() {
            if (commandValue == null) {
                if (rawCommand.indexOf(" ") > 0) {
                    commandValue = rawCommand.substring(rawCommand.indexOf(" ")+1);
                } else {
                    commandValue = "";
                }
            }
            return commandValue;
        }
        
        
    }
    
    public class SessionException extends IOException {
        public SessionException(String message) {
            super(message);
        }
    }
    
    public class ExceptionEvent extends ActionEvent {
        private Throwable exception;
        public ExceptionEvent(Throwable exception) {
            super(NovaServerSession.this);
            this.exception = exception;
        }
        
        public Throwable getException() {
            return exception;
        }
    }

    public static final EntityType TYPE = new EntityType(){{
        connected = Boolean();
        realm = string();
        nonce = string();
        username = string();
        password = string();
        errorCode = Integer();
        
    }};
    
    {
        setEntityType(TYPE);
    }
    
    private WebSocket socket;
    private String ircURL = "ws://10.0.1.77:8080/nova";
    private String httpURL = "http://10.0.1.77:8080/proxy";
    private LinkedList<Message> messageQueue = new LinkedList<>();
    
    
    
    
    public class Message extends AsyncResource<String>{
        private boolean consumed;
        private String body;
        public Message(String body) {
            this.body = body;
        }
      
        
        public void consume() {
            consumed = true;
        }
        
        public boolean isConsumed() {
            return consumed;
        }
    }
    
    public Message sendMessage(String message, boolean waitForResponse) {
        
        Message msg = new Message(message);
        if (waitForResponse) {
            synchronized(messageQueue) {
                messageQueue.add(msg);
            }
        } else {
            msg.complete(null);
        }
        socket.send(message+"\r\n");
        return msg;
    }
    
    
    public void connect(String userName, String passwd) {
        setText(username, userName);
        setText(password, passwd);
        socket = new WebSocket(ircURL) {
            @Override
            protected void onOpen() {
                System.out.println("Websocket open.  Sending NONC");
                sendMessage("NONC :user=\""+userName+"\"", true);
            }
            
            @Override
            protected void onClose(int arg0, String arg1) {
                System.out.println("Socket closed");
                setBoolean(connected, false);
            }
            
            @Override
            protected void onMessage(String body) {
                Message msg = null;
                synchronized(messageQueue) {
                    if (!messageQueue.isEmpty()) {
                        msg = messageQueue.removeFirst();
                    }
                }
                if (msg != null) {
                    final Message fMsg = msg;
                    msg.complete(body);
                    msg.ready(m->{
                        if (!fMsg.isConsumed()) {
                            handleMessage(fMsg);
                        }
                    });
                } else {
                    Message m = new Message(null);
                    m.complete(body);
                    handleMessage(m);
                }
                
                
            }
            
            
            
            @Override
            protected void onMessage(byte[] arg0) {
                
            }
            
            @Override
            protected void onError(Exception arg0) {
                Log.e(arg0);
            }
        };
        socket.connect();
    }
    
    
    public void disconnect() {
        socket.close();
    }
    
    private void handleMessage(Message msg) {
        String content = msg.get();
        System.out.println("Received "+content);
        Form form = CN.getCurrentForm();
        Object source = form != null ? form : this;
        CommandEvent evt = new CommandEvent(source, content);
        ActionSupport.dispatchEvent(evt);
        if (evt.isConsumed()) {
            return;
        }
        if (content.startsWith("300 :digest ")) {
            // After the client sends its NONC command,
            // the server will respond with this 300 :digest response,
            // which should be a request for the user to send a digest auth
            // This should include a nonce and realm to use.
            RE nonceRegex = new RE("nonce=\"([^\"]+)\"");
            RE realmRegex = new RE("realm=\"([^\"]+)\"");
            if (nonceRegex.match(content) && realmRegex.match(content)) {
                setText(nonce, nonceRegex.getParen(1));
                setText(realm, realmRegex.getParen(1));
                String auth = createDigestAuthHeader("/", getText(username), getText(password), "irc");
                System.out.println("Auth header is "+auth);
                sendMessage(auth, true);
            } else {
                errorListeners.fireActionEvent(new ExceptionEvent(new SessionException("digest request malformed")));
            }
            
            return;
            
        }
        if (content.trim().equals("300")) {
            // After the client sends its AUTH command with its digest
            // a 300 response will indicate that the login was successful
            System.out.println("You are now logged In!!!");
            setBoolean(connected, true);
            
            sendMessage("JOIN ServerMessages", false);
            return;
        }
        
        if (content.trim().equals("464")) {
            // 464 code means that the user is already logged in with another client
            setInt(errorCode, 464);
            return;
        }
        
        if (content.trim().startsWith("REQUEST ")) {
            String url = content.substring(content.indexOf(" ")+1).trim();
            if (url.endsWith("/welcome.html")) {
                httpGetString(url).onResult((res, err)->{
                    if (err != null) {
                        Log.e(err);
                        return;
                    }
                    if (res.length() == 0) {
                        throw new RuntimeException("Empty HTTP response");
                    }
                    if (res.trim().substring(0,1).equals("<")) {
                        // This is HTML
                        HTMLParser parser = new HTMLParser();
                        Element root = parser.parse(res).get();
                        Result r = Result.fromContent(root);

                        Element template = (Element)r.get("//template");
                        String templateName = template.getAttribute("name");
                        System.out.println("Template name is "+templateName);

                        WelcomeWindowView.WelcomeWindowModel model = new WelcomeWindowView.WelcomeWindowModel();
                        Element body = (Element)r.get("//part[@name='body']");
                        Element img = (Element)r.get("//img");
                        img.setAttribute("src", httpGetAsDataUri("http://*/file/TheTeam", "image/gif").get());
                        XMLWriter xwriter = new XMLWriter(true);
                        String bodyStr = xwriter.toXML(body);
                        //bodyStr = bodyStr.substring(6, bodyStr.length() - 7);
                        System.out.println("bodystr="+bodyStr);
                        model.setText(WelcomeWindow.body, bodyStr);
                        ActionSupport.dispatchEvent(new WelcomeViewEvent(source, model, evt));
                    }
                });
            } else {
                NavigationEvent nevt = new NavigationEvent(source, url);
              
                ActionSupport.dispatchEvent(nevt);
            }
            
        }
    }
    
    private static String md5(String input)  {
        
        String data = input;
        MD5Digest sha1 = new MD5Digest();
        try {
            byte[] b = data.getBytes("UTF-8");
            sha1.update(b, 0, b.length);
            byte[] hash = new byte[sha1.getDigestSize()];
            sha1.doFinal(hash, 0);
            BigInteger bigInt = new BigInteger(1,hash);
            String hashtext = bigInt.toString(16);
            while(hashtext.length() < 32 ){
              hashtext = "0"+hashtext;   
            }
            return hashtext;


        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }

    }
    
    private String createDigestAuthHeader(String uri, String username, String password, String method) {
        uri = StringUtil.replaceAll(uri, " ", "%20");
        String ha1 = md5(username + ":" + getText(realm) + ":" + password);
        String ha2 = md5(method + ":" + uri);
        String response = md5(ha1+":"+getText(nonce)+":"+ha2);
        return "AUTH :digest username=\""+username+"\",realm=\""+getText(realm)+"\",nonce=\""+getText(nonce)+"\",uri=\""+uri+"\",response=\""+response+"\"";
    }
    private String createHttpDigestAuthHeader(String uri, String username, String password, String method) {
        return createHttpDigestAuthHeader(uri, username, password, method, true);
    }
    private String createHttpDigestAuthHeader(String uri, String username, String password, String method, boolean encodeUri) {
        if (encodeUri ) uri = StringUtil.replaceAll(uri, " ", "%20");
        String ha1 = md5(username + ":" + getText(realm) + ":" + password);
        String ha2 = md5(method + ":" + uri);
        String response = md5(ha1+":"+getText(nonce)+":"+ha2);
        return "digest username=\""+username+"\",realm=\""+getText(realm)+"\",nonce=\""+getText(nonce)+"\",uri=\""+uri+"\",response=\""+response+"\"";
    }
    
    
    public class HTTPStringResponse extends AsyncResource<String> {
        
    }
    
    
    public class HTTPImageResponse extends AsyncResource<Image> {
        
    }
    
    
    
    public HTTPImageResponse httpGetImage(String url, File destFile) {
        url = StringUtil.replaceAll(url, " ", "%20");
        HTTPImageResponse out = new HTTPImageResponse();
        final ConnectionRequest req = new ConnectionRequest();
        if (destFile != null) {
            req.setDestinationFile(destFile.getAbsolutePath());
        }
        req.setHttpMethod("GET");
        req.setPost(false);
        req.setUrl(this.httpURL + "?uri="+Util.encodeUrl(url));
        req.addRequestHeader("Authorization", createHttpDigestAuthHeader(url, getText(username), getText(password), "GET"));
        req.addResponseCodeListener(evt->{
            out.error(new IOException("Request failed: "+req.getResponseCode()));
        });
        req.addResponseListener(evt->{
            try {
                if (destFile == null) {
                    byte[] responseData = req.getResponseData();
                    out.complete(Image.createImage(req.getResponseData(), 0, responseData.length));
                } else {
                    FileSystemStorage fs = FileSystemStorage.getInstance();
                    out.complete(EncodedImage.create(fs.openInputStream(destFile.getAbsolutePath()), (int)fs.getLength(destFile.getAbsolutePath())));
                }
            } catch (Exception ex) {
                
                Log.e(ex);
                out.error(new AsyncResource.AsyncExecutionException(ex));
            }
        });
        NetworkManager.getInstance().addToQueue(req);
        return out;
    }
    
    /**
     * Creates a URL with the authorization header embedded in the URL GET parameters so that it can be opened in a browser.
     * @param url The URL
     * @param forDownload IF true, this will add the Content-Type request parameter which will direct the proxy to just return
     * the attachment portion of the multipart response.
     * @return 
     */
    public String createAuthenticatedURL(String url, boolean forDownload) {
        url = StringUtil.replaceAll(url, " ", "%20");
        String out = this.httpURL + "?uri="+Util.encodeUrl(url)+"&Authorization="+Util.encodeUrl(createHttpDigestAuthHeader(url, getText(username), getText(password), "GET"));
        if (forDownload) {
            out += "&Content-Type="+Util.encodeUrl("application/octet-stream");
        }
        return out;
        
    }
    
    
    public class HTTPFileUploadResponse extends AsyncResource<Boolean> {
    
    
    }
    
    public HTTPFileUploadResponse httpFileUpload(String url, String name, String subject, String description, String filePath) {
        //url = StringUtil.replaceAll(url, " ", "%20");
        if (!url.startsWith("http:")) {
            url = "http://*"+url;
        }
        
        String fileName = new File(filePath).getName();
        //fileName = "cindy-richard.jpg";
        HTTPFileUploadResponse out = new HTTPFileUploadResponse();
        MultipartRequest req = new MultipartRequest();
        req.setHttpMethod("POST");
        req.setPost(true);
        //req.setBase64Binaries(false);
        req.setUrl(this.httpURL + "?uri="+Util.encodeUrl(url));
        req.addRequestHeader("Authorization", createHttpDigestAuthHeader(url, getText(username), getText(password), "POST", false));
        req.addRequestHeader("X-NT-Name", name);
        req.addRequestHeader("X-NT-Subject", subject);
        req.addRequestHeader("X-NT-Attached", fileName);
        req.addRequestHeader("X-NS-FileSize", ""+new File(filePath).length());
        req.addRequestHeader("X-NS-LastModified", ""+new File(filePath).lastModified());
        try {
            req.addRequestHeader("X-NS-Mimetype", Util.guessMimeType(filePath));
            
        } catch (Throwable e) {
            Log.e(e);
        }
        req.addArgument("Description", description);
        try {
            
            req.addData("File", filePath, Util.guessMimeType(filePath));
            req.setFilename("File", fileName);
        } catch (IOException ex) {
            out.error(ex);
            return out;
        }
        req.addResponseCodeListener(evt->{
            out.error(new IOException("Request failed: "+req.getResponseCode()));
        });
        req.addResponseListener(evt->{
            out.complete(req.getResponseCode() == 200);
        });
        NetworkManager.getInstance().addToQueue(req);
        return out;
    }
    
    public HTTPStringResponse httpGetString(String url) {
        url = StringUtil.replaceAll(url, " ", "%20");
        HTTPStringResponse out = new HTTPStringResponse();
        final ConnectionRequest req = new ConnectionRequest();
        req.setDuplicateSupported(true);
        req.setHttpMethod("GET");
        req.setPost(false);
        req.setUrl(this.httpURL + "?uri="+Util.encodeUrl(url));
        req.addRequestHeader("Authorization", createHttpDigestAuthHeader(url, getText(username), getText(password), "GET"));
        req.addResponseCodeListener(evt->{
            out.error(new IOException("Request failed: "+req.getResponseCode()));
        });
        
        req.addResponseListener(evt->{
            try {

                String str = new String(req.getResponseData(), "UTF-8");
                str = StringUtil.replaceAll(str, "\r\n", "\r");
                str = StringUtil.replaceAll(str, "\n", "\r");
                str = StringUtil.replaceAll(str, "\r", "\r\n");
                out.complete(str);
                
                
                
                
            } catch (Exception ex) {
                
                Log.e(ex);
                out.error(new AsyncResource.AsyncExecutionException(ex));
            }
        });
        NetworkManager.getInstance().addToQueue(req);
        return out;
    }
    
    
    public HTTPStringResponse httpGetAsDataUri(String url, String dataUriType ) {
        url = StringUtil.replaceAll(url, " ", "%20");
        HTTPStringResponse out = new HTTPStringResponse();
        final ConnectionRequest req = new ConnectionRequest();
        req.setHttpMethod("GET");
        req.setPost(false);
        req.setUrl(this.httpURL + "?uri="+Util.encodeUrl(url));
        req.addRequestHeader("Authorization", createHttpDigestAuthHeader(url, getText(username), getText(password), "GET"));
        req.addResponseCodeListener(evt->{
            out.error(new IOException("Request failed: "+req.getResponseCode()));
        });
        req.addResponseListener(evt->{
            try {
                String str = WebBrowser.createDataURI(req.getResponseData(), dataUriType);
                
                out.complete(str);
            } catch (Exception ex) {
                
                Log.e(ex);
                out.error(new AsyncResource.AsyncExecutionException(ex));
            }
        });
        NetworkManager.getInstance().addToQueue(req);
        return out;
    }
    
}
