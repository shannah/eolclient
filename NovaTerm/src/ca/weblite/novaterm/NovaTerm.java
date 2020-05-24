package ca.weblite.novaterm;


import ca.weblite.novaterm.controllers.LoginFormController;
import ca.weblite.novaterm.controllers.WelcomeWindowController;
import ca.weblite.novaterm.services.NovaServerSession;
import ca.weblite.novaterm.services.NovaServerSession.WelcomeViewEvent;
import com.codename1.components.ToastBar;
import com.codename1.rad.controllers.ApplicationController;
import com.codename1.rad.controllers.ControllerEvent;
import com.codename1.ui.CN;

/**
 * This file was generated by <a href="https://www.codenameone.com/">Codename One</a> for the purpose 
 * of building native mobile applications using Java.
 */
public class NovaTerm extends ApplicationController {
    private NovaServerSession session;
    
    @Override
    public void init(Object context) {
        super.init(context);
        session = new NovaServerSession();
        session.addPropertyChangeListener(NovaServerSession.connected, evt->{
            if (session.getBoolean(NovaServerSession.connected)) {
                
            } else {
                ToastBar.showInfoMessage("You have been disconnected");
                new LoginFormController(this).showBack();
            }
        });
        this.addLookup(session);
        CN.setProperty("openGallery.openFilesInPlace", "true");
        
    }
    
    

    @Override
    public void start() {
        super.start();
        new LoginFormController(this).show();
    }

    @Override
    public void actionPerformed(ControllerEvent evt) {
        if (evt.getClass() == NovaServerSession.WelcomeViewEvent.class) {
            evt.consume();
            WelcomeViewEvent welcomeEvent = (WelcomeViewEvent)evt;
            new WelcomeWindowController(this, welcomeEvent.getWelcomeViewModel()).show();
        }
        super.actionPerformed(evt);
    }
    
    
    
}