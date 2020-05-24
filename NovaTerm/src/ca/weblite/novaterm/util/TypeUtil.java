/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.novaterm.util;

/**
 *
 * @author shannah
 */
public class TypeUtil {
    public static int toInt(Integer i, int defaultVal) {
        if (i == null) {
            return defaultVal;
        }
        return i;
    }
    
    public static int toInt(Integer i) {
        return toInt(i, 0);
    }
}
