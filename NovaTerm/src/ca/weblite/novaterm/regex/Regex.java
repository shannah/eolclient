/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.regex;

import com.codename1.util.regex.RE;

/**
 *
 * @author shannah
 */
public class Regex {
    public static String regex_replace(String pattern, String replacement, String subject) {
        RE regex = new RE(pattern);
        return regex.subst(subject, replacement, RE.REPLACE_BACKREFERENCES);
        
    }
}
