/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;

import ca.weblite.novaterm.events.RefreshEvent;
import ca.weblite.novaterm.models.MessageForumModel;
import ca.weblite.novaterm.models.MessageModel;
import ca.weblite.novaterm.models.PostMessageModel;
import ca.weblite.novaterm.schemas.Message;
import ca.weblite.novaterm.schemas.MessageForum;
import ca.weblite.novaterm.schemas.PostMessageSchema;
import ca.weblite.novaterm.services.NovaServerSession;
import ca.weblite.novaterm.views.PostMessageView;
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
public class PostMessageController extends NTBaseFormController {
    private MessageForumModel forum;
    private MessageModel message;
    private PostMessageModel post;
    public static final ActionNode selectAttachment = new ActionNode(UI.label("Select File..."));
    public static final ActionNode submit = new ActionNode(UI.label("Submit"));
    
    public PostMessageController(Controller parent, MessageForumModel forum) {
        this(parent, forum, null);
    }
    public PostMessageController(Controller parent, MessageForumModel forum, MessageModel replyTo) {
        super(parent);
        this.forum = forum;
        this.message = new MessageModel();
        if (replyTo != null) {
            this.message.setText(Message.attentionTo, replyTo.getText(Message.author));
            String sub = replyTo.getText(Message.subject);
            if (sub == null) {
                sub = "";
            }
            if (!sub.toLowerCase().startsWith("re:")) {
                sub = "Re: "+sub;
            }
            this.message.setText(Message.subject, sub);
        }
        message.set(Message.forum, forum);
        this.post = new PostMessageModel();
        post.set(PostMessageSchema.message, message);
        NovaServerSession session = lookup(NovaServerSession.class);
        setTitle("Post Message");
        setView(new PostMessageView(post, getViewNode()));
        
        addActionListener(selectAttachment, evt->{
            evt.consume();
            if (FileChooser.isAvailable()) {
                FileChooser.showOpenDialog(false, "*", e2->{
                    String filePath = (String)e2.getSource();
                    if (filePath != null) {
                        message.setText(Message.attachment, filePath);
                    }
                });
            }
        });
        
        addActionListener(submit, evt->{
            evt.consume();
            if (message.isEmpty(Message.attentionTo)) {
                message.setText(Message.attentionTo, "");
            }
            if (message.isEmpty(Message.subject)) {
                message.setText(Message.subject, "No subject");
                return;
            }
            if (message.isEmpty(Message.body)) {
                ToastBar.showErrorMessage("Body is required");
                return;
            }
            String attachment = null;
            if (!message.isEmpty(Message.attachment)) {
                File file = new File(message.getText(Message.attachment));
                
                if (!file.exists()) {
                    ToastBar.showErrorMessage("The file "+file+" could not be found.");
                    return;
                }
                attachment = file.getAbsolutePath();
            }
            
            
            ToastBar.Status status = ToastBar.getInstance().createStatus();
            status.setMessage("Posting message...");
            status.setShowProgressIndicator(true);
            status.show();
            
            session.httpPostMessage(
                    replyTo == null ? forum.getText(MessageForum.url) : replyTo.getText(Message.url), 
                    message.getText(Message.attentionTo), 
                    message.getText(Message.subject), 
                    message.getText(Message.body),
                    attachment
            )
                    .onResult((res, err)->{
                        if (err != null) {
                            Log.e(err);
                            ToastBar.showErrorMessage(err.getMessage());
                            return;
                        }
                        
                        if (!res) {
                            ToastBar.showErrorMessage("Post failed.");
                            return;
                        }
                        
                        ToastBar.showInfoMessage("Post successful");
                        // We want to trigger the library to refresh itself.
                        ActionSupport.dispatchEvent(new RefreshEvent(getView()));
                        
                        
                    });
            
        });
        
    }

    @Override
    protected ViewNode createViewNode() {
        ViewNode out = super.createViewNode(); 
        out.setAttributes(
                UI.actions(PostMessageView.SELECT_ATTACHMENT, selectAttachment),
                UI.actions(PostMessageView.SUBMIT, submit)
        );
        return out;
    }
    
    
}
