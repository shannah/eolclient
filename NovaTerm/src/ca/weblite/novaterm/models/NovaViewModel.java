/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.models;

import ca.weblite.novaterm.controllers.DesignController;
import ca.weblite.novaterm.models.NovaViewModel.NovaViewLink;
import com.codename1.io.Util;
import com.codename1.processing.Result;
import com.codename1.ui.geom.Dimension;
import com.codename1.xml.Element;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author shannah
 */
public class NovaViewModel implements Iterable<NovaViewLink> {
    private Dimension size;
    private List<NovaViewLink> links;
    private String name;
    
    
    

    public NovaViewModel(Element root) {
        Result r = Result.fromContent(root);
        Element templateNode = (Element)r.get("//template");
        name = templateNode.getAttribute("name");
        String[] sizeStr = Util.split(templateNode.getAttribute("size"), ",");
        size = new Dimension(Integer.parseInt(sizeStr[0].trim()), Integer.parseInt(sizeStr[1].trim()));
        links = new ArrayList<>();
        for (Element a : (List<Element>)r.getAsArray("//a")) {
            links.add(new NovaViewLink(a));
        }
        
        

    }

    @Override
    public Iterator<NovaViewLink> iterator() {
        return links.iterator();
    }
    
    
    
    
    public class NovaViewLink {

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @param label the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }

        /**
         * @return the href
         */
        public String getHref() {
            return href;
        }

        /**
         * @return the effect
         */
        public String getEffect() {
            return effect;
        }

        /**
         * @return the imageSrc
         */
        public String getImageSrc() {
            return imageSrc;
        }

        /**
         * @param imageSrc the imageSrc to set
         */
        public void setImageSrc(String imageSrc) {
            this.imageSrc = imageSrc;
        }
        private String href;
        private String effect;
        private String imageSrc;
        private String label;
        
        NovaViewLink(Element a) {
            href = a.getAttribute("href");
            effect = a.getAttribute("effect");
            for (Element image : (List<Element>)a.getChildrenByTagName("novaterm-image")) {
                imageSrc = image.getAttribute("src");
                label = getText(image, new StringBuilder());
            }
            
        }
        
        
        
        
        
    }
    private static String getText(Element el, StringBuilder sb) {
        if (el.isTextElement()) {
            sb.append(el.getText());
        } else {
            if (el.hasTextChild()) {
                for (Element child : (List<Element>)el.getTextChildren(null, false)) {
                    sb.append(" ");
                    getText(child, sb);
                }
            }
            
        }
        return sb.toString().trim();
    }
}
