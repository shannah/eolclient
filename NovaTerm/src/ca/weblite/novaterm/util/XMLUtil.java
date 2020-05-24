/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.util;

import com.codename1.xml.Element;
import java.util.List;

/**
 *
 * @author shannah
 */
public class XMLUtil {
    public static String getText(Element el) {
        return getText(el, new StringBuilder());
    }
    public static String getText(Element el, StringBuilder sb) {
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
