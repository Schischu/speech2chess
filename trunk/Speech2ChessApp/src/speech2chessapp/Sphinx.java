/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;

/**
 *
 * @author i7
 */
public class Sphinx {
    private ConfigurationManager mCm;
    private Recognizer mRecognizer;
    private Microphone mMicrophone;

    public Sphinx() {
        mCm = new ConfigurationManager(Speech2ChessApp.class.getResource("speech2chess.config.xml"));
        mRecognizer = (Recognizer) mCm.lookup("recognizer");
        mRecognizer.allocate();

        mMicrophone = (Microphone) mCm.lookup("microphone");
    }

    public void finish() {
        mRecognizer.deallocate();
    }

    public boolean record() {
        if (!mMicrophone.startRecording()) {
            System.out.println("Cannot start microphone.");
            return false;
        }

        return true;
    }
}
