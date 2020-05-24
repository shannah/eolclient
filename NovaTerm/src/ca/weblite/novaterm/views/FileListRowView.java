/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.schemas.FileSchema;
import com.codename1.rad.models.Entity;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ActionNode.Category;
import com.codename1.rad.nodes.FieldNode;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.propertyviews.LabelPropertyView;
import com.codename1.rad.schemas.Thing;
import com.codename1.rad.ui.AbstractEntityView;
import com.codename1.rad.ui.EntityView;
import com.codename1.rad.ui.UI;
import com.codename1.ui.Button;
import static com.codename1.ui.CN.NORTH;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.GridLayout;


/**
 *
 * @author shannah
 */
public class FileListRowView extends AbstractEntityView {
    private ViewNode node;
    private Label nameLabel, dateLabel, sizeLabel;
    private Button iconButton;
    public static final Category FILE_ROW_CLICKED = new Category();

    public FileListRowView(Entity entity, ViewNode node) {
        super(entity);
        this.node = node;
        nameLabel = new Label();
        dateLabel = new Label();
        sizeLabel = new Label();
        iconButton = new Button();
        iconButton.setMaterialIcon(FontImage.MATERIAL_FILE_DOWNLOAD);
        setLeadComponent(iconButton);
        
        setLayout(new BorderLayout());
        LabelPropertyView pv = new LabelPropertyView(nameLabel, entity, FieldNode.createWithTags(Thing.name));
        add(CENTER, pv);
        Container south = new Container(new GridLayout(1, 2));
        south.add(new LabelPropertyView(dateLabel, entity, FieldNode.createWithTags(FileSchema.date)));
        south.add(
                new LabelPropertyView(
                        sizeLabel, 
                        entity, 
                        new FieldNode(
                                UI.tags(FileSchema.contentSize), 
                                UI.textFormat(in->{return in+" bytes";})
                        )
                )
        );
        add(BorderLayout.SOUTH, south);
        add(BorderLayout.WEST, iconButton);
        
        iconButton.addActionListener(e->{
            
            ActionNode action = node.getInheritedAction(FILE_ROW_CLICKED);
            if (action != null) {
                
                ActionEvent evt = action.fireEvent(entity, this);
                if (evt.isConsumed()) {
                    e.consume();
                }
            }
        });
        
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
