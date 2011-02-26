/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import edu.cmu.sphinx.decoder.search.Token;
import edu.cmu.sphinx.frontend.util.Microphone;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public List<String> record(){
        ArrayList<String> resultText = new ArrayList();

         System.out.println("Speak now:\n");

        if (!mMicrophone.startRecording()) {
            System.out.println("Cannot start microphone.");
            return null;
        }

        while (mMicrophone.isRecording()) {
            Result result = mRecognizer.recognize();
            if (result != null) {
                resultText.add(result.getBestFinalResultNoFiller());
                List<Token> tokens = result.getResultTokens();
                for(Token t : tokens) {
                    resultText.add(t.getWordPathNoFiller());
                }


                System.out.println("You said: " + resultText + '\n');
                break;
            }
        }
        mMicrophone.stopRecording();
        return resultText;
    }
}
