/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm;

import com.codename1.html.HTMLParser;
import ca.weblite.novaterm.services.NovaServerSession;
import static ca.weblite.novaterm.services.NovaServerSession.connected;
import static ca.weblite.novaterm.services.NovaServerSession.password;
import static ca.weblite.novaterm.services.NovaServerSession.username;
import com.codename1.processing.Result;
import com.codename1.testing.AbstractTest;
import com.codename1.xml.Element;
import com.codename1.xml.XMLWriter;

/**
 *
 * @author shannah
 */
public class NTSessionTest extends AbstractTest  {

    @Override
    public boolean shouldExecuteOnEDT() {
        return true;
    }

    
    
    @Override
    public boolean runTest() throws Exception {
        NovaServerSession session = new NovaServerSession();
        try {
            session.connect("SYSOP", "flutie");
            super.waitFor(5000);
            assertTrue(session.getBoolean(connected));
            
            String mainMenu = session.httpGetString("http://*/Design/17").get();
            HTMLParser p = new HTMLParser();
            Element root = p.parse(mainMenu).get();

            Result result = Result.fromContent(root);
            Element template = (Element) result.get("//template");
            assertNotNull(template);
            assertEqual("NavigateWindow", template.getAttribute("name"));
            XMLWriter writer = new XMLWriter(true);
            System.out.println(writer.toXML(root));
            
            return true;
        } finally {
            session.disconnect();
        }
        
        
    }
    
}
