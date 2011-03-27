/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import speech2chessapp.Controller.eCommand;
import speech2chessapp.SocketToChess.SocketCommand;

/**
 *
 * @author brandma31324
 */
public class ReceiveFromChessThread extends Thread {

    private Controller pController = null;

    public ReceiveFromChessThread(Controller controller) {
        pController = controller;
    }

    
    @Override
    public void run()
    {
        while(true) {
            SocketCommand cmd = SocketToChess.receiveCMD();
            if(cmd != null)
                pController.cmd(eCommand.NEW_PACKAGE_RECEIVED, cmd); 
        }
    }
}
