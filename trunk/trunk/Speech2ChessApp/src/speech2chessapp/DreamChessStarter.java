/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author i3
 */
public class DreamChessStarter extends Thread {

    private boolean mFullscreen = false;

    public DreamChessStarter(boolean fullscreen) {
        mFullscreen = fullscreen;
    }
    @Override
    public void run() 
    {
        try {
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            Process p = null;
            if(mFullscreen)
                p = Runtime.getRuntime().exec("dreamchess.exe --fullscreen --width " + dim.width + " --height " + dim.height);
            else
                p = Runtime.getRuntime().exec("dreamchess.exe --window --width 640 --height 480");
            //Process p = Runtime.getRuntime().exec("C:\\Program Files (x86)\\DreamChess\\dreamchess.exe");
            //Process p = Runtime.getRuntime().exec("C:\\Program Files\\DreamChess\\dreamchess.exe");

            p.waitFor();
            System.exit(0);
        } catch (Exception ex) {
            Logger.getLogger(DreamChessStarter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
