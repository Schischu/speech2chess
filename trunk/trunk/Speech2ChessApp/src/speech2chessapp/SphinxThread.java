/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import java.util.List;

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
            pSphinx = new Sphinx();
            List<String> result = pSphinx.record();
            if(result != null) {
                pController.cmd(Controller.eCommand.PARSE_STRINGS, result);
                //pSphinx.finish();
                break;
            }
            pSphinx.finish();
        }
    }

}
