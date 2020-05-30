/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.models.Messages;
import ca.weblite.novaterm.schemas.MessageForum;
import com.codename1.rad.models.Entity;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ActionNode.Category;
import com.codename1.rad.nodes.ListNode;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.AbstractEntityView;
import com.codename1.rad.ui.Actions;
import com.codename1.rad.ui.DefaultEntityListCellRenderer;
import com.codename1.rad.ui.EntityView;
import com.codename1.rad.ui.NodeList;
import com.codename1.rad.ui.entityviews.EntityListView;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.List;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.list.DefaultListCellRenderer;

/**
 *
 * @author shannah
 */
public class MessageForumView extends AbstractEntityView {

    public static final Category FORUM_ACTIONS = new Category();
    
    private ViewNode node;
    
    public MessageForumView(Entity entity, ViewNode node) {
        super(entity);
        this.node = node;
        setLayout(new BorderLayout());
        Messages messages = (Messages)entity.get(MessageForum.messages);
        if (messages == null) {
            messages = new Messages();
            entity.set(MessageForum.messages, messages);
        }
        ListNode list = (ListNode)node.getChildNode(ListNode.class);
        if (list == null) {
            list = new ListNode();
            list.setParent(node);
        }
        
        EntityListView messageList = new EntityListView(messages, list);
        messageList.setListCellRenderer(new RowRenderer());
        add(BorderLayout.CENTER, messageList);
        Actions actions = node.getInheritedActions(FORUM_ACTIONS);
        Container toolbar = new Container(new GridLayout(actions.size()));
        actions.addToContainer(toolbar, entity);
        add(BorderLayout.NORTH, toolbar);
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
    
    public static class RowRenderer extends DefaultEntityListCellRenderer {

        @Override
        public EntityView getListCellRendererComponent(EntityListView list, Entity value, int index, boolean isSelected, boolean isFocused) {
            ListNode node = (ListNode)list.getViewNode();
            return new MessageForumRowView(value, node.getRowTemplate());
        }
        
        
    }
    
}
