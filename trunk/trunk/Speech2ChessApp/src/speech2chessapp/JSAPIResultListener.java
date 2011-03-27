/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import com.cloudgarden.speech.CGResult;
import java.util.ArrayList;
import java.util.Date;
import javax.speech.recognition.DictationGrammar;
import javax.speech.recognition.FinalDictationResult;
import javax.speech.recognition.FinalResult;
import javax.speech.recognition.FinalRuleResult;
import javax.speech.recognition.Recognizer;
import javax.speech.recognition.Result;
import javax.speech.recognition.ResultAdapter;
import javax.speech.recognition.ResultEvent;
import javax.speech.recognition.ResultStateError;
import javax.speech.recognition.ResultToken;
import javax.speech.recognition.RuleGrammar;
import speech2chessapp.SocketToChess.SocketCommand;

/**
 *
 * @author i7
 */
public class JSAPIResultListener extends ResultAdapter {
    private Recognizer mRec = null;

    public ArrayList<String> sResults = new ArrayList<String>();

    /**
     * Creates a TestResultListener which will deallocate the given Recognizer
     * after "nRecs" accepted recognitions, and will re-play the recorded audio
     * if "playAudio" is true (and audio is being saved using
     * RecognizerProperties.setResultAudioProvided(true))
     */
    public JSAPIResultListener(Recognizer rec) {
        this.mRec = rec;
    }

    @Override
    public void resultRejected(ResultEvent e) {
        Result r = (Result)(e.getSource());
        System.out.println("Result Rejected "+r);
    }
    @Override
    public void resultCreated(ResultEvent e) {
        Result r = (Result)(e.getSource());
        System.out.println("Result Created ");

        SocketCommand sockcmd = new SocketCommand();
        sockcmd.type = SocketToChess.REQ_SPEECHACTIVITY;
        sockcmd.data = (" ").getBytes();
        SocketToChess.sendCMD(sockcmd);
    }
    @Override
    public void resultUpdated(ResultEvent e) {
        Result r = (Result)(e.getSource());
        System.out.println("Result Updated... "+r);
        if(r instanceof CGResult) {
            SocketCommand sockcmd = new SocketCommand();
            sockcmd.type = SocketToChess.REQ_SPEECHACTIVITY;
            sockcmd.data = (" ").getBytes();
            SocketToChess.sendCMD(sockcmd);

            /*ResultToken[] tokens = r.getBestTokens();
            if(tokens != null && tokens.length > 0) {
                displayTimes(tokens[0]);
            }*/
        }
    }

    private void displayTimes(ResultToken token) {
        Date start = new Date(token.getStartTime());
        Date now = new Date(System.currentTimeMillis());
        System.out.println("Result start = "+start.getMinutes()+":"+start.getSeconds()+
        ", length = "+((token.getEndTime() - token.getStartTime())/1000.0)+
        ", now="+now.getMinutes()+":"+now.getSeconds());
    }

    @Override
    public  void resultAccepted(ResultEvent e) {
        final FinalResult r = (FinalResult)(e.getSource());
        Runnable lt = new Runnable() {
            public void run() {
                try {
                    System.out.print("Result Accepted: " + r);
                    sResults.clear();
                    if(r.getGrammar() instanceof RuleGrammar) {
                        FinalRuleResult rr = ((FinalRuleResult)r);
                        for(int i = 0; i < rr.getNumberGuesses(); i++) {
                            ResultToken tokens[] = null;
                            System.out.println("\nRuleGrammar name="+rr.getRuleGrammar(i).getName());
                            System.out.println("Rule name="+rr.getRuleName(i));
                            tokens = rr.getAlternativeTokens(i);

                            String result = "";
                            for(ResultToken token : tokens) {
                                result += token.getSpokenText() + " ";
                            }
                            result = result.trim();

                            sResults.add(result);
                        }
                    } else {
                        ResultToken tokens[] = null;
                        System.out.println("\nGrammar name="+r.getGrammar().getName());
                        tokens = r.getBestTokens();

                        String result = "";
                        for(ResultToken token : tokens) {
                            result += token.getSpokenText() + " ";
                        }
                        result = result.trim();

                        sResults.add(result);
                    }

                    mRec.pause();

                    /*

                    //Test out token correction - here we just get the "nAlt"th alternative
                    //and "correct" the result using it
                    //(except here we've commented out the tokenCorrection call
                    //...include that line if you actually want to update your profile randomly!

                    try {
                        ResultToken[] toks = null;
                        int nAlt = 3;
                        if(r.getGrammar() instanceof DictationGrammar) {

                            //Print out first three alternatives
                            String str = "";
                            FinalDictationResult fdr = (FinalDictationResult)r;
                            ResultToken start = fdr.getBestToken(0);
                            ResultToken end =  fdr.getBestToken(fdr.numTokens()-1);
                            ResultToken[][] tokenArray = fdr.getAlternativeTokens(start,end,3);
                            if(tokenArray != null) {
                                for(int i=0;i<tokenArray.length;i++) {
                                    str+="\nAlternative (engineConf="+((CGResult)fdr).getEngineConfidence(i)+") "+i+" =";
                                    for(int j=0;j<tokenArray[i].length;j++) {
                                        str+=" "+tokenArray[i][j].getSpokenText();
                                    }
                                }
                            }
                            System.out.println(str);

                            tokenArray = fdr.getAlternativeTokens(start, end, 3);
                            if(tokenArray != null && tokenArray.length > nAlt) toks = tokenArray[nAlt];

                        }  else {
                            //Print out first three alternatives
                            String str = "";
                            FinalRuleResult frr = (FinalRuleResult)r;
                            for(int i=0;i<3; i++) {
                                ResultToken[] tokenArray = frr.getAlternativeTokens(i);
                                if(tokenArray != null) {
                                    str+="\nAlternative (engineConf="+((CGResult)frr).getEngineConfidence(i)+") "+i+" =";
                                    for(int j=0;j<tokenArray.length;j++) {
                                        str+=" "+tokenArray[j].getSpokenText();
                                    }
                                }
                            }
                            System.out.println(str);
                            toks = ((FinalRuleResult)r).getAlternativeTokens(nAlt);
                        }
                        if(toks != null) {
                            String[] stoks = new String[toks.length];
                            for(int i=0;i<stoks.length;i++) stoks[i] = toks[i].getSpokenText();
                            System.out.print("Could correct ... '");
                            for(int i=0;i<tokens.length;i++) System.out.print(tokens[i].getSpokenText()+" ");
                            System.out.print("' ...with... '");
                            for(int i=0;i<stoks.length;i++)  System.out.print(stoks[i]+" ");
                            System.out.println("' ...(except the tokenCorrection call is commented out)");
                            //r.tokenCorrection(stoks,tokens[0],tokens[tokens.length-1], 0);

                            // NOTE: for SAPI4 engines, after correction, the alternatives are deleted,
                            // but for SAPI5 engines the alternatives are rearranged to be consistent
                            // with the correction - you can observe this by looking at the output of the
                            // System.out.println("RESULT is "+r); line below

                        }
                    } catch(ResultStateError er) {
                        er.printStackTrace(System.out);
                        //Result is not a DictationResult
                    }
                     */
                } catch(Exception e1) {
                    e1.printStackTrace(System.out);
                } catch(ResultStateError er) {
                    er.printStackTrace(System.out);
                }
            }
        };
        (new Thread(lt)).start();

    }
}
