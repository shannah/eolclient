/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;


import ca.weblite.novaterm.schemas.Message;
import static ca.weblite.novaterm.util.TypeUtil.toInt;
import com.codename1.rad.controllers.ActionSupport;
import com.codename1.rad.models.Entity;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ActionNode.Category;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.AbstractEntityView;
import com.codename1.rad.ui.UIBuilder;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.EAST;
import static com.codename1.ui.CN.WEST;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;

/**
 *
 * @author shannah
 */
public class MessageForumRowView extends AbstractEntityView {
    private ViewNode node;
    
    public static final Category MESSAGE_CLICKED = new Category();
    
    public MessageForumRowView(Entity entity, ViewNode node) {
        super(entity);
        this.node = node;
        UIBuilder uib = new UIBuilder(entity, node);
        int numMessages = Math.max(toInt(entity.getInt(Message.numMessages), 1),1 );
        char iconChar = numMessages > 1 ? FontImage.MATERIAL_FOLDER : FontImage.MATERIAL_MESSAGE;
        setLayout(new BorderLayout());
        Button btn = new Button(iconChar);
        setLeadComponent(btn);
        add(WEST, btn);
        btn.addActionListener(evt->{
            ActionNode action = node.getInheritedAction(MESSAGE_CLICKED);
            if (action != null) {
                evt.consume();
                action.fireEvent(entity, MessageForumRowView.this);
            }
        });
        
        Container c = new Container(BoxLayout.y());
        c.add(uib.label(Message.author));
        c.add(uib.label(Message.subject));
        c.add(uib.label(Message.datePosted));
        add(CENTER, c);
        
        add(EAST, uib.label(Message.numMessages));
        
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
