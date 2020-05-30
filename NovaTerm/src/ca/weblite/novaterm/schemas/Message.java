/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.schemas;

import com.codename1.rad.models.Tag;
import com.codename1.rad.schemas.ChatMessage;
import com.codename1.rad.schemas.Comment;
import com.codename1.rad.schemas.Thing;

/**
 *
 * @author shannah
 */
public interface Message {
    public static final Tag subject = Thing.name,
            author = ChatMessage.author,
            attachment = ChatMessage.attachment,
            datePosted = ChatMessage.datePublished,
            numMessages = Comment.commentCount,
            url = Thing.url,
            // Boolean whether it has been read or not
            read = new Tag("read"),
            body = Comment.text,
            attentionTo = new Tag("attention"),
            nextThreadUrl = new Tag("nextThread"),
            prevThreadUrl = new Tag("prevThread"),
            nextMessageUrl = new Tag("nextMessage"),
            prevMessageUrl = new Tag("prevMessage"),
    
            messages = MessageForum.messages,
            forum = new Tag("forum");
    
    public static final Tag[] ALL_TAGS = new Tag[]{subject, author, attachment, datePosted, numMessages, url, read, body, attentionTo, messages, nextThreadUrl, prevThreadUrl, nextMessageUrl, prevMessageUrl, messages, forum};
            
}
