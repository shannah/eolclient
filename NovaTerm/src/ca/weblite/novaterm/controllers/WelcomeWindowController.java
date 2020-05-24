/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.controllers;

import ca.weblite.novaterm.views.WelcomeWindowView;
import ca.weblite.novaterm.views.WelcomeWindowView.WelcomeWindowModel;
import com.codename1.rad.controllers.Controller;
import com.codename1.rad.controllers.FormController;
import static com.codename1.rad.models.EntityType.label;
import com.codename1.rad.nodes.ActionNode;
import com.codename1.rad.nodes.ViewNode;
import static com.codename1.rad.ui.UI.actions;

/**
 *
 * @author shannah
 */
public class WelcomeWindowController extends FormController {
    public static ActionNode next = new ActionNode(label("Main Menu"));
    public WelcomeWindowController(Controller parent, WelcomeWindowModel model) {
        super(parent);
        WelcomeWindowView view = new WelcomeWindowView(model, getViewNode());
        setView(view);
        addActionListener(next, evt->{
            evt.consume();
            new DesignController(this, "17").show();
        });
    }

    @Override
    protected ViewNode createViewNode() {
        ViewNode node = super.createViewNode();
        node.setAttributes(actions(WelcomeWindowView.GO_TO_MAIN_MENU, next));
        return node;
    }
    
    
}
