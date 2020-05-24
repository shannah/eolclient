/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.html;

import com.codename1.html.HTMLParser;
import com.codename1.processing.Result;
import com.codename1.testing.AbstractTest;
import com.codename1.xml.Element;
import com.codename1.xml.XMLWriter;

/**
 *
 * @author shannah
 */
public class HTMLParserTest extends AbstractTest {

    @Override
    public boolean shouldExecuteOnEDT() {
        return true;
    }

    
    
    @Override
    public boolean runTest() throws Exception {
        String content = "<html><template name=\"NavigateWindow\" size=\"467;350\"><title>Main Menu</title>\n" +
"<part name=\"Navigate Edit part\">\n" +
"<fig src=\"/file/MainMenu\" version=\"Sat, 22 Jul 1995 10:49:31 -0700\">\n" +
"href=\"/design/837\" effect=replace></a>\n" +
"href=\"/design/1447\" effect=replace></a>\n" +
"href=\"quit://\" effect=replace></a>\n" +
"href=\"/design/6727\" effect=replace></a>\n" +
"href=\"/design/783\" effect=replace></a>\n" +
"href=\"/design/729\" effect=replace></a>\n" +
"href=\"/news/Product Info/\" effect=new></a></fig></part></html>";
        
        HTMLParser p = new HTMLParser();
        Element root = p.parse(content).get();
        
        Result result = Result.fromContent(root);
        Element template = (Element) result.get("//template");
        assertNotNull(template);
        assertEqual("NavigateWindow", template.getAttribute("name"));
        XMLWriter writer = new XMLWriter(true);
        System.out.println(writer.toXML(root));
        return true;
        
        
    }
    
}
