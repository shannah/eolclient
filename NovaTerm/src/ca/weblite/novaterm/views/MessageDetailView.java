/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.schemas.FileSchema;
import ca.weblite.novaterm.schemas.Message;
import com.codename1.components.SpanLabel;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.PropertySelector;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ActionNode.Category;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.propertyviews.HTMLComponentPropertyView;
import com.codename1.rad.propertyviews.LabelPropertyView;
import com.codename1.rad.propertyviews.SpanLabelPropertyView;
import com.codename1.rad.schemas.ListRowItem;
import com.codename1.rad.schemas.Thing;
import com.codename1.rad.ui.AbstractEntityView;
import com.codename1.rad.ui.UIBuilder;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.EAST;
import static com.codename1.ui.CN.NORTH;
import static com.codename1.ui.CN.WEST;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;

/**
 *
 * @author shannah
 */
public class MessageDetailView extends AbstractEntityView {
    
    public static final Category NEXT_MESSAGE = new Category(),
            PREV_MESSAGE = new Category(),
            NEXT_THREAD = new Category(),
            PREV_THREAD = new Category();
    
    private ViewNode node;
    private LabelPropertyView subjectLabel, dateLabel, authorLabel;
    private HTMLComponentPropertyView bodyLabel;
    //private Button downloadButton;
    public static final Category DOWNLOAD_FILE = new Category();
    
    public MessageDetailView(Entity entity, ViewNode node) {
        super(entity);
        setLayout(new BorderLayout());
        this.node = node;
        setUIID("MessageDetailView");
        String base = "MessageDetailView";
        UIBuilder ui = new UIBuilder(entity, node);
        subjectLabel = ui.label(Message.subject);
        
        dateLabel = ui.label(Message.datePosted);
        bodyLabel = ui.htmlComponent(Message.body);
        
        authorLabel = ui.label(Message.author);
        
        Container north = new Container(new BorderLayout());
        north.add(WEST, new Button(FontImage.MATERIAL_MESSAGE));
        
        
        north.add(CENTER, BoxLayout.encloseY(subjectLabel, authorLabel));
        
        ActionNode nextMessageAction = node.getInheritedAction(NEXT_MESSAGE);
        ActionNode prevMessageAction = node.getInheritedAction(PREV_MESSAGE);
        ActionNode nextThreadAction = node.getInheritedAction(NEXT_THREAD);
        ActionNode prevThreadAction = node.getInheritedAction(PREV_THREAD);
        
        Container navBar = new Container(BoxLayout.x());
        Container threadNav = new Container(BoxLayout.y());
        if (nextThreadAction != null) {
            threadNav.add(nextThreadAction.createView(entity));
        }
        if (prevThreadAction != null) {
            threadNav.add(prevThreadAction.createView(entity));
        }
        navBar.add(threadNav);
        if (prevMessageAction != null) {
            navBar.add(prevMessageAction.createView(entity));
        }
        if (nextMessageAction != null) {
            navBar.add(nextMessageAction.createView(entity));
        }
        add(NORTH, BoxLayout.encloseY(north, navBar));
        add(CENTER, bodyLabel);
        
        
        
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
