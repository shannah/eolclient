/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;

import ca.weblite.novaterm.events.NavigationEvent;
import com.codename1.html.HTMLParser;
import ca.weblite.novaterm.services.NovaServerSession;
import com.codename1.processing.Result;
import com.codename1.rad.controllers.Controller;
import com.codename1.ui.BrowserComponent;
import static com.codename1.ui.BrowserComponent.onLoad;
import static com.codename1.ui.CN.CENTER;
import static com.codename1.ui.CN.callSerially;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.events.BrowserNavigationCallback;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.util.regex.RE;
import com.codename1.xml.Element;
import com.codename1.xml.XMLWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shannah
 */
public class FileFormController extends NTBaseFormController {
    private BrowserComponent webview;
    private NovaServerSession session;
    private Container cnt;
    public FileFormController(Controller parent, String url) {
        super(parent);
        session = lookup(NovaServerSession.class);
        cnt = new Container(new BorderLayout());
        webview = new BrowserComponent();
        webview.addBrowserNavigationCallback(destUrl -> {
            callSerially(()->{
                NavigationEvent evt = new NavigationEvent(destUrl);
                dispatchEvent(evt);
            });
            return false;
        });
        cnt.add(CENTER, webview);
        setView(cnt);
        load(url);
        
        
    }
    
    private void load(String url) {
        session.httpGetString(url).onResult((res, err)->{
            if (err != null) {
                Dialog.show("Error", "Failed to load "+url, "Go Back", null);
                getParent().getFormController().showBack();
                return;
            }
            
            HTMLParser parser = new HTMLParser();
            RE re = new RE("<template [^>]+>");
            res = re.subst(res, "");
            Element root = parser.parse(res).get();
            Result r = Result.fromContent(root);
            // Update images
            List<Element> images = r.getAsArray("//img");
            int index = 0;
            List<String> toLoad = new ArrayList<>();
            if (images != null) {
                for (Element img : images) {
                    String src = img.getAttribute("src");
                    if (src.startsWith("http://*/") || (!src.startsWith("http://") && !src.startsWith("data:") && !src.startsWith("https"))) {
                        img.setAttribute("id", "nt-image-"+index);
                        toLoad.add(src);
                        img.setAttribute("src", "");
                        index++;
                    }
                }
            }
            XMLWriter writer = new XMLWriter(true);
            String pageContent = writer.toXML(root);
            webview.setPage(pageContent, url);
            index = 0;
            for (String imgUrl : toLoad) {
                if (!imgUrl.startsWith("/") && !imgUrl.startsWith("http:")) {
                    imgUrl = "http://*/file/"+imgUrl;
                }
                int fIndex = index;
                String dataType  = "image/gif";
                if (imgUrl.toLowerCase().endsWith(".jpg")) {
                    dataType = "image/jpeg";
                }
                if (imgUrl.toLowerCase().endsWith(".png")) {
                    dataType = "image/png";
                }
                session.httpGetAsDataUri(imgUrl, dataType).ready(dataUrl->{
                    webview.ready(bc->{
                        webview.execute("document.getElementById('nt-image-"+fIndex+"').src=${0};", new Object[]{dataUrl});
                    });
                    
                });
                index++;
                        
            }
            
            
        });
    }
}
