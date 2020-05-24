/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.schemas.FileLibrary;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityList;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ActionNode.Category;
import com.codename1.rad.nodes.ListNode;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.AbstractEntityView;
import com.codename1.rad.ui.NodeList;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;

/**
 *
 * @author shannah
 */
public class FileLibraryView extends AbstractEntityView {
    private ViewNode node;
    public static final Category UPLOAD_FILE = new Category();
    private Button sysopReleaseButton, sysopDeleteButton;
    private FileListView fileList;
    
    
    public FileLibraryView(Entity model, ViewNode node) {
        super(model);
        this.node = node;
        
        Component sendFileButton;
        ActionNode sendAction = node.getInheritedAction(UPLOAD_FILE);
        if (sendAction != null) {
            sendFileButton = sendAction.createView(model);
        } else {
            sendFileButton = new Button("Upload");
        }
        
        sysopReleaseButton = new Button("Release");
        sysopDeleteButton = new Button("Delete");
        
        EntityList files = model.getEntityList(FileLibrary.files);
        NodeList listNodes = node.getChildNodes(ListNode.class);
        ListNode listNode;
        if (listNodes.isEmpty()) {
            listNode = new ListNode();
            listNode.setParent(node);
        } else {
            listNode = (ListNode)listNodes.iterator().next();
        }
        
        fileList = new FileListView(files, listNode);
        fileList.setAnimateInsertions(false);
        fileList.setAnimateRemovals(false);
        setLayout(new BorderLayout());
        Container north = new Container(new BorderLayout());
        north.add(BorderLayout.EAST, sendFileButton);
        north.add(BorderLayout.WEST, BoxLayout.encloseX(sysopReleaseButton, sysopDeleteButton));
        add(BorderLayout.NORTH, north);
        add(BorderLayout.CENTER, fileList);
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
