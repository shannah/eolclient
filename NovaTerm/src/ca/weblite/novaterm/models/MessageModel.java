/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.models;

import ca.weblite.novaterm.schemas.Message;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityType;

/**
 *
 * @author shannah
 */
public class MessageModel extends Entity {
    public static final EntityType TYPE = new EntityType(){{
        string(Message.attentionTo);
        string(Message.subject);
        string(Message.body);
        string(Message.author);
        date(Message.datePosted);
        string(Message.attachment);
        list(Messages.class, Message.messages);
        Integer(Message.numMessages);
        string(Message.url);
        string(Message.nextMessageUrl);
        string(Message.prevMessageUrl);
        string(Message.nextThreadUrl);
        string(Message.prevThreadUrl);
        
        
    }};
    
    {
        setEntityType(TYPE);
    }

    
}
