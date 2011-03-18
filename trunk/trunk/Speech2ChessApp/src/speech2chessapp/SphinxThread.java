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
    private static SpeechEngine pSpeechEngine = null;

    public SphinxThread(Controller controller) {
        pController = controller;
        if(pSpeechEngine == null) {
            //pSpeechEngine = new Sphinx();
            pSpeechEngine = new JSAPI();
        }
    }

    @Override
    public void run()
    {
        System.out.println("SphinxThread::run() ->");
        while(true) {
            //pSphinx = new Sphinx();
            List<String> result = pSpeechEngine.record();
            if(result != null) {
                pController.cmd(Controller.eCommand.PARSE_STRINGS_SPHINX, result);
                System.out.println("SphinxThread::run() a");
                //pSphinx.finish();
                System.out.println("SphinxThread::run() b");
                break;
            }
            System.out.println("SphinxThread::run() c");
            //pSphinx.finish();
            //System.out.println("SphinxThread::run() d");
        }
        System.out.println("SphinxThread::run() <-");
    }

}
