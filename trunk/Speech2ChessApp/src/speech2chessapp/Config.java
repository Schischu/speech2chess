/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 *
 * @author i7
 */
public class Config {
    private static Config pConfig = null;

    public static Config getInstance() {
        if (pConfig == null)
            pConfig = new Config();

        return pConfig;
    }

    //--------------------------------------

    private Properties mProperties = null;

    private Config() {
        try {
            mProperties = new Properties();
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream("speech2chess.properties"));
            mProperties.load(stream);
            stream.close();
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }

    public String get(String prop) {
        if (mProperties != null)
            return mProperties.getProperty(prop);
        else
            return null; 
    }

    public void set(String prop, String value) {
        mProperties.setProperty(prop, value);
        try {
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream("speech2chess.properties"));
            mProperties.store(stream, "");
        } catch(Exception ex) {
            System.out.println(ex);
        }
    }
}
