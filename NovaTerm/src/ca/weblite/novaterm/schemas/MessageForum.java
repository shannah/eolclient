/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.schemas;

import com.codename1.rad.models.Tag;
import com.codename1.rad.schemas.Thing;

/**
 *
 * @author shannah
 */
public interface MessageForum {
    public static final Tag name = Thing.name;
    public static final Tag messages = new Tag("messages");
    public static final Tag url = Thing.url;
}
