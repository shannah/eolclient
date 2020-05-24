/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.models;

import ca.weblite.novaterm.schemas.FileSchema;
import com.codename1.rad.models.Entity;
import com.codename1.rad.models.EntityType;


/**
 *
 * @author shannah
 */
public class FileModel extends Entity implements FileSchema {
    public static final EntityType TYPE = new EntityType(){{
        string(name);
        Integer(contentSize);
        string(description);
        string(date);
        string(downloadUrl)/*
                .setter((entity, value, defaultSetter)->{
                    if (value != null) {
                        if (!value.startsWith("/") && !value.startsWith("http:")) {
                            value = "/file/"+value;
                        }
                    }
                    defaultSetter.setValue(entity, value, null);
                })*/;
        string(postedBy);
        string(detailsUrl);
    }};
    {
        setEntityType(TYPE);
    }
}
