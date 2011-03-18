/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import com.cloudgarden.speech.userinterface.SpeechEngineChooser;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineList;
import javax.speech.recognition.Recognizer;
import javax.speech.recognition.RecognizerModeDesc;

/**
 *
 * @author i7
 */
public class JSAPI extends SpeechEngine {

    private static Recognizer rec = null;

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
                        rec = Central.createRecognizer(desc);
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

        if(rec == null) {
            SpeechEngineChooser chooser = SpeechEngineChooser.getRecognizerDialog();
            chooser.show();
            RecognizerModeDesc desc = chooser.getRecognizerModeDesc();
            if(desc != null) {
                Config.getInstance().set("recognizer", desc.getEngineName());
                try {
                    rec = Central.createRecognizer(desc);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (EngineException ex) {
                    Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(JSAPI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if(rec != null) {
            
        }
        
        
    }

    @Override
    public void finish() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> record() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
