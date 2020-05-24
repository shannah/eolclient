/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;

import ca.weblite.novaterm.models.FileModel;
import ca.weblite.novaterm.schemas.FileSchema;
import ca.weblite.novaterm.services.NovaServerSession;
import ca.weblite.novaterm.views.FileDetailView;
import com.codename1.io.Util;
import com.codename1.processing.Result;
import com.codename1.rad.controllers.Controller;
import com.codename1.rad.models.Entity;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.UI;
import com.codename1.ui.CN;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.util.regex.RE;
import com.codename1.xml.Element;

/**
 *
 * @author shannah
 */
public class LibraryFileDetailController extends NTBaseFormController implements FileSchema {
    private FileModel fileModel;
    public static final ActionNode download = new ActionNode(UI.icon(FontImage.MATERIAL_FILE_DOWNLOAD));
    public LibraryFileDetailController(Controller parent, FileModel file) {
        super(parent, file.getText(detailsUrl));
        this.fileModel = file;
        setView(new FileDetailView(file, getViewNode()));
        load();
        NovaServerSession session = lookup(NovaServerSession.class);
        addActionListener(download, evt->{
            evt.consume();
            Entity toDownload = evt.getEntity();
            if (!toDownload.isFalsey(downloadUrl)) {
                String authenticatedUrl = session.createAuthenticatedURL(toDownload.getText(downloadUrl), true);
                System.out.println(authenticatedUrl);
                CN.execute(authenticatedUrl);
            }
            
            
        });
    }

    @Override
    protected void load(String rawContent, Element doc) {
        /*
        <html><template name="DownloadWindow"></template>
<part name="Name">NovaTerm</part>
<part name="from">Sysop</part>
<part name="size">4060651</part>
<part name="accessed">0</part>
<part name="kind">document
<part name="sent at">Wed, 29 Apr 2020 19:59:12 -0700</part>
<part name="receive file"><a href="/library/Client Software/1.61183919.Golden.Beta/"></a></part>
<part name="body"><FONT NAME="Geneva" SIZE=10pt>This is the client for this server

</part></template></html>
        */
        
        Result r = Result.fromContent(doc);
        FileModel detailModel = fileModel;
        detailModel.set(name, r.getAsString("//part[@name='Name']"));
        detailModel.set(postedBy, r.getAsString("//part[@name='from']"));
        detailModel.setText(contentSize, r.getAsString("//part[@name='size']"));
        detailModel.setText(FileSchema.downloadCount, r.getAsString("//part[@name='accessed']"));
        String kindStr = r.getAsString("//part[@name='kind']");
        if (kindStr != null && kindStr.indexOf(" ") != -1) {
            kindStr = kindStr.substring(0, kindStr.indexOf(" "));
        }
        detailModel.setText(kind, kindStr);
        detailModel.setText(FileSchema.date, r.getAsString("//part[@name='sent at']"));
        String dlUrl = "";
        Element a = (Element)r.get("//part[@name='receive file']/a");
        if (a != null) {
            dlUrl = a.getAttribute("href");
        }
        detailModel.setText(downloadUrl, dlUrl);
        //detailModel.setText(description, r.getAsString("//part[@name='body']"));
        String[] parts = Util.split(rawContent, "<part name=\"body\">");
        String desc = "";
        if (parts.length > 1) {
            parts = Util.split(parts[1], "</part>");
            desc = parts[0];
        }
        detailModel.setText(description, desc);
        setTitle(detailModel.getText(name));
        Form f = getView().getComponentForm();
        if (f != null) {
            f.revalidateWithAnimationSafety();
        }
        
        
        
    }

    @Override
    protected ViewNode createViewNode() {
        ViewNode out = super.createViewNode();
        out.setAttributes(UI.actions(FileDetailView.DOWNLOAD_FILE, download));
        return out;
    }
    
    
    
}
