/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;



import ca.weblite.novaterm.models.MessageForumModel;
import ca.weblite.novaterm.models.MessageModel;
import ca.weblite.novaterm.schemas.FileLibrary;
import ca.weblite.novaterm.schemas.Message;
import ca.weblite.novaterm.schemas.MessageForum;
import static ca.weblite.novaterm.util.TypeUtil.toInt;
import static ca.weblite.novaterm.util.XMLUtil.getText;
import ca.weblite.novaterm.views.MessageForumRowView;
import ca.weblite.novaterm.views.MessageForumView;
import com.codename1.processing.Result;
import com.codename1.rad.controllers.Controller;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityList;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.UI;
import com.codename1.ui.Label;
import com.codename1.xml.Element;
import java.util.List;

/**
 *
 * @author shannah
 */
public class MessageForumController extends NTBaseFormController {
    private MessageForumModel model;
    public static final ActionNode viewMessage = new ActionNode(UI.label("View Message"));
    
    public MessageForumController(Controller parent, String url) {
        super(parent, url);
        this.model = new MessageForumModel();
        model.setText(MessageForum.url, getForumURL());
        this.addLookup(model);
        setView(new MessageForumView(model, getViewNode()));
        load();
        addActionListener(viewMessage, evt->{
            evt.consume();
            Entity messageModel = evt.getEntity();
            int numMessages = toInt(messageModel.getInt(Message.numMessages), 1);
            if (numMessages > 1) {
                //new MessageThreadFormController(this, (MessageModel)messageModel).show();
                new MessageForumController(this, messageModel.getText(Message.url)).show();
            } else {
                new MessageFormController(this, (MessageModel)messageModel).show();
            }
        });
    }

    @Override
    protected void load(String rawContent, Element doc) {
        updateMessageForumModel(model, doc);
        setTitle(model.getText(MessageForum.name));
        getView().revalidateWithAnimationSafety();
    }
    
    private MessageModel createMessageModel(Element a) {
        MessageModel out = new MessageModel();
        out.set(Message.url, getForumURL() +a.getAttribute("href"));
        List<Element> children = (List<Element>)a.getChildrenByTagName("ni");
        String type = getText(children.get(0));
        out.setText(Message.subject, getText(children.get(1)));
        out.setText(Message.author, getText(children.get(2)));
        out.setText(Message.datePosted, getText(children.get(3)));
        out.setText(Message.numMessages, getText(children.get(4)));
        System.out.println("Created message model: "+out.toMap(Message.ALL_TAGS));
        return out;
    }
    
    private EntityList fillMessageList(Element root, EntityList out) {
        Result r = Result.fromContent(root);
        List<Element> files = r.getAsArray("//li/i/a");
        for (Element el : files) {
            out.add(createMessageModel(el));
        }
        
        files = r.getAsArray("//li/a");
        for (Element el : files) {
            out.add(createMessageModel(el));
        }
        return out;
    }
    
    private MessageForumModel updateMessageForumModel(MessageForumModel out, Element root) {
        Result r = Result.fromContent(root);
        out.setText(FileLibrary.name, r.getAsString("//title"));
        Element filesPart = (Element)r.get("//part[@name='List Library Part']");
        fillMessageList(filesPart, out.getEntityList(MessageForum.messages));
        return out;
        
    }

    @Override
    protected ViewNode createViewNode() {
        ViewNode out = super.createViewNode();
        out.setAttributes(UI.actions(MessageForumRowView.MESSAGE_CLICKED, viewMessage));
        return out;
    }
    
    
    public String getForumURL() {
        String u = getURL();
        int newsPos = u.indexOf("/news/");
        int nextSlashPos = u.indexOf("/", newsPos+6);
        String out = u;
        if (nextSlashPos != -1) {
            out = u.substring(0, nextSlashPos+1);
        }
        return out;
        
    }
    
}
