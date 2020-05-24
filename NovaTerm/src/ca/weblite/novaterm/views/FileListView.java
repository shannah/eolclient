/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityList;
import com.codename1.rad.nodes.ListNode;
import com.codename1.rad.ui.DefaultEntityListCellRenderer;
import com.codename1.rad.ui.EntityView;
import com.codename1.rad.ui.entityviews.EntityListView;

/**
 *
 * @author shannah
 */
public class FileListView extends EntityListView {
    public FileListView(EntityList model, ListNode node) {
        super(model, node);
        setListCellRenderer(new FileListRowRenderer());
    }
    
    public static class FileListRowRenderer extends DefaultEntityListCellRenderer {

        @Override
        public EntityView getListCellRendererComponent(EntityListView list, Entity value, int index, boolean isSelected, boolean isFocused) {
            ListNode node = (ListNode)list.getViewNode();
            return new FileListRowView(value, node.getRowTemplate());
        }
        
    }
}
