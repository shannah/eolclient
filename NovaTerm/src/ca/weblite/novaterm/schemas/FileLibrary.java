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
public interface FileLibrary {
    public static final Tag files = new Tag(),
            name = Thing.name,
            url = new Tag("url");
}
