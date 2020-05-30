/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.models;

import com.codename1.rad.models.EntityType;
import com.codename1.rad.models.Entity;
import ca.weblite.novaterm.schemas.PostMessageSchema;

/**
 *
 * @author shannah
 */
public class PostMessageModel extends Entity {
    public static final EntityType TYPE = new EntityType() {{
        entity(MessageModel.class, PostMessageSchema.message);
        string(PostMessageSchema.errorMessage);
    }};
    {
        setEntityType(TYPE);
    }
    
}
