/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.schemas;

import com.codename1.rad.models.Tag;

/**
 *
 * @author shannah
 */
public interface LoginAction {
    public static final Tag username = new Tag(),
            password = new Tag(),
            serverAddress = new Tag(),
            errorMessage = new Tag();
}
