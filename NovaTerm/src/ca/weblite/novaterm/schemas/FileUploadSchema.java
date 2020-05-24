/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.schemas;

import com.codename1.rad.models.Tag;
import com.codename1.rad.schemas.Action;
import com.codename1.rad.schemas.Article;
import com.codename1.rad.schemas.Comment;
import com.codename1.rad.schemas.MediaObject;
import com.codename1.rad.schemas.Thing;

/**
 *
 * @author shannah
 */
public interface FileUploadSchema {
    public static final Tag name = Thing.name,
            subject = new Tag("subject"),
            description = Thing.description,
            attachment = new Tag("attachment"),
            errorMessage = Action.error,
            library = new Tag("library")
            ;
}
