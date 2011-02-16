/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import java.util.ArrayList;
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
    
    public Controller() {
        // Create Receive From Chess Task

        mReceiveFromChessThread = new ReceiveFromChessThread(this);
        mSphinxThread = new SphinxThread(this);
        mParseSyntax = new ParseSyntax();

    }

    public enum eCommand {
        NEW_PACKAGE_RECEIVED,
        APPEND_LOG,
        PARSE_STRING,
        START,
        GRANT_UI_ACCESS,
    };

    public void cmd(eCommand cmd, Object o) {
        switch(cmd) {
            default:
                break;

            case NEW_PACKAGE_RECEIVED:
            {
                SocketCommand scmd = (SocketCommand)o;
                switch(scmd.type) {
                    default:
                        break;
                    case SocketToChess.RES_VERIFY:
                        if(scmd.data.length > 0) {
                            byte rtv = scmd.data[0];
                            boolean verify = rtv>0?true:false;
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

            case PARSE_STRING:
            {
                String s = (String)o;
                mParseSyntax.set(s);
                boolean b = mParseSyntax.check();
                if(b) {
                    ArrayList<Action> a = mParseSyntax.getActionList();
                    String src = "";
                    String dst = "";
                    for (Action action : a) {
                        if(action.type == eAction.FIELD) {
                            if(src.length() == 0)
                                src = action.innerData;
                            else if(dst.length() == 0)
                                dst = action.innerData;
                        }
                    }

                    if (src.length() > 0 && dst.length() > 0) {
                        cmd(eCommand.APPEND_LOG, src + " -> " + dst);
                        SocketCommand sockcmd = new SocketCommand();
                        sockcmd.type = SocketToChess.REQ_MOVE;
                        sockcmd.data = (src + dst).getBytes();
                        SocketToChess.sendCMD(sockcmd);
                    }
                }
                mParseSyntax.clear();
            }
                break;

            case START:
            {
                mReceiveFromChessThread.start();
                mSphinxThread.start();
            }
            break;

            case GRANT_UI_ACCESS:
            {
                Speech2ChessView s = (Speech2ChessView)o;
                mSpeech2ChessView = s;
            }
                break;
        }
    }
}
