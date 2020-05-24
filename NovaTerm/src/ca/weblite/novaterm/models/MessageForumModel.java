/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.models;

import ca.weblite.novaterm.schemas.MessageForum;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityType;
import com.codename1.rad.schemas.ChatMessage;

/**
 *
 * @author shannah
 */
public class MessageForumModel extends Entity {
    public static final EntityType TYPE = new EntityType() {{
        string(MessageForum.name);
        list(Messages.class, MessageForum.messages);
        string(MessageForum.url);
    }};
    
    {
        setEntityType(TYPE);
        set(MessageForum.messages, new Messages());
    }
}
