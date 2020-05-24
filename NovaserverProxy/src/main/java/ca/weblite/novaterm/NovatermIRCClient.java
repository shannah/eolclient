/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author shannah
 */
public class NovatermIRCClient implements Runnable {
    private ServerSocket serverSocket;
    private int port = 90194;
    
    
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                Socket sock = serverSocket.accept();
                
                
            }
        } catch (IOException ex) {
            
        }
        
        
    }
    
}
