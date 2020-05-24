/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.schemas;

import com.codename1.rad.models.Tag;
import com.codename1.rad.schemas.Comment;
import com.codename1.rad.schemas.CreativeWork;
import com.codename1.rad.schemas.MediaObject;
import com.codename1.rad.schemas.Thing;

/**
 *
 * @author shannah
 */
public interface FileSchema {
    public static final Tag name = Thing.name;
    public static final Tag date = MediaObject.uploadDate;
    public static final Tag description = CreativeWork.description;
    public static final Tag contentSize = MediaObject.contentSize;
    public static final Tag downloadUrl = MediaObject.contentUrl;
    public static final Tag downloadCount = MediaObject.viewCount;
    public static final Tag postedBy = Comment.author;
    public static final Tag detailsUrl = new Tag();
    public static final Tag kind = new Tag();
}
