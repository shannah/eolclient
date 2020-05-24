/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.views;

import ca.weblite.novaterm.schemas.LoginAction;
import com.codename1.rad.models.Entity;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ActionNode.Category;
import com.codename1.rad.nodes.Node;
import com.codename1.rad.nodes.ViewNode;
import com.codename1.rad.ui.AbstractEntityView;
import com.codename1.rad.ui.EntityEditor;
import com.codename1.rad.ui.UI;
import com.codename1.ui.layouts.BorderLayout;

/**
 *
 * @author shannah
 */
public class LoginFormView extends AbstractEntityView<Entity> implements LoginAction {
    private ViewNode node;
    private EntityEditor form;
    public static final Category LOGIN_ACTION = new Category();
   
    public LoginFormView(Entity viewModel, ViewNode node) {
        super(viewModel);
        this.node = node;
        setLayout(new BorderLayout());
        
        ActionNode loginAction = node.getAction(LOGIN_ACTION);
        form = new EntityEditor(viewModel, new UI(){{
            form(
                    editable(true),
                    columns(1),
                    label("Login"),
                    description("Please enter the username and password to login"),
                    textField(
                            label("Username"),
                            tags(username)
                    ),
                    textField(
                            label("Password"),
                            tags(password)
                    ),
                    actions(BOTTOM_RIGHT_MENU, loginAction)
                    
            );
        }});
        add(CENTER, form);
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
