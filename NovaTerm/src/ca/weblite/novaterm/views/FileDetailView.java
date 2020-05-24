/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.schemas.FileSchema;
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
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;

/**
 *
 * @author shannah
 */
public class FileDetailView extends AbstractEntityView implements FileSchema {
    private ViewNode node;
    private LabelPropertyView nameLabel, sizeLabel, typeLabel, dateLabel, uploadedByLabel, icon;
    private HTMLComponentPropertyView descriptionLabel;
    //private Button downloadButton;
    public static final Category DOWNLOAD_FILE = new Category();
    
    public FileDetailView(Entity entity, ViewNode node) {
        super(entity);
        setLayout(new BorderLayout());
        this.node = node;
        setUIID("FileDetailView");
        String base = "FileDetailView";
        UIBuilder ui = new UIBuilder(entity, node);
        nameLabel = ui.label(name);
        sizeLabel = ui.label(contentSize);
        typeLabel = ui.label(kind);
        dateLabel = ui.label(date);
        descriptionLabel = ui.htmlComponent(description);
        icon = ui.label(ListRowItem.icon);
        
        Container north = new Container(new BorderLayout());
        north.add(WEST, icon);
        ActionNode downloadAction = node.getInheritedAction(DOWNLOAD_FILE);
        if (downloadAction != null) {
            north.add(EAST, downloadAction.createView(entity));
        }
        
        north.add(CENTER, BoxLayout.encloseY(nameLabel, sizeLabel, typeLabel));
        add(NORTH, north);
        add(CENTER, descriptionLabel);
        
        
        
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
