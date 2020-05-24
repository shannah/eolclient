/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.models.FileLibraryModel;
import ca.weblite.novaterm.schemas.FileLibrary;
import ca.weblite.novaterm.schemas.FileUploadSchema;
import com.codename1.io.File;
import com.codename1.rad.models.Entity;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ActionNode.Category;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.propertyviews.LabelPropertyView;
import com.codename1.rad.propertyviews.TextAreaPropertyView;
import com.codename1.rad.propertyviews.TextFieldPropertyView;
import com.codename1.rad.ui.AbstractEntityView;
import com.codename1.rad.ui.UI;
import com.codename1.rad.ui.UIBuilder;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;

/**
 *
 * @author shannah
 */
public class FileUploadForm extends AbstractEntityView implements FileUploadSchema {
    private ViewNode node;
    public static final Category SELECT_ATTACHMENT = new Category();
    public static final Category SUBMIT = new Category();
    
    public FileUploadForm(Entity entity, ViewNode node) {
        super(entity);
        this.node = node;
        FileLibraryModel lib = (FileLibraryModel)entity.getEntity(library);
        if (lib == null) {
            throw new IllegalStateException("FileUpload entity must have a library registered");
        }
        UIBuilder ui = new UIBuilder(entity, node);
        LabelPropertyView errorLabel = ui.label(errorMessage);
        TextFieldPropertyView nameField = ui.textField(name);
        TextFieldPropertyView subjectField = ui.textField(subject);
        TextAreaPropertyView descriptionField = ui.textArea(description);
        LabelPropertyView attachmentField = ui.label(attachment);
        attachmentField.getField().setAttributes(UI.textFormat(str->{
            if (str != null) {
                return new File(str).getName();
            }
            return str;
        }));
        
        ActionNode action = node.getInheritedAction(SELECT_ATTACHMENT);
        Component attachButton = null;
        if (action != null) {
            attachButton = action.createView(entity);
        } else {
            attachButton = new Button("Select...");
        }
        
        ActionNode submit = node.getInheritedAction(SUBMIT);
        Component submitButton = null;
        if (submit != null) {
            submitButton = submit.createView(entity);
        } else {
            submitButton = new Button("Submit");
        }
        
        setLayout(BoxLayout.y());
        addAll(
                errorLabel,
                new Label("Name"),
                nameField,
                new Label("Comment"),
                subjectField,
                new Label("Attachment"),
                BoxLayout.encloseX(attachmentField, attachButton),
                new Label("Description"),
                descriptionField,
                FlowLayout.encloseCenter(submitButton)
        );
        
        
        
    }
    
    @Override
    public void update() {
        
    }

    @Override
    public void commit() {
        
    }

    @Override
    public Node getViewNode() {
        return node;
    }
    
}
