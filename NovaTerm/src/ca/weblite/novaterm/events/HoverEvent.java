/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.events;

import com.codename1.rad.controllers.ControllerEvent;

/**
 *
 * @author shannah
 */
public class HoverEvent extends ControllerEvent {
    private String textMessage;
    
    public HoverEvent(Object source, String textMessage) {
        super(source);
        this.textMessage = textMessage;
    }
    
    public String getTextMessage() {
        return textMessage;
    }
}
