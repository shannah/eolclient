/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;

import ca.weblite.novaterm.models.LoginFormViewModel;
import ca.weblite.novaterm.schemas.LoginAction;
import ca.weblite.novaterm.services.NovaServerSession;
import ca.weblite.novaterm.views.LoginFormView;
import com.codename1.rad.controllers.Controller;
import com.codename1.rad.controllers.FormController;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ViewNode;
import static com.codename1.rad.ui.UI.actions;
import static com.codename1.rad.ui.UI.label;

/**
 *
 * @author shannah
 */
public class LoginFormController extends FormController implements LoginAction {
    
    private ActionNode login = new ActionNode(
            label("Login")
    );
    
    public LoginFormController(Controller parent) {
        super(parent);
        LoginFormViewModel viewModel = new LoginFormViewModel();
        setTitle("Login");
        setView(new LoginFormView(viewModel, getViewNode()));
        final NovaServerSession session = this.lookup(NovaServerSession.class);
        addActionListener(login, evt->{
            evt.consume();
            session.connect(viewModel.getText(username), viewModel.getText(password));
            
        });
    }

    @Override
    protected ViewNode createViewNode() {
        ViewNode node =  super.createViewNode();
        node.setAttributes(actions(LoginFormView.LOGIN_ACTION, login));
        return node;
    }
    
    
}
