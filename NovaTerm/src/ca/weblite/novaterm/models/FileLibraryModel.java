/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.models;

import ca.weblite.novaterm.schemas.FileLibrary;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityList;
import com.codename1.rad.models.EntityType;

/**
 *
 * @author shannah
 */
public class FileLibraryModel extends Entity implements FileLibrary {
    public static final EntityType TYPE = new EntityType(){{
        string(name);
        string(url);
        list(EntityList.class, files);
        
    }};
    {
        setEntityType(TYPE);
        set(files, new EntityList(FileModel.TYPE, -1));
        setText(name, "");
        setText(url, "");
    }
    
}
