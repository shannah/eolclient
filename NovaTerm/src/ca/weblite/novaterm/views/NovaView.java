/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.events.HoverEvent;
import ca.weblite.novaterm.events.NavigationEvent;
import ca.weblite.novaterm.models.NovaViewModel;
import ca.weblite.novaterm.models.NovaViewModel.NovaViewLink;
import com.codename1.rad.controllers.ActionSupport;
import com.codename1.ui.Button;
import com.codename1.ui.CN;
import com.codename1.ui.Component;
import static com.codename1.ui.ComponentSelector.$;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.Layout;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shannah
 */
public class NovaView extends Container {
    private NovaViewModel model;
    private static Map<String,Character> icons = new HashMap<>();
    static {
        icons.put("NV Library Icon", FontImage.MATERIAL_LIBRARY_BOOKS);
        icons.put("NV Newscan", FontImage.MATERIAL_NEW_RELEASES);
        icons.put("NV SelectNewscan", FontImage.MATERIAL_SELECT_ALL);
    }
    
    private static Layout createLayout() {
        if (CN.isTablet()) {
            FlowLayout l = new FlowLayout();
            l.setValign(TOP);
            l.setAlign(LEFT);
            return l;
        } else {
            return BoxLayout.y();
        }
    }
    
    private static String iconKey(String src) {
        if (src.indexOf("/") > 0) {
            return src.substring(src.lastIndexOf("/")+1);
            
            
        }
        return src;
    }
    
    private static char _getMaterialIcon(String src) {
        System.out.println("Material icon for "+src);
        Character out = (Character)icons.get(iconKey(src));
        if (out != null) {
            return out;
        }
        return FontImage.MATERIAL_LIBRARY_BOOKS;
    }
    
    private static float _getIconSize() {
        if (CN.isTablet()) {
            return 15f;
        } else {
            return -1;
        }
    }
    
    public NovaView(NovaViewModel model) {
        super(createLayout());
        this.model = model;
        for (NovaViewLink link : model) {
            add(new LinkView(link));
        }
        
    }
         
    
    private class LinkView extends Button {
        NovaViewLink model;
        LinkView(NovaViewLink model) {
            super(model.getLabel());
            this.model = model;
            setUIID("NovaViewLink");
            setCursor(Component.HAND_CURSOR);
            float iconSize = _getIconSize();
            if (iconSize > 0) {
                setMaterialIcon(_getMaterialIcon(model.getImageSrc()), iconSize);
            } else {
                setMaterialIcon(_getMaterialIcon(model.getImageSrc()));
            }
            if (CN.isTablet()) {
                setTextPosition(BOTTOM);
                getAllStyles().setAlignment(CENTER);
                $(this).setMarginMillimeters(5f);
            }
            addActionListener(evt->{
                ActionSupport.dispatchEvent(new NavigationEvent(this, this.model.getHref()));
            });
            
        }

        @Override
        public void pointerHover(int[] x, int[] y) {
            super.pointerHover(x, y);
            ActionSupport.dispatchEvent(new HoverEvent(this, model.getHref()));
        }
        
        

        @Override
        protected void initComponent() {
            super.initComponent();
            getComponentForm().setEnableCursors(true);
        }
        
        
    }
           
}
