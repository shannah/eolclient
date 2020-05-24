/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.models;

import ca.weblite.novaterm.schemas.FileUploadSchema;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityType;

/**
 *
 * @author shannah
 */
public class FileUploadModel extends Entity implements FileUploadSchema {
    public static final EntityType TYPE = new EntityType(){{
        string(name);
        string(attachment);
        string(subject);
        string(description);
        string(errorMessage);
        entity(FileLibraryModel.class, library);
    }};
    {
        setEntityType(TYPE);
    }
}
