/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.models.FileLibraryModel;
import ca.weblite.novaterm.models.MessageForumModel;
import ca.weblite.novaterm.models.MessageModel;
import ca.weblite.novaterm.schemas.FileLibrary;
import ca.weblite.novaterm.schemas.FileUploadSchema;
import ca.weblite.novaterm.schemas.Message;
import ca.weblite.novaterm.schemas.PostMessageSchema;
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
public class PostMessageView extends AbstractEntityView implements PostMessageSchema, Message {
    private ViewNode node;
    public static final Category SELECT_ATTACHMENT = new Category();
    public static final Category SUBMIT = new Category();
    
    public PostMessageView(Entity entity, ViewNode node) {
        super(entity);
        this.node = node;
        MessageModel message = (MessageModel)entity.getEntity(PostMessageSchema.message);
        MessageForumModel lib = (MessageForumModel)message.getEntity(forum);
        if (lib == null) {
            throw new IllegalStateException("Message entity must have a forum registered");
        }
        UIBuilder ui0 = new UIBuilder(entity, node);
        UIBuilder ui = new UIBuilder(message, node);
        LabelPropertyView errorLabel = ui0.label(errorMessage);
        TextFieldPropertyView toField = ui.textField(Message.attentionTo);
        TextFieldPropertyView subjectField = ui.textField(subject);
        TextAreaPropertyView bodyField = ui.textArea(Message.body);
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
            attachButton = new Button("Attach File...");
        }
        
        ActionNode submit = node.getInheritedAction(SUBMIT);
        Component submitButton = null;
        if (submit != null) {
            submitButton = submit.createView(entity);
        } else {
            submitButton = new Button("Submit");
        }
        
        setLayout(BoxLayout.y());
        addAll(errorLabel,
                new Label("To"),
                toField,
                new Label("Subject"),
                subjectField,
                new Label("Attachment"),
                BoxLayout.encloseX(attachmentField, attachButton),
                new Label("Body"),
                bodyField,
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
