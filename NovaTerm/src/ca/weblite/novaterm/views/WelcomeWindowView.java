/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.schemas.WelcomeWindow;
import com.codename1.rad.controllers.ActionSupport;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityType;
import com.codename1.rad.models.Property.Label;
import com.codename1.rad.models.PropertyChangeEvent;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ActionNode.Category;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.AbstractEntityView;
import com.codename1.rad.ui.EntityView;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Component;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;


/**
 *
 * @author shannah
 */
public class WelcomeWindowView extends AbstractEntityView implements WelcomeWindow {
    private BrowserComponent bodyView;
    private ViewNode node;
    public static final Category GO_TO_MAIN_MENU = new Category();
    
    private final ActionListener<PropertyChangeEvent> bodyListener = evt->{
        if (bodyView != null) {
            bodyView.setPage(getEntity().getText(body), "/");
        }
    };
    
    public static class WelcomeWindowModel extends Entity {
        public static final EntityType TYPE = new EntityType(){{
            string(body);
        }};
        {
            setEntityType(TYPE);
        }
    }
    
    public WelcomeWindowView(Entity entity, ViewNode node) {
        super(entity);
        this.node = node;

        bodyView = new BrowserComponent();
        String bodyText = entity.getText(body);
        bodyView.setPage("<!doctype html>\r\n"+entity.getText(body).trim(), "/");
        setLayout(new BorderLayout());
        
        add(CENTER, bodyView);
        ActionNode goToMain = node.getAction(GO_TO_MAIN_MENU);
        if (goToMain != null) {
            addComponent(BorderLayout.SOUTH, (Component)goToMain.createView(getEntity()));
        }
        
        
    }

    @Override
    public void bind() {
        super.bind();
        getEntity().addPropertyChangeListener(getEntity().getEntityType().findProperty(body), bodyListener);
    }

    @Override
    public void unbind() {
        getEntity().removePropertyChangeListener(getEntity().getEntityType().findProperty(body), bodyListener);
        super.unbind(); //To change body of generated methods, choose Tools | Templates.
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
