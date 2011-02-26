/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import speech2chessapp.ParseSyntax.Action;
import speech2chessapp.ParseSyntax.eAction;
import speech2chessapp.SocketToChess.SocketCommand;

/**
 *
 * @author brandma31324
 */
public class Controller {

    private Thread mReceiveFromChessThread = null;
    private Thread mSphinxThread = null;
    private ParseSyntax mParseSyntax = null;
    private Speech2ChessView mSpeech2ChessView = null;

    private List<String> mSaveSpeechResults = null;
    private Integer mSaveSpeechResultsIndex = 0;

    public Controller() {
        // Create Receive From Chess Task

        Common.load("EN");

        mReceiveFromChessThread = new ReceiveFromChessThread(this);
        mSphinxThread = new SphinxThread(this);
        mParseSyntax = new ParseSyntax();

    }


    public enum eCommand {
        NEW_PACKAGE_RECEIVED,
        APPEND_LOG,
        PARSE_STRING,
        PARSE_STRINGS,
        START,
        GRANT_UI_ACCESS,
        TEST_MOVE,
        DO_MOVE,
        RECORD,
    };

    public void cmd(eCommand cmd, Object o) {
        System.out.println("cmd -> " + cmd.toString());
        switch(cmd) {
            default:
                break;

            case NEW_PACKAGE_RECEIVED:
            {
                SocketCommand scmd = (SocketCommand)o;
                switch(scmd.type) {
                    default:
                        break;
                    case SocketToChess.REQ_VERIFY:
                        if(scmd.data.length > 0) {
                            byte rtv = scmd.data[0];
                            boolean verify = rtv>0?true:false;
                            if(verify) {
                                byte data[] = new byte[4];
                                data[0] = scmd.data[1];
                                data[1] = scmd.data[2];
                                data[2] = scmd.data[3];
                                data[3] = scmd.data[4];


                                SocketCommand sockcmd = new SocketCommand();
                                sockcmd.type = SocketToChess.REQ_PRINT;
                                sockcmd.data = (Common.mMove + " (" + 5 + ")").getBytes();
                                SocketToChess.sendCMD(sockcmd);

                                for(int i = 0; i < 5; i++) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    sockcmd.type = SocketToChess.REQ_PRINT;
                                    sockcmd.data = (Common.mMove + " (" + (5-i) + ")").getBytes();
                                    SocketToChess.sendCMD(sockcmd);
                                }

                                sockcmd.type = SocketToChess.REQ_PRINT;
                                sockcmd.data = "".getBytes();
                                SocketToChess.sendCMD(sockcmd);


                                cmd(eCommand.DO_MOVE, new String(data));
                            }
                             else {
                                cmd(eCommand.PARSE_STRING, null);
                             }
                        }
                        break;
                }
            }
                break;
                
            case APPEND_LOG:
            {
                String s = (String)o;
                mSpeech2ChessView.appendLog(s);
            }
                break;

            case TEST_MOVE:
            {
                String s = (String)o;
                SocketCommand sockcmd = new SocketCommand();
                sockcmd.type = SocketToChess.REQ_VERIFY;
                sockcmd.data = s.getBytes();
                SocketToChess.sendCMD(sockcmd);
            }
                break;

            case DO_MOVE:
            {
                String s = (String)o;
                cmd(eCommand.APPEND_LOG, "Move " + s);
                SocketCommand sockcmd = new SocketCommand();
                sockcmd.type = SocketToChess.REQ_MOVE;
                sockcmd.data = s.getBytes();
                SocketToChess.sendCMD(sockcmd);
            }
                break;

            case RECORD:
            {
                mSphinxThread.start();
                SocketCommand sockcmd = new SocketCommand();
                sockcmd.type = SocketToChess.REQ_PRINT;
                sockcmd.data = Common.mSpeakNow.getBytes();
                SocketToChess.sendCMD(sockcmd);
            }
                break;

            case PARSE_STRINGS:
            {
                SocketCommand sockcmd = new SocketCommand();
                sockcmd.type = SocketToChess.REQ_PRINT;
                sockcmd.data = "".getBytes();
                SocketToChess.sendCMD(sockcmd);

                List<String> s = (List<String>)o;

                mSaveSpeechResults = s;
                mSaveSpeechResultsIndex = 0;

                cmd(eCommand.PARSE_STRING, null);

                cmd(eCommand.RECORD, null);
            }
                break;

            case PARSE_STRING:
            {
                if(mSaveSpeechResultsIndex >= mSaveSpeechResults.size()) {
                     //cmd(eCommand.RECORD, null);

                }
                else {
                    String s = mSaveSpeechResults.get(mSaveSpeechResultsIndex++); //(String)o;
                    mParseSyntax.set(s);
                    boolean b = mParseSyntax.check();
                    if(b) {
                        ArrayList<Action> a = mParseSyntax.getActionList();
                        String src = "";
                        String dst = "";
                        eAction type = eAction.GENERIC;
                        for (Action action : a) {
                            type = action.type;
                            if(action.type == eAction.FIELD) {
                                if(src.length() == 0)
                                    src = action.innerData;
                                else if(dst.length() == 0)
                                    dst = action.innerData;
                            }
                             else if(action.type == eAction.COMMAND) {
                                dst = action.innerData;
                             }
                        }

                        if(type == eAction.FIELD ||type == eAction.FIGURES) {
                            if (src.length() > 0 && dst.length() > 0) {
                                /*cmd(eCommand.APPEND_LOG, src + " -> " + dst);

                                SocketCommand sockcmd = new SocketCommand();
                                sockcmd.type = SocketToChess.REQ_MOVE;
                                sockcmd.data = (src + dst).getBytes();
                                SocketToChess.sendCMD(sockcmd);*/
                                cmd(eCommand.TEST_MOVE, (src + dst));
                            }
                        }
                        else if (type == eAction.COMMAND) {
                            if(dst.equals("end")) {
                                cmd(eCommand.APPEND_LOG, "End Game");

                                SocketCommand sockcmd = new SocketCommand();
                                sockcmd.type = SocketToChess.REQ_QUIT;
                                sockcmd.data = dst.getBytes();
                                SocketToChess.sendCMD(sockcmd);
                            } else if(dst.equals("restart")) {
                                cmd(eCommand.APPEND_LOG, "Restart Game");

                                SocketCommand sockcmd = new SocketCommand();
                                sockcmd.type = SocketToChess.REQ_RESTART;
                                sockcmd.data = dst.getBytes();
                                SocketToChess.sendCMD(sockcmd);
                            }
                        }
                    }
                    mParseSyntax.clear();
                }
            }
                break;

            case START:
            {
                mReceiveFromChessThread.start();
                cmd(eCommand.RECORD, null);
            }
            break;

            case GRANT_UI_ACCESS:
            {
                Speech2ChessView s = (Speech2ChessView)o;
                mSpeech2ChessView = s;
            }
                break;
        }
        System.out.println("cmd <- " + cmd.toString());
    }
}
