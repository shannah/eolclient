/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;

import ca.weblite.novaterm.models.FileModel;
import ca.weblite.novaterm.models.MessageForumModel;
import ca.weblite.novaterm.models.MessageModel;
import ca.weblite.novaterm.schemas.FileSchema;
import static ca.weblite.novaterm.schemas.FileSchema.contentSize;
import static ca.weblite.novaterm.schemas.FileSchema.description;
import static ca.weblite.novaterm.schemas.FileSchema.downloadUrl;
import static ca.weblite.novaterm.schemas.FileSchema.kind;
import static ca.weblite.novaterm.schemas.FileSchema.name;
import static ca.weblite.novaterm.schemas.FileSchema.postedBy;
import ca.weblite.novaterm.schemas.Message;
import ca.weblite.novaterm.schemas.MessageForum;
import ca.weblite.novaterm.views.MessageDetailView;
import com.codename1.io.Util;
import com.codename1.processing.Result;
import com.codename1.rad.controllers.Controller;
import static com.codename1.rad.models.EntityType.label;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.UI;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.xml.Element;

/**
 *
 * @author shannah
 */
public class MessageFormController extends NTBaseFormController {
    private MessageModel model;
    
    public static final ActionNode nextMessage = new ActionNode(
            label("Next Message"), UI.icon(FontImage.MATERIAL_ARROW_RIGHT),
            UI.enabledCondition(entity->{
                return !entity.isFalsey(Message.nextMessageUrl);
            })
    );
    public static final ActionNode prevMessage = new ActionNode(
            label("Previous Message"), UI.icon(FontImage.MATERIAL_ARROW_LEFT),
            UI.enabledCondition(entity->{
                return !entity.isFalsey(Message.prevMessageUrl);
            })
    );
    public static final ActionNode nextThread = new ActionNode(
            label("Next Thread"), UI.icon(FontImage.MATERIAL_ARROW_UPWARD),
            UI.enabledCondition(entity->{
                return !entity.isFalsey(Message.nextThreadUrl);
            })
            
    );
    public static final ActionNode prevThread = new ActionNode(
            label("Previous Thread"), UI.icon(FontImage.MATERIAL_ARROW_DOWNWARD),
    UI.enabledCondition(entity->{
                return !entity.isFalsey(Message.prevThreadUrl);
            })
    );
    
    public MessageFormController(Controller parent, MessageModel model ) {
        super(parent, model.getText(Message.url));
        this.model = model;
        setView(new MessageDetailView(model, getViewNode()));
        load();
        
        addActionListener(nextMessage, evt->{
            evt.consume();
            MessageModel nextModel = new MessageModel();
            nextModel.setText(Message.url, model.getText(Message.nextMessageUrl));
            new MessageFormController(getParent(), nextModel).show();
        });
        
        addActionListener(prevMessage, evt->{
            evt.consume();
            MessageModel prevModel = new MessageModel();
            prevModel.setText(Message.url, model.getText(Message.prevMessageUrl));
            
            new MessageFormController(getParent(), prevModel).show();
        });
        
        addActionListener(nextThread, evt->{
            evt.consume();
            MessageModel nextModel = new MessageModel();
            nextModel.setText(Message.url, model.getText(Message.nextThreadUrl));
            if (model.getText(Message.nextThreadUrl).endsWith("/")) {
                new MessageForumController(getParent(),model.getText(Message.nextThreadUrl)).show();
            } else {
                new MessageFormController(getParent(), nextModel).show();
            }
        });
        
        addActionListener(prevThread, evt->{
            evt.consume();
            MessageModel prevModel = new MessageModel();
            prevModel.setText(Message.url, model.getText(Message.prevThreadUrl));
            if (model.getText(Message.prevThreadUrl).endsWith("/")) {
                new MessageForumController(getParent(),model.getText(Message.prevThreadUrl)).show();
            } else {
                new MessageFormController(getParent(), prevModel).show();
            }
        });
    }

    @Override
    protected void load(String rawContent, Element doc) {
        
        /*
        

--uyhndkoxsrkfqqjsyvplezzzqdcytwia
Content-type: text/html
Content-Transfer-Encoding: binary

<html><template name="ReadForumWindow"></template>
<part name="Delete Button"><a href="*"></a></part><LINK HREF="10.61217740.Golden.Beta" REL="Next"><LINK HREF="11.61217740.Golden.Beta" REL="NextThread"><LINK HREF="8.61217740.Golden.Beta" REL="Previous">
<part name="from">Sysop</part>
<part name="to">Sysop</part>
<part name="subject">Re: First test message</part>
<part name="sent at">Sun, 24 May 2020 05:45:48 -0700</part>
<part name="body"><FONT NAME="Geneva" SIZE=10pt>This is a reply to the first test message!

</part></template></html>
--uyhndkoxsrkfqqjsyvplezzzqdcytwia--
        */
        
        
        /*
        

--kyfaspxvvgfcdmgbxqfnzawhltifxepw
Content-type: text/html
Content-Transfer-Encoding: binary

<html><template name="ReadForumWindow"></template>
<part name="Delete Button"><a href="*"></a></part><LINK HREF="8.61217740.Golden.Beta" REL="PreviousThread">
<part name="from">Sysop</part>
<part name="to"></part>
<part name="subject">This is a message without reply</part>
<part name="sent at">Sun, 24 May 2020 05:47:14 -0700</part>
<part name="body"><FONT NAME="Geneva" SIZE=10pt>Nobody answer this one

</part></template></html>
--kyfaspxvvgfcdmgbxqfnzawhltifxepw--
        */
         Result r = Result.fromContent(doc);
        MessageModel detailModel = (MessageModel)model;
        
        
        detailModel.setText(Message.subject, r.getAsString("//part[@name='subject']"));
        detailModel.setText(Message.author, r.getAsString("//part[@name='from']"));
        detailModel.setText(Message.datePosted, r.getAsString("//part[@name='sent at']"));
        detailModel.setText(Message.attentionTo, r.getAsString("//part[@name='to']"));
        
        Element nextMessageLink = (Element)r.get("//LINK[@REL='Next']");
        Element prevMessageLink = (Element)r.get("//LINK[@REL='Previous']");
        Element prevThreadLink = (Element)r.get("//LINK[@REL='PreviousThread']");
        Element nextThreadLink = (Element)r.get("//LINK[@REL='NextThread']");
        MessageForumModel forum = lookup(MessageForumModel.class);
        if (forum != null) {
            if (nextMessageLink != null && nextMessageLink.getAttribute("HREF") != null) {
                detailModel.setText(Message.nextMessageUrl, forum.getText(MessageForum.url)+nextMessageLink.getAttribute("HREF"));
            }
            if (prevMessageLink != null && prevMessageLink.getAttribute("HREF") != null) {
                detailModel.setText(Message.prevMessageUrl, forum.getText(MessageForum.url)+prevMessageLink.getAttribute("HREF"));
            }
            if (prevThreadLink != null && prevThreadLink.getAttribute("HREF") != null) {
                detailModel.setText(Message.prevThreadUrl, forum.getText(MessageForum.url)+prevThreadLink.getAttribute("HREF"));
            }
            if (nextThreadLink != null && nextThreadLink.getAttribute("HREF") != null) {
                detailModel.setText(Message.nextThreadUrl, forum.getText(MessageForum.url)+nextThreadLink.getAttribute("HREF"));
            }
        }
        
        
        String kindStr = r.getAsString("//part[@name='kind']");
        if (kindStr != null && kindStr.indexOf(" ") != -1) {
            kindStr = kindStr.substring(0, kindStr.indexOf(" "));
        }
        
        

        //detailModel.setText(description, r.getAsString("//part[@name='body']"));
        String[] parts = Util.split(rawContent, "<part name=\"body\">");
        String desc = "";
        if (parts.length > 1) {
            parts = Util.split(parts[1], "</part>");
            desc = parts[0];
        }
        detailModel.setText(Message.body, desc);
        setTitle(detailModel.getText(Message.subject));
        Form f = getView().getComponentForm();
        if (f != null) {
            f.revalidateWithAnimationSafety();
        }
        
    }

    @Override
    protected ViewNode createViewNode() {
        ViewNode out = super.createViewNode();
        out.setAttributes(
                UI.actions(MessageDetailView.NEXT_MESSAGE, nextMessage),
                UI.actions(MessageDetailView.PREV_MESSAGE, prevMessage),
                UI.actions(MessageDetailView.NEXT_THREAD, nextThread),
                UI.actions(MessageDetailView.PREV_THREAD, prevThread)
        );
        return out;
    }
    
    
    
    
}
