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
import speech2chessapp.SocketToChess.SocketCommand;

/**
 * Handels the Sphinx
 * @author i7
 */
public class Sphinx extends SpeechEngine {
    private ConfigurationManager mCm;
    private Recognizer mRecognizer;
    private Microphone mMicrophone;

    /**
     * Initialises JSAPI
     */
    public Sphinx() {
        try {
            mCm = new ConfigurationManager(Speech2ChessApp.class.getResource("speech2chess_" + Common.mLanguage + ".config.flat.xml"));
            //mCm = new ConfigurationManager(Speech2ChessApp.class.getResource("speech2chess_" + Common.mLanguage + ".config.xml"));
            mRecognizer = (Recognizer) mCm.lookup("recognizer");
        
            mRecognizer.allocate();
        } catch(java.lang.RuntimeException ex) {

            System.out.println(ex.toString());
            java.io.IOException cause = (java.io.IOException)ex.getCause();
            System.out.println(cause);
            edu.cmu.sphinx.jsgf.JSGFGrammarParseException ex2 = (edu.cmu.sphinx.jsgf.JSGFGrammarParseException)cause.getCause();

            System.out.println(ex2.charNumber);
            System.out.println(ex2.details);
            System.out.println(ex2.lineNumber);
            System.out.println(ex2.message);

            System.exit(0);
        }

        mMicrophone = (Microphone) mCm.lookup("microphone");
    }

    /**
     * Is called before exiting the app
     */
    public void finish() {
        mRecognizer.deallocate();
    }

    /**
     * Will block till a string has been recorded
     * @return
     */
    public List<String> record(){
        ArrayList<String> resultText = new ArrayList();

         
        mMicrophone.clear();
        if (!mMicrophone.startRecording()) {
            System.out.println("Cannot start microphone.");
            return null;
        }

        System.out.println("Speak now:\n");
        SocketCommand sockcmd = new SocketCommand();
        sockcmd.type = SocketToChess.REQ_PRINT;
        sockcmd.data = Common.mSpeakNow.getBytes();
        SocketToChess.sendCMD(sockcmd);


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
