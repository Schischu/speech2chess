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
    private Thread mWorker = null;

    private Thread mCountdownThread = null;

    private ParseSyntax mParseSyntax = null;
    private Speech2ChessView mSpeech2ChessView = null;

    private List<String> mSaveSpeechResults = null;
    private Integer mSaveSpeechResultsIndex = 0;

    public Controller() {
        // Create Receive From Chess Task

        String language = Config.getInstance().get("language");
        if(language == null) {
            language = "DE";
            Config.getInstance().set("language", language);
        }
        Common.load(language);

        mWorker = new Worker();
        mWorker.start();

        mCountdownThread = new CountdownThread();
        mCountdownThread.start();

        mReceiveFromChessThread = new ReceiveFromChessThread(this);
        //mSphinxThread = new SphinxThread(this);
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
        TEST_MOVES,
        PARSE_STRINGS_SPHINX,
    };

    class eCommandWithData {
        public eCommand cmd;
        public Object o;
        public eCommandWithData(eCommand cmd, Object o) {
            this.cmd = cmd;
            this.o = o;
        }
    }

    ArrayList<eCommandWithData> mCommandMessageQueue = new  ArrayList<eCommandWithData>();

    public void cmd(eCommand cmd, Object o) {
        mCommandMessageQueue.add(new eCommandWithData(cmd, o));
    }

    class Worker extends Thread {
        @Override
        public void run() {
            while(true) {
                while(mCommandMessageQueue.size() > 0) {
                    eCommandWithData cmdWData = mCommandMessageQueue.remove(0);
                    __cmd(cmdWData.cmd, cmdWData.o);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
    }

    private boolean mWaitForYesEndGame = false;
    private boolean mWaitForYesRestartGame = false;

    class eMove {
        public String src;
        public String dst;
        public boolean valid;
        public eMove(String src, String dst) {
            this.src = src;
            this.dst = dst;
        }
    }

    private int mPossibleMovesIndex = 0;
    private boolean mPossibleMovesWalkSrc = true;
    ArrayList<eMove> mPossibleMoves = new  ArrayList<eMove>();
    public void addPossibleMove(String src, String dst) {
        System.out.println("addPossibleMove -> " + src + " " + dst);

        if (src.equals(dst))
            return;

        boolean alreadyIn = false;

        for(eMove m : mPossibleMoves)
            if(m.src.equals(src) && m.dst.equals(dst))
                alreadyIn = true;

       if(!alreadyIn)
            mPossibleMoves.add(new eMove(src, dst));
    }

    /* 1. umwandeln aller möglichen eingaben in züge
     * 2. durchwandern der züge und umwandeln von figuren in felder
     * 3. jetzt die züge nacheinander durchtesten (zukünftig alle durchtesten)
     * 4. sobald einer valid den ausführen
     *
     */

    // Wird aufgerufen bis er bei allen zügen figuren turch felder ausgetauscht hat
    public boolean walkThroughPossibleMove() {
        System.out.println("walkThroughPossibleMove -> " + mPossibleMovesIndex + " " + mPossibleMovesWalkSrc);
        int i = 0;
        boolean canceled = false;
        for(i = mPossibleMovesIndex; i < mPossibleMoves.size(); ) {
            int id = Common.strToFigureId(mPossibleMovesWalkSrc?mPossibleMoves.get(i).src:mPossibleMoves.get(i).dst, mPossibleMovesWalkSrc);
            if(id >= 0) {
                System.out.println("walkThroughPossibleMove " + id + " (" + mPossibleMoves.get(i).src + " " + mPossibleMoves.get(i).dst + ")");
                SocketCommand sockcmd = new SocketCommand();
                sockcmd.type = SocketToChess.REQ_FIGURES;
                sockcmd.data = new byte[1];
                sockcmd.data[0] = (byte) (id & 0xff);
                System.out.println("REQ_FIGURES: " + id);
                SocketToChess.sendCMD(sockcmd);
                i++;
                canceled = true;
                break;
            }
            i++;
        }
        mPossibleMovesIndex = i;
        if(mPossibleMovesWalkSrc && mPossibleMovesIndex >= mPossibleMoves.size()) {
            // ONLY WHITE CHECKED
            mPossibleMovesWalkSrc = false;
            mPossibleMovesIndex = 0;
        } else if(!mPossibleMovesWalkSrc && mPossibleMovesIndex >= mPossibleMoves.size()) {
            // ALL CHECKED
            mPossibleMovesIndex = 0;
            mPossibleMovesWalkSrc = true;
            
            cmd(eCommand.TEST_MOVES, null);
            System.out.println("walkThroughPossibleMove <-");
            return true;
        }

        if(!canceled && !mPossibleMovesWalkSrc)
            walkThroughPossibleMove();

        System.out.println("walkThroughPossibleMove <-");
        return false;
    }

    /*public boolean calculatePossibleMoves(Action a) {
        int id = -1;
        id = a.
        if
        Common.strToFigureId("pawn", true);
        SocketCommand sockcmd = new SocketCommand();
        sockcmd.type = SocketToChess.REQ_FIGURES;
        sockcmd.data = new byte[1];
        sockcmd.data[0] = (byte) (id & 0xff);
        SocketToChess.sendCMD(sockcmd);
    }*/

    public String mDOMOVE = null;

    class CountdownThread extends Thread {
        @Override
        public void run() {
            while(true) {
                if(mDOMOVE == null) {
                    try {
                            Thread.sleep(10);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                        }
                } else {
                    String s = mDOMOVE;
                    cmd(eCommand.APPEND_LOG, "Move " + s);
                    SocketCommand sockcmd = new SocketCommand();
                                        sockcmd.type = SocketToChess.REQ_PRINT2;
                                        sockcmd.data = (Common.mMove + " " + s + " " + Common.mCorrect_Abort + " (" + 5 + ")").getBytes();
                                        SocketToChess.sendCMD(sockcmd);
                    boolean cancel = false;
                    for(int i = 0; i < 50; i++) {
                        if(mDOMOVE == null) {
                            cancel = true;
                            System.out.println("\tabort: " + cancel);
                            break;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if(mDOMOVE == null) {
                            cancel = true;
                            System.out.println("\tabort: " + cancel);
                            break;
                        }
                        if(i%10 == 0) {
                            sockcmd.type = SocketToChess.REQ_PRINT2;
                            sockcmd.data = (Common.mMove + " " + s + " " + Common.mCorrect_Abort + " (" + (4-(i/10)) + ")").getBytes();
                            SocketToChess.sendCMD(sockcmd);
                        }
                    }

                    sockcmd.type = SocketToChess.REQ_PRINT2;
                    sockcmd.data = "".getBytes();
                    SocketToChess.sendCMD(sockcmd);

                    mPossibleMoves.clear();
                    mPossibleMovesIndex = 0;
                    System.out.println("\tcancel: " + cancel);
                    if(!cancel)
                        cmd(eCommand.DO_MOVE, s);
                    mDOMOVE = null;
                }
            }
        }
    }


    public void __cmd(eCommand cmd, Object o) {
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
                    case SocketToChess.REQ_FIGURES:
                        System.out.println("cmd -> " + cmd.toString());
                        int figureId = scmd.data[0];
                        int index = mPossibleMovesIndex;
                        if(index > 0)
                            index--;
                        for(int i = 1; i < scmd.data.length; i++) {
                            System.out.print(scmd.data[i] + " ");
                            if(Common.isWhite(figureId))
                                addPossibleMove(Common.FieldIdToStr(scmd.data[i]), mPossibleMoves.get(index).dst);
                            else
                                addPossibleMove(mPossibleMoves.get(index).src, Common.FieldIdToStr(scmd.data[i]));
                        }
                        walkThroughPossibleMove();

                        break;
                    case SocketToChess.REQ_VERIFY:
                        if(scmd.data.length > 0) {
                            byte rtv = scmd.data[0];
                            boolean verify = rtv>0?true:false;
                            System.out.println("\tREQ_VERIFY: " + verify);
                            if(verify) {
                                byte data[] = new byte[4];
                                data[0] = scmd.data[1];
                                data[1] = scmd.data[2];
                                data[2] = scmd.data[3];
                                data[3] = scmd.data[4];

                                String s = new String(data);

                                SocketCommand sockcmd = new SocketCommand();
                                sockcmd.type = SocketToChess.REQ_PRINT2;
                                sockcmd.data = (Common.mMove + " " + s + " " + Common.mCorrect_YESNO + " (" + 5 + ")").getBytes();
                                SocketToChess.sendCMD(sockcmd);

                                mDOMOVE = s;

                                /*for(int i = 0; i < 5; i++) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    sockcmd.type = SocketToChess.REQ_PRINT2;
                                    sockcmd.data = (Common.mMove + " " + s + " Correct? Say Yes / No (" + (4-i) + ")").getBytes();
                                    SocketToChess.sendCMD(sockcmd);
                                }

                                sockcmd.type = SocketToChess.REQ_PRINT2;
                                sockcmd.data = "".getBytes();
                                SocketToChess.sendCMD(sockcmd);

                                mPossibleMoves.clear();
                                mPossibleMovesIndex = 0;
                                cmd(eCommand.DO_MOVE, s);*/
                            }
                            else {
                               cmd(eCommand.TEST_MOVES, null);
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

            case TEST_MOVES:
            {
                if(mPossibleMovesIndex >= mPossibleMoves.size()) {
                    mPossibleMoves.clear();
                    mPossibleMovesIndex = 0;
                    cmd(eCommand.RECORD, null);

                } else {
                    eMove m = mPossibleMoves.get(mPossibleMovesIndex);
                    //System.out.println("\tmove: " + m.src + "->" + m.dst);
                    if((Common.strToFigureId(m.src, true) < 0) && (Common.strToFigureId(m.dst, true) < 0)) {
                        // Möglicher Zug
                        mPossibleMovesIndex++;
                        cmd(eCommand.TEST_MOVE, (m.src + m.dst));
                    } else {
                        mPossibleMovesIndex++;
                        cmd(eCommand.TEST_MOVES, null);
                    }
                }
            }
                break;

            case TEST_MOVE:
            {
                String s = (String)o;
                System.out.println("\tmove: " + s);
                SocketCommand sockcmd = new SocketCommand();
                sockcmd.type = SocketToChess.REQ_VERIFY;
                sockcmd.data = s.getBytes();
                SocketToChess.sendCMD(sockcmd);
            }
                break;

            case DO_MOVE:
            {
                String s = (String)o;
                SocketCommand sockcmd = new SocketCommand();
                sockcmd.type = SocketToChess.REQ_MOVE;
                sockcmd.data = s.getBytes();
                SocketToChess.sendCMD(sockcmd);
            }
                break;

            case RECORD:
            {
                if(mSphinxThread == null || !mSphinxThread.isAlive()) {
                    //System.out.println(mSphinxThread.getState());
                    mSphinxThread = new SphinxThread(this);
                    mSphinxThread.start();
                } else {
                    // Actually this is printed by sphinx but if we entered a string than sphinx is not run again
                    SocketCommand sockcmd = new SocketCommand();
                    sockcmd.type = SocketToChess.REQ_PRINT;
                    sockcmd.data = Common.mSpeakNow.getBytes();
                    SocketToChess.sendCMD(sockcmd);
                }
            }
                break;

            case PARSE_STRINGS_SPHINX:
                mSphinxThread = null;

            case PARSE_STRINGS:
            {
                SocketCommand sockcmd = new SocketCommand();
                sockcmd.type = SocketToChess.REQ_PRINT;
                sockcmd.data = "".getBytes();
                SocketToChess.sendCMD(sockcmd);

                List<String> s = (List<String>)o;

                String result = "";
                for(String str : s)
                    result += str + ", ";
                cmd(eCommand.APPEND_LOG, "[ " + result + " ]");

                mSaveSpeechResults = s;
                mSaveSpeechResultsIndex = 0;

                cmd(eCommand.PARSE_STRING, null);

                //cmd(eCommand.RECORD, null);
            }
                break;

            case PARSE_STRING:
            {
                if(mSaveSpeechResultsIndex >= mSaveSpeechResults.size()) {
                     walkThroughPossibleMove();
                     cmd(eCommand.RECORD, null);

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

                        type = eAction.FIELD;
                        
                        if(a.size() == 1) {
                            type = a.get(0).type;
                            dst = a.get(0).innerData;
                        }
                        else if(a.size() == 2) {
                            src = a.get(0).innerData;
                            dst = a.get(1).innerData;
                        }
                        else if (a.size() == 3) {
                            if(a.get(0).type == eAction.FIELD) { // 2 and 3 are dest
                                src = a.get(0).innerData; // FIELD
                                // FIGURE
                                dst = a.get(2).innerData; // FIELD
                            } else if(a.get(1).type == eAction.FIELD) { // 2 and 3 are dest
                                // FIGURE
                                src = a.get(1).innerData; // FIELD
                                dst = a.get(2).innerData; // FIGURE OR FIELD
                            } else if(a.get(2).type == eAction.FIELD) { // 2 and 3 are dest
                                src = a.get(0).innerData; // FIGURE
                                // FIGURE
                                dst = a.get(2).innerData; // FIELD
                            }
                        }
                        else if(a.size() == 4) {
                            src = a.get(1).innerData;
                            dst = a.get(3).innerData;
                        }

                        if(type == eAction.FIELD ||type == eAction.FIGURES) {
                            if (src.length() > 0 && dst.length() > 0) {
                                //cmd(eCommand.TEST_MOVE, (src + dst));
                                addPossibleMove(src, dst);
                            }
                        }
                        else if (type == eAction.COMMAND) {
                            if(Common.mLanguage.equals("DE")) {
                                if(dst.equals("beenden"))
                                    dst = "end";
                                if(dst.equals("neustarten"))
                                    dst = "restart";
                                if(dst.equals("ja"))
                                    dst = "yes";
                                if(dst.equals("nein"))
                                    dst = "no";
                            }


                            if(dst.equals("end")) {
                                cmd(eCommand.APPEND_LOG, Common.mEndGame);
                                mWaitForYesEndGame = true;

                                SocketCommand sockcmd = new SocketCommand();
                                sockcmd.type = SocketToChess.REQ_PRINT2;
                                sockcmd.data = (Common.mEndGame).getBytes();
                                SocketToChess.sendCMD(sockcmd);
                            } else if(dst.equals("restart")) {
                                cmd(eCommand.APPEND_LOG, Common.mRestartGame);
                                mWaitForYesRestartGame = true;

                                SocketCommand sockcmd = new SocketCommand();
                                sockcmd.type = SocketToChess.REQ_PRINT2;
                                sockcmd.data = (Common.mRestartGame).getBytes();
                                SocketToChess.sendCMD(sockcmd);
                            } else if(dst.equals("yes")) {
                                cmd(eCommand.APPEND_LOG, Common.mYes);
                                SocketCommand sockcmd = new SocketCommand();
                                if (mWaitForYesEndGame) {
                                    mWaitForYesEndGame = false;
                                    sockcmd.type = SocketToChess.REQ_QUIT;
                                    sockcmd.data = dst.getBytes();
                                    SocketToChess.sendCMD(sockcmd);

                                    sockcmd.type = SocketToChess.REQ_PRINT2;
                                    sockcmd.data = (" ").getBytes();
                                    SocketToChess.sendCMD(sockcmd);
                                } else if  (mWaitForYesRestartGame) {
                                    mWaitForYesRestartGame = false;
                                    sockcmd.type = SocketToChess.REQ_RESTART;
                                    sockcmd.data = dst.getBytes();
                                    SocketToChess.sendCMD(sockcmd);
                                    
                                    sockcmd.type = SocketToChess.REQ_PRINT2;
                                    sockcmd.data = (" ").getBytes();
                                    SocketToChess.sendCMD(sockcmd);
                                }
                                
                            } else if(dst.equals("no")) {
                                cmd(eCommand.APPEND_LOG, Common.mNo);

                                if (mDOMOVE != null) {
                                    System.out.println("\tDO abort: " + mDOMOVE);
                                    mDOMOVE = null;
                                    mPossibleMoves.clear();
                                    mPossibleMovesIndex = 0;
                                } else {
                                    mWaitForYesEndGame = false;
                                    mWaitForYesRestartGame = false;

                                    SocketCommand sockcmd = new SocketCommand();
                                    sockcmd.type = SocketToChess.REQ_PRINT2;
                                    sockcmd.data = (" ").getBytes();
                                    SocketToChess.sendCMD(sockcmd);
                                }
                            }
                            //cmd(eCommand.RECORD, null);
                        }
                    }
                    mParseSyntax.clear();

                    /*if(mSaveSpeechResultsIndex >= mSaveSpeechResults.size()) {
                        // Starte den Lauf vorgang
                        walkThroughPossibleMove();
                    }*/
                    cmd(eCommand.PARSE_STRING, null);
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
