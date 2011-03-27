/*
 * Speech2ChessApp.java
 */

package speech2chessapp;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class Speech2ChessApp extends SingleFrameApplication {

    Controller pController;
    private static boolean sFullscreen = true;


    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        try {
            if(Config.getInstance().get("firststart") != null) {
                //new DreamChessStarter().start();
                //Thread.sleep(6000);
                pController = new Controller();
                if(!sFullscreen)
                    show(new Speech2ChessView(this, pController));
                else
                    pController.cmd(Controller.eCommand.START, null);
                Thread.sleep(3000);
                new DreamChessStarter(sFullscreen).start();
            }
            else {
                Config.getInstance().set("firststart", "false");
                JSAPI jsapi = new JSAPI();
                jsapi.finish();
                System.exit(0);
                //pController = new Controller();
                //pController.cmd(Controller.eCommand.START, null);
                //show(new Speech2ChessView(this, pController));
            }
        } catch (Exception ex) {
            Logger.getLogger(Speech2ChessApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of Speech2ChessApp
     */
    public static Speech2ChessApp getApplication() {
        return Application.getInstance(Speech2ChessApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {

        if(args.length > 0)
            sFullscreen = false;

        launch(Speech2ChessApp.class, args);
    }
}
