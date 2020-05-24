/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;


import ca.weblite.novaterm.events.HoverEvent;
import ca.weblite.novaterm.events.NavigationEvent;
import com.codename1.html.HTMLParser;
import ca.weblite.novaterm.services.NovaServerSession;
import com.codename1.imagemap.ImageMapContainer;
import com.codename1.io.File;
import com.codename1.io.FileSystemStorage;
import com.codename1.io.Log;
import com.codename1.io.Util;
import com.codename1.processing.Result;
import com.codename1.rad.controllers.Controller;
import com.codename1.ui.CN;
import static com.codename1.ui.CN.CENTER;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Form;
import com.codename1.ui.Image;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.geom.Rectangle;

import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Effects;
import com.codename1.xml.Element;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import ca.weblite.novaterm.models.*;
import ca.weblite.novaterm.views.NovaView;
import com.codename1.rad.controllers.ControllerEvent;
import com.codename1.ui.Label;
import static com.codename1.ui.layouts.BorderLayout.SOUTH;
import com.codename1.util.StringUtil;

/**
 *
 * @author shannah
 */
public class DesignController extends NTBaseFormController {
    private String designId;
    private NovaServerSession session;
    private Container cnt;
    private Label statusLabel = new Label();
    public DesignController(Controller parent, String designId) {
        super(parent);
        this.designId = designId;
        session = lookup(NovaServerSession.class);
        cnt = new Container(new BorderLayout(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE));
        cnt.add(SOUTH, statusLabel);
        setView(cnt);
        Form f = cnt.getComponentForm();
        if (f != null) {
            f.setEnableCursors(true);
        }
        load();
        
    }
    
    private class NavigateWindowLink {
        private String href;
        private String shape;
        private Rectangle bounds;
        private String effect;
        
        NavigateWindowLink(Element a) {
            href = a.getAttribute("href");
            String shapeStr = a.getAttribute("shape");
            shape = shapeStr.substring(0, shapeStr.indexOf(" "));
            shapeStr = shapeStr.substring(shapeStr.indexOf(" ")+1).trim();
            String[] parts = Util.split(shapeStr, ",");
            bounds = new Rectangle(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim()),
                    Integer.parseInt(parts[3].trim())
            );
            
            effect = a.getAttribute("effect");
        }
    }
    
    
    
    
    private class NavigateWindowTemplate {
        private String name;
        private Dimension size;
        private Image img;
        private List<NavigateWindowLink> links = new ArrayList<>();
        
        NavigateWindowTemplate(Element template, Element fig) {
            name = template.getAttribute("name");
            String sizeStr = template.getAttribute("size");
            String[] parts = Util.split(sizeStr, ";");
            size = new Dimension(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            String src = fig.getAttribute("src");
            if (src.startsWith("/")) {
                src = src.substring(1);
            }
            String version = fig.getAttribute("version");
            File file = new File(src);
            file.getParentFile().mkdirs();
            File versionFile = new File(src+".version");
            String versionString = "";
            FileSystemStorage fs = FileSystemStorage.getInstance();
            if (versionFile.exists()) {
                try {
                    versionString = Util.readToString(fs.openInputStream(versionFile.getAbsolutePath()));
                } catch (IOException ex){
                    Log.e(ex);
                }
            }
            versionString = versionString.trim();
            if (!file.exists() || !versionString.equals(version.trim())) {
                // Version has changed
                img = session.httpGetImage(fig.getAttribute("src"), file).get();
                
                try (OutputStream versionOut = fs.openOutputStream(versionFile.getAbsolutePath())) {
                    versionOut.write(version.getBytes("UTF-8"));
                } catch (IOException ex) {
                    Log.e(ex);
                }
        } else {
                // We can just used the previously downloaded version.
                try {
                    img = EncodedImage.create(fs.openInputStream(file.getAbsolutePath()), (int)fs.getLength(file.getAbsolutePath()));
                    
                } catch (IOException ex) {
                    Log.e(ex);
                }
            }
            
            List<Element> linkTags = fig.getChildrenByTagName("a");
            for (Element a : linkTags) {
                links.add(new NavigateWindowLink(a));
            }
            
        }
        
    }
    
    private ImageMapContainer createImageMap(NavigateWindowTemplate tpl) {
        int w = tpl.size.getWidth();
        int h = tpl.size.getHeight();
        if (!CN.isDesktop()) {
            if (CN.isPortrait()) {
                double availableWidth = cnt.getWidth() - cnt.getStyle().getHorizontalPadding();
                double factor = (availableWidth)/w;
                h = (int)Math.round(factor * h);
                w = (int)Math.round(availableWidth);
            } else {
                double availableHeight = cnt.getHeight() - cnt.getStyle().getVerticalPadding();
                double factor = (availableHeight)/h;
                w = (int)Math.round(factor * w);
                h = (int)Math.round(availableHeight);
            }
        }
        ImageMapContainer out = new ImageMapContainer(tpl.img, w, h);
        for (NavigateWindowLink link : tpl.links) {
            ImageMapContainer.ClickableAreaType type = ImageMapContainer.ClickableAreaType.Rect;
            if ("oval".equals(link.shape)) {
                type = ImageMapContainer.ClickableAreaType.Oval;
            }
            ImageMapContainer.ClickableArea area = out.new ClickableArea(type, link.bounds, evt->{
                NavigationEvent nav = new NavigationEvent(link.href);
                dispatchEvent(nav);
            });
            out.addClickableAreas(area);
            
        }
        return out;
    }
    
    public void load() {
        session.httpGetString(getURL()).onResult((res,err)->{
            if (err != null) {
                Dialog.show("Failed to load", "The design failed to load", "OK", null);
                getParent().getFormController().showBack();
                return;
            }
            
            
            HTMLParser parser = new HTMLParser();
            // Because the HTML parser fixes the image tags replacing them with img and strips out the text content.
            res = StringUtil.replaceAll(res, "<image ", "<novaterm-image ");
            res = StringUtil.replaceAll(res, "</image>", "</novaterm-image>");
            Element doc = parser.parse(res).get(); 
            
            Result r = Result.fromContent(doc);
            String title = r.getAsString("//title");
            System.out.println("Title is "+title);
            if (title != null && cnt.getComponentForm() != null) {
                System.out.println("Setting title to "+title);
                setTitle(title);
                
            }
            Element template = (Element)r.get("//template");
            String templateName = template.getAttribute("name");
            
            if (templateName.equals("NavigateWindow")) {
                Element figure = (Element)r.get("//fig");
                NavigateWindowTemplate tpl = new NavigateWindowTemplate(template, figure);
                
                Image bg = tpl.img.scaledLargerRatio(cnt.getWidth(), cnt.getHeight());
                bg = Effects.gaussianBlurImage(tpl.img, 25f);
                cnt.getAllStyles().setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
                cnt.getAllStyles().setBgImage(bg);
                
                //ImageMapContainer imageMap = new ImageMapContainer(img);
                Container yScroller = new Container(BoxLayout.y());
                yScroller.setScrollableY(true);
                ImageMapContainer imMap = createImageMap(tpl);
                yScroller.add(imMap);
                if (!CN.isTablet()) {
                    ((BorderLayout)cnt.getLayout()).setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_CENTER);
                }
                cnt.add(CENTER, yScroller);
                if (cnt.getComponentForm() != null) {
                    cnt.getComponentForm().revalidateWithAnimationSafety();
                }

            } else if (templateName.equals("NovaViewWindow")) {
                NovaViewModel nvModel = new NovaViewModel(doc);
                ((BorderLayout)cnt.getLayout()).setCenterBehavior(BorderLayout.CENTER_BEHAVIOR_CENTER);
                cnt.add(CENTER, new NovaView(nvModel));
                if (cnt.getComponentForm() != null) {
                    cnt.getComponentForm().revalidateWithAnimationSafety();
                }
                
            }
        });
    }  
    
    public String getURL() {
        return "http://*/Design/"+designId;
    }

    @Override
    public void actionPerformed(ControllerEvent evt) {
        if (evt.getClass() == HoverEvent.class) {
            evt.consume();
            String textMessage = ((HoverEvent)evt).getTextMessage();
            if (!textMessage.equals(statusLabel.getText())) {
                statusLabel.setText(textMessage);
                statusLabel.getComponentForm().revalidateWithAnimationSafety();
            }
            
            
        }
        super.actionPerformed(evt);
    }

   
    
    
    
    
}
