/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaserver;

import ca.weblite.novaterm.SocketWrapper;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author shannah
 */
@ServerEndpoint("/nova")
public class NovaserverEndpoint {

    private String novaserverHost = "10.0.1.117";
    private int novaserverPort = 194;
    
    private static Map<Session,SocketWrapper> sessions = Collections.synchronizedMap(new HashMap<Session,SocketWrapper>());
   @OnMessage
    public String onMessage(Session session, String message) {
        System.out.println("Received message "+message);
        SocketWrapper wrap = sessions.get(session);
        if (wrap != null) {
            wrap.onMessage(message);
        } else {
            System.out.println("No registered session found");
        }
        
        return null;
    }
    
    @OnOpen
    public void onOpen(final Session peer) {
        System.out.println("Opened!!");
        SocketWrapper wrap = new SocketWrapper(novaserverHost, novaserverPort, peer, new Runnable() {
            public void run() {
                sessions.remove(peer);
            }
        });
        
        sessions.put(peer, wrap);
        System.out.println("Sessions: "+sessions);
        wrap.bind();
    }
    
    @OnClose
    public void onClose(Session peer) {
        SocketWrapper wrap = sessions.get(peer);
        if (wrap != null) {
            wrap.stop();
        }
        sessions.remove(peer);
        System.out.println("CLosed!!");
        System.out.println("Sessions: "+sessions);
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Received error from session");
        throwable.printStackTrace();
    }
    
}