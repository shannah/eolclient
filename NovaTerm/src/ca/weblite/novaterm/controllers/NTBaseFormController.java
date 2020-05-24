/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;

import ca.weblite.novaterm.events.NavigationEvent;
import ca.weblite.novaterm.models.NovaViewModel;
import ca.weblite.novaterm.services.NovaServerSession;
import ca.weblite.novaterm.views.NovaView;
import com.codename1.html.HTMLParser;
import com.codename1.imagemap.ImageMapContainer;
import com.codename1.processing.Result;
import com.codename1.rad.controllers.Controller;
import com.codename1.rad.controllers.ControllerEvent;
import com.codename1.rad.controllers.FormController;
import com.codename1.ui.CN;
import static com.codename1.ui.CN.CENTER;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Image;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Effects;
import com.codename1.util.StringUtil;
import com.codename1.util.regex.RE;
import com.codename1.xml.Element;

/**
 *
 * @author shannah
 */
public class NTBaseFormController extends FormController {
    private String url;
    private NovaServerSession session;
    
    public NTBaseFormController(Controller parent) {
        this(parent, null);
        
    }
    public NTBaseFormController(Controller parent, String url) {
        super(parent);
        this.url = url;
        session = lookup(NovaServerSession.class);
    }

    @Override
    public void actionPerformed(ControllerEvent evt) {
        if (evt.getClass() == NavigationEvent.class) {
            handleNavigationEvent((NavigationEvent)evt);
            if (evt.isConsumed()) {
                return;
            }
        }
        super.actionPerformed(evt);
    }
    
    
    public void handleNavigationEvent(NavigationEvent evt) {
        
        String dest = evt.getDestinationUrl();
        String lcDest = dest.toLowerCase();
        if (lcDest.startsWith("/design/") || lcDest.startsWith("http://*/design/")) {
            evt.consume();
            String designId = dest.substring(dest.lastIndexOf("/")+1).trim();
            DesignController ctl = new DesignController(this, designId);
            ctl.show();
            return;
        }
        if (lcDest.startsWith("/file/") || lcDest.startsWith("http://*/file/")) {
            evt.consume();
            FileFormController ctl = new FileFormController(this, dest);
            ctl.show();
            return;
        }
        if (lcDest.startsWith("/library/") || lcDest.startsWith("http://*/library/")) {
            evt.consume();
            LibraryFormController ctl = new LibraryFormController(this, dest);
            ctl.show();
            return;
        }
        if (lcDest.startsWith("/news/") || lcDest.startsWith("http://*/news/")) {
            evt.consume();
            new MessageForumController(this, dest).show();
            return;
        }
    }
    
    public String getURL() {
        return url;
    }
    
    protected void load(String rawContent, Element doc) {
        
    }
    
    public void load() {
        if (getURL() == null) {
            return;
        }
        session.httpGetString(getURL()).onResult((res,err)->{
            if (err != null) {
                Dialog.show("Failed to load", "The design failed to load", "OK", null);
                getParent().getFormController().showBack();
                return;
            }
            
            if (res.startsWith("--")) {
                String boundary = res.substring(2, res.indexOf("\r"));
                res = res.substring(boundary.length()+2);
                res = res.trim();
                res = res.substring(res.indexOf("\r\n\r\n"));
                res = res.trim();
                res = res.substring(0, res.indexOf("--"+boundary+"--"));
                res = res.trim();
            }
            res = new RE("<ni>([^<]*)").subst(res, "<ni>$1</ni>", RE.REPLACE_BACKREFERENCES);
            res = new RE("<template ([^>]+)>").subst(res, "<template $1></template>", RE.REPLACE_BACKREFERENCES | RE.REPLACE_FIRSTONLY);
            
            HTMLParser parser = new HTMLParser();
            // Because the HTML parser fixes the image tags replacing them with img and strips out the text content.
            res = StringUtil.replaceAll(res, "<image ", "<novaterm-image ");
            res = StringUtil.replaceAll(res, "</image>", "</novaterm-image>");
            Element doc = parser.parse(res).get(); 
            load(res, doc);
            
        });
    } 
    
    
}
