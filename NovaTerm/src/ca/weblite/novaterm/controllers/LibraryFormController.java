/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;

import ca.weblite.novaterm.events.RefreshEvent;
import ca.weblite.novaterm.models.FileLibraryModel;
import ca.weblite.novaterm.models.FileModel;
import ca.weblite.novaterm.models.FileUploadModel;
import ca.weblite.novaterm.schemas.FileLibrary;
import ca.weblite.novaterm.schemas.FileSchema;
import ca.weblite.novaterm.schemas.FileUploadSchema;
import static ca.weblite.novaterm.util.XMLUtil.getText;
import ca.weblite.novaterm.views.FileLibraryView;
import ca.weblite.novaterm.views.FileListRowView;
import com.codename1.processing.Result;
import com.codename1.rad.controllers.Controller;
import com.codename1.rad.controllers.ControllerEvent;
import com.codename1.rad.models.EntityList;
import static com.codename1.rad.models.EntityType.label;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.UI;
import com.codename1.xml.Element;
import java.util.List;

/**
 *
 * @author shannah
 */
public class LibraryFormController extends NTBaseFormController implements FileSchema{
    private FileLibraryModel model;
    private static final ActionNode viewDetails = new ActionNode(),
            uploadFile = new ActionNode(label("Upload"));
    
    private String url;
    public LibraryFormController(Controller parent, String url) {
        super(parent, url);
        this.url = url;
        model = new FileLibraryModel();
        model.setText(FileLibrary.url, url);
        setView(new FileLibraryView(model, getViewNode()));
        load();
        addActionListener(viewDetails, evt->{
            evt.consume();
            new LibraryFileDetailController(this, (FileModel)evt.getEntity()).show();
            
        });
        
        addActionListener(uploadFile, evt->{
            evt.consume();
            FileUploadModel uploadModel = new FileUploadModel();
            uploadModel.setEntity(FileUploadSchema.library, model);
            new FileUploadFormController(this, uploadModel).show();
        });
    }

    @Override
    protected void load(String rawContent, Element doc) {
        /*
        --xjdjdsdivncsjdjvfifdvdfvd
        Content-type: text/html
        Content-encoding: binary

        <html><template name="ListLibraryWindow"><title>Client Software</title>
        <part name="Sysop Release Button"><a href="*"></a></part>
        <part name="Sysop Delete Button"><a href="*"></a></part>
        <part name="Send File Button"><a href="*"></a></part>
        <part name="List Library Part"><ul>
        <li><a href="1.61183919.Golden.Beta">*<ni>NovaTerm<ni>Wed, 29 Apr 2020 19:59:12 -0700<ni>4060651<ni>0<ni><ni></a>
        </ul></part></template></html>

        --xjdjdsdivncsjdjvfifdvdfvd--

        */
        updateFileLibraryModel(model, doc);
        setTitle(model.getText(FileLibraryModel.name));
        getView().revalidateWithAnimationSafety();
        /*
        Result r = Result.fromContent(doc);
        String title = r.getAsString("//title");
        */
        
        
        
    }

    @Override
    protected ViewNode createViewNode() {
        ViewNode node = super.createViewNode();
        node.setAttributes(
                UI.actions(FileListRowView.FILE_ROW_CLICKED, viewDetails),
                UI.actions(FileLibraryView.UPLOAD_FILE, uploadFile)
        );
        return node;
    }
    
    
    
    private FileModel createFileModel(Element a) {
        FileModel out = new FileModel();
        out.set(detailsUrl, url +a.getAttribute("href"));
        List<Element> children = (List<Element>)a.getChildrenByTagName("ni");
        out.setText(name, getText(children.get(0)));
        out.setText(date, getText(children.get(1)));
        out.setText(contentSize, getText(children.get(2)));
        out.setText(downloadCount, getText(children.get(3)));
        return out;
    }
    
    private EntityList fillFileList(Element root, EntityList out) {
        Result r = Result.fromContent(root);
        List<Element> files = r.getAsArray("//li/i/a");
        for (Element el : files) {
            out.add(createFileModel(el));
        }
        
        files = r.getAsArray("//li/a");
        for (Element el : files) {
            out.add(createFileModel(el));
        }
        return out;
    }
    
    private FileLibraryModel updateFileLibraryModel(FileLibraryModel out, Element root) {
        Result r = Result.fromContent(root);
        out.setText(FileLibrary.name, r.getAsString("//title"));
        Element filesPart = (Element)r.get("//part[@name='List Library Part']");
        fillFileList(filesPart, out.getEntityList(FileLibrary.files));
        return out;
        
    }

    @Override
    public void actionPerformed(ControllerEvent evt) {
        if (evt instanceof RefreshEvent) {
            // A refresh event occurred.  We should refresh the form.
            evt.consume();
            new LibraryFormController(getParent(), url).showBack();
            return;
            
        }
        super.actionPerformed(evt);
    }
    
    
    
}
