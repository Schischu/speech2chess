/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

/**
 *
 * @author i7
 */
public class SphinxThread extends Thread {
    private Controller pController = null;
    private Sphinx pSphinx = null;

    public SphinxThread(Controller controller) {
        pController = controller;
        pSphinx = new Sphinx();
    }

    @Override
    public void run()
    {
        while(true) {
            String result = pSphinx.record();
            if(result != null) {
                pController.cmd(Controller.eCommand.PARSE_STRING, result);
            }
        }
    }

}
