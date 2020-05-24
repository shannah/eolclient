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
public class NavigationEvent extends ControllerEvent {
    private String destinationUrl;
    private String sourceUrl;
    public NavigationEvent(String destinationUrl) {
        this.destinationUrl = destinationUrl;
        
    }
    
    public NavigationEvent(Object eventSource, String destinationUrl) {
        super(eventSource);
        this.destinationUrl = destinationUrl;
    }
    
    
    public NavigationEvent(String source, String dest) {
        this.sourceUrl = source;
        this.destinationUrl = dest;
    }
    
    public String getSourceUrl() {
        return sourceUrl;
    }
    
    public String getDestinationUrl() {
        return destinationUrl;
    }
    
    public void setSourceUrl(String source) {
        this.sourceUrl = source;
    }
}
