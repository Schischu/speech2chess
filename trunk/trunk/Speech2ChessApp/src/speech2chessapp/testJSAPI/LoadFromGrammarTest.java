/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp.testJSAPI;

import com.cloudgarden.speech.userinterface.*;
import javax.speech.*;
import javax.speech.recognition.*;
import java.io.*;
import java.net.*;

/**
 * Demonstrates loading a grammar file, "grammars.helloWorld", which
 * imports another grammar, "grammars.numbers", and recognition of commands
 * from the helloWorld grammar.
 */
public class LoadFromGrammarTest {
    private static Recognizer rec = null;

    public static void main(String[] args) {
        try {
            // Wir müssen den Nutzer anfangs auf jeden Fall nach nem Profil fragen
            // sonst kann er kein Profil trainieren.
            SpeechEngineChooser chooser = SpeechEngineChooser.getRecognizerDialog();
            chooser.show();
            RecognizerModeDesc desc = chooser.getRecognizerModeDesc();
            rec = Central.createRecognizer(desc);

            rec.addEngineListener(new TestEngineListener());

            // Second parameter of TestResultListener determines number of
            // recognitions before deallocation.
            rec.addResultListener(new TestResultListener(rec,100,true));

            RecognizerAudioAdapter raud = new TestAudioListener();
            rec.getAudioManager().addAudioListener(raud);

            rec.allocate();

            rec.waitEngineState(Recognizer.ALLOCATED);
            RecognizerProperties props = rec.getRecognizerProperties();
            props.setNumResultAlternatives(5);
            props.setResultAudioProvided(true);

            URL dir=new File("").toURL(); //The recognizer will look
            //for the grammar files starting from the examples directory

            RuleGrammar gram = rec.loadJSGF(dir,"speech2chessapp.speech2chess_DE", true,true,null);
            String[] names = gram.listRuleNames();
            for(int i=0;i<names.length; i++) {
                Rule rule = gram.getRule(names[i]);
                System.out.println("<"+names[i]+">="+rule);
            }

            gram.setEnabled(true);

            rec.commitChanges();
            rec.waitEngineState(rec.LISTENING);
            System.out.println("\nUsing engine "+rec.getEngineModeDesc());

            rec.requestFocus();
            rec.resume();

            rec.waitEngineState(Recognizer.DEALLOCATED);
            //TestResultListener deallocates after three recognitions

        } catch(Exception e) {
            e.printStackTrace(System.out);
        } catch(Error e1) {
            e1.printStackTrace(System.out);
        } finally {
            try {
                rec.deallocate();
            } catch(Exception e2) {
                e2.printStackTrace(System.out);
            }
            System.exit(0);
        }
    }
}