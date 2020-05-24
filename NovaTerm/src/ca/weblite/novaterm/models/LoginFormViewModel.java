/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.models;

import ca.weblite.novaterm.schemas.LoginAction;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityType;
import com.codename1.rad.schemas.Person;

/**
 *
 * @author shannah
 */
public class LoginFormViewModel extends Entity implements LoginAction {
    public static final EntityType TYPE = new EntityType(){{
        string(LoginAction.username);
        string(LoginAction.password);
        string(LoginAction.errorMessage);
        string(LoginAction.serverAddress);
        
    }};
    {
        setEntityType(TYPE);
    }
    
    
}
