/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;

import ca.weblite.novaterm.events.RefreshEvent;
import ca.weblite.novaterm.models.FileLibraryModel;
import ca.weblite.novaterm.models.FileUploadModel;
import ca.weblite.novaterm.schemas.FileLibrary;
import ca.weblite.novaterm.schemas.FileUploadSchema;
import ca.weblite.novaterm.services.NovaServerSession;
import ca.weblite.novaterm.views.FileUploadForm;
import com.codename1.components.ToastBar;
import com.codename1.ext.filechooser.FileChooser;
import com.codename1.io.File;
import com.codename1.io.Log;
import com.codename1.rad.controllers.ActionSupport;
import com.codename1.rad.controllers.Controller;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.UI;

/**
 *
 * @author shannah
 */
public class FileUploadFormController extends NTBaseFormController implements FileUploadSchema {
    private FileLibraryModel library;
    public static final ActionNode selectAttachment = new ActionNode(UI.label("Select File..."));
    public static final ActionNode submit = new ActionNode(UI.label("Submit"));
    public FileUploadFormController(Controller parent, FileUploadModel upload) {
        super(parent);

        setTitle("File Upload");
        NovaServerSession session = lookup(NovaServerSession.class);
        setView(new FileUploadForm(upload, getViewNode()));
        
        addActionListener(selectAttachment, evt->{
            evt.consume();
            if (FileChooser.isAvailable()) {
                FileChooser.showOpenDialog(false, "*", e2->{
                    String filePath = (String)e2.getSource();
                    if (filePath != null) {
                        upload.setText(FileUploadSchema.attachment, filePath);
                    }
                });
            }
        });
        
        addActionListener(submit, evt->{
            evt.consume();
            if (upload.isEmpty(name)) {
                ToastBar.showErrorMessage("Name is required");
                return;
            }
            if (upload.isEmpty(subject)) {
                ToastBar.showErrorMessage("Comments are required");
                return;
            }
            if (upload.isEmpty(description)) {
                ToastBar.showErrorMessage("Description is required");
                return;
            }
            
            if (upload.isEmpty(attachment)) {
                ToastBar.showErrorMessage("Please select a file to attach");
                return;
            }
            File file = new File(upload.getText(attachment));
            if (!file.exists()) {
                ToastBar.showErrorMessage("The file "+file+" could not be found.");
                return;
            }
            ToastBar.Status status = ToastBar.getInstance().createStatus();
            status.setMessage("Sending file...");
            status.setShowProgressIndicator(true);
            status.show();
            
            session.httpFileUpload(
                    upload.getEntity(FileUploadSchema.library).getText(FileLibrary.url), 
                    upload.getText(name), 
                    upload.getText(subject), 
                    upload.getText(description),
                    upload.getText(attachment)
            )
                    .onResult((res, err)->{
                        if (err != null) {
                            Log.e(err);
                            ToastBar.showErrorMessage(err.getMessage());
                            return;
                        }
                        
                        if (!res) {
                            ToastBar.showErrorMessage("Upload failed.");
                            return;
                        }
                        
                        ToastBar.showInfoMessage("Upload successful");
                        // We want to trigger the library to refresh itself.
                        ActionSupport.dispatchEvent(new RefreshEvent(getView()));
                        
                        
                    });
            
        });
    }

    
    @Override
    protected ViewNode createViewNode() {
        ViewNode n = super.createViewNode();
        n.setAttributes(UI.actions(FileUploadForm.SELECT_ATTACHMENT, selectAttachment));
        n.setAttributes(UI.actions(FileUploadForm.SUBMIT, submit));
        return n;
    }
    
    
}
