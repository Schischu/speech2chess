/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import com.cloudgarden.speech.userinterface.SpeechEngineChooser;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineList;
import javax.speech.EngineStateError;
import javax.speech.recognition.GrammarException;
import javax.speech.recognition.Recognizer;
import javax.speech.recognition.RecognizerModeDesc;
import javax.speech.recognition.RecognizerProperties;
import javax.speech.recognition.Rule;
import javax.speech.recognition.RuleGrammar;

/**
 *
 * @author i7
 */
public class JSAPI extends SpeechEngine {

    private static Recognizer mRec = null;
    private static JSAPIResultListener mJSAPIResultListener = null;

    public JSAPI() {
        //"Microsoft Speech Recognizer 8.0 for Windows (English - UK), SAPI5, Microsoft"
        String recognizer = Config.getInstance().get("recognizer");

        if(recognizer != null) {
            RecognizerModeDesc required = new RecognizerModeDesc();
            EngineList list = Central.availableRecognizers(required);
            for(Object o : list) {
                RecognizerModeDesc desc = (RecognizerModeDesc)o;
                if(desc.getEngineName().equals(recognizer)) {
                    try {
                        mRec = Central.createRecognizer(desc);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (EngineException ex) {
                        Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
                        Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                }
            }
        }

        if(mRec == null) {
            SpeechEngineChooser chooser = SpeechEngineChooser.getRecognizerDialog();
            chooser.show();
            RecognizerModeDesc desc = chooser.getRecognizerModeDesc();
            if(desc != null) {
                Config.getInstance().set("recognizer", desc.getEngineName());
                try {
                    mRec = Central.createRecognizer(desc);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (EngineException ex) {
                    Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if(mRec != null) {
            try {
                //Do we need this?
                //rec.addEngineListener(new TestEngineListener());
                mJSAPIResultListener = new JSAPIResultListener(mRec);
                mRec.addResultListener(mJSAPIResultListener);

                //Do we need this?
                //RecognizerAudioAdapter raud = new TestAudioListener();
                //rec.getAudioManager().addAudioListener(raud);
                mRec.allocate();

                mRec.waitEngineState(Recognizer.ALLOCATED);
                RecognizerProperties props = mRec.getRecognizerProperties();
                props.setNumResultAlternatives(5);
                props.setResultAudioProvided(true);

                URL dir=new File("").toURL(); //The recognizer will look
                //for the grammar files starting from the examples directory

                RuleGrammar gram = mRec.loadJSGF(dir,"speech2chessapp.speech2chess_" + Common.mLanguage, true,true,null);
                String[] names = gram.listRuleNames();
                for(int i=0;i<names.length; i++) {
                    Rule rule = gram.getRule(names[i]);
                    System.out.println("<"+names[i]+">="+rule);
                }

                gram.setEnabled(true);

                mRec.commitChanges();

                mRec.pause();
            } catch (GrammarException ex) {
                Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EngineException ex) {
                Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (EngineStateError ex) {
                Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("\nUsing engine "+mRec.getEngineModeDesc());
    }

    @Override
    public void finish() {
        if( mRec.testEngineState(Recognizer.ALLOCATED)
                && !mRec.testEngineState(Recognizer.DEALLOCATING_RESOURCES)) {
                    try {
                        System.out.println("forcing finalize");
                        mRec.forceFinalize(true);
                        System.out.println("deallocating");
                        mRec.deallocate();
                    } catch(Exception e2) {
                        e2.printStackTrace(System.out);
                    }
                }
    }

    @Override
    public List<String> record() {
        try {
            mRec.requestFocus();
            mRec.resume();
            mRec.waitEngineState(mRec.LISTENING);

            // Will be paused if the recognizerlistener has something recognized
            mRec.waitEngineState(mRec.PAUSED);

            return mJSAPIResultListener.sResults;

        } catch (AudioException ex) {
            Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EngineStateError ex) {
            Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
