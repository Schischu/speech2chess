/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author i3
 */
public class DreamChessStarter extends Thread {
    @Override
    public void run()
    {
        try {
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            //Process p = Runtime.getRuntime().exec("dreamchess.exe --fullscreen --width " + dim.width + " --height " + dim.height);
            Process p = Runtime.getRuntime().exec("C:\\Program Files\\DreamChess\\dreamchess.exe --fullscreen --width " + dim.width + " --height " + dim.height);
            //Process p = Runtime.getRuntime().exec("C:\\Program Files\\DreamChess\\dreamchess.exe");

            p.waitFor();
            System.exit(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(DreamChessStarter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DreamChessStarter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
