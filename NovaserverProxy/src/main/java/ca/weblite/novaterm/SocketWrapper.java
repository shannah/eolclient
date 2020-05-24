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
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.websocket.Session;

/**
 *
 * @author shannah
 */
public class SocketWrapper {
    private final String host;
    private final int port;
    private Socket socket;
    private Session session;
    private boolean stop;
    private boolean running;
    private BufferedReader input;
    private BufferedWriter output;
    private Runnable onClose;
    private LinkedList<String> messageQueue = new LinkedList<String>();
    
    public SocketWrapper(String host, int port, Session session, Runnable onClose) {
        this.host = host;
        this.port = port;
        this.session = session;
        this.onClose = onClose;
    }
    
    public synchronized void onMessage(String message) {
        
        if (output == null) {
            
            if (output == null) {
                messageQueue.add(message);
                return;
            }
            
        }

        try {
            System.out.println("Writing messagse to socket "+message);
            output.write(message);
            output.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public synchronized void bind() {
        if (running) {
            return;
        }
        running = true;
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    socket = new Socket(host, port);
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                    System.out.println("Opened input and output streams for socket");
                    final ArrayList<String> pendingMessages = new ArrayList<>();
                    synchronized(SocketWrapper.this) {
                        while (!messageQueue.isEmpty()) {
                            pendingMessages.add(messageQueue.removeFirst());
                        }
                    }
                    if (!pendingMessages.isEmpty()) {
                        for (String message : pendingMessages) {
                            onMessage(message);
                        }
                       
                    }
                    
                    while (!stop && socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
                        System.out.println("Waiting for input");
                        String line = input.readLine();
                        if (line != null) {
                            System.out.println("Line of input received from socket: "+line);
                            session.getBasicRemote().sendText(line);
                            
                        } else {
                            break;
                        }
                        System.out.println("Input received and sent");
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();

                } finally {
                    running = false;
                    try {
                        System.out.println("Closing websocket");
                        session.close();
                    } catch (Throwable t) {
                        
                    }
                    try {
                        if (input != null) {
                            System.out.println("Closing inputstream");
                            input.close();
                        }
                    } catch (Throwable t){}
                    try {
                        if (output != null) {
                            System.out.println("Closing outputstream");
                            output.close();
                        }
                    } catch (Throwable t){}
                    try {
                        if (socket != null) {
                            System.out.println("Closing socket");
                            socket.close();
                        }
                    } catch (Throwable t){}
                    try {
                        System.out.println("Closing websocket again.");
                        session.close();
                    } catch (Throwable t){}
                    if (onClose != null) {
                        System.out.println("Firing onClose callback");
                        onClose.run();
                    }
                }
            }
            
        });

        t.start();
        
    }
    
    public void stop() {
        System.out.println("Stopping wrapper");
        stop = true;
        try {
            if (socket != null) {
                socket.close();
            }
        }  catch (Throwable ex) {}
    }
}
