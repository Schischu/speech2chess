/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import java.lang.String;
import java.util.ArrayList;

/**
 *
 * @author brandma31324
 */
public class ParseSyntax {

    private ArrayList<Action> mActionList = new ArrayList<Action>();

    public enum eAction {GENERIC, FIELD, FIGURES, COMMAND, };

    public class Action {

        public Action(String inner) {
            innerData = inner;
        }
        public eAction type = eAction.GENERIC;
        public String innerData;
    }

    public class ActionField extends Action {
        public ActionField(String inner) {
            super(inner);
            type = eAction.FIELD;
        }
    }

    public class ActionFigure extends Action {
        public ActionFigure(String inner) {
            super(inner);
            type = eAction.FIGURES;
        }
    }

     public class ActionCommand extends Action {
        public ActionCommand(String inner) {
            super(inner);
            type = eAction.COMMAND;
        }
    }

    private String mInput = "";

    // So ne art liste welche elemente in welcher reiehnfolge kommen
    // z.b. erstes element z.b. a3
    // zu
    // a4
    // in diesen beispiel ist uns also nur wichtig das wir 2 felder haben,
    // wobei das erste der ursprung ist das 2te das ziel
    // d.h. wir haben listen von eingaben, sprich addrese

    //String[] mFields = {"a1", "a2", ...};

    ArrayList<String> mFields = new ArrayList<String>();
    ArrayList<String> mFigures = new ArrayList<String>();
    ArrayList<String> mCommands = new ArrayList<String>();


    public ParseSyntax() {
        createFieldsList();
        createFiguresList();
        createCommandsList();
    }


    private void createFieldsList() {
        for(int i = '1'; i <= '8'; i++) {
            for(int j = 'a'; j <= 'h'; j++) {
                mFields.add(String.format("%c%c", j, i));
            }
        }
    }

    private void createFiguresList() {

         mFigures.add(Common.mPawn.toLowerCase());
         mFigures.add(Common.mKnight.toLowerCase());
         mFigures.add(Common.mBishop.toLowerCase());
         mFigures.add(Common.mRook.toLowerCase());
         mFigures.add(Common.mQueen.toLowerCase());
         mFigures.add(Common.mKing.toLowerCase());
         // ...
    }

    private void createCommandsList() {
        if(Common.mLanguage.equals("EN")) {
         mCommands.add("end".toLowerCase());
         mCommands.add("restart".toLowerCase());
         mCommands.add("yes".toLowerCase());
         mCommands.add("no".toLowerCase());
        } else {
         mCommands.add("beenden".toLowerCase());
         mCommands.add("nocheinmal".toLowerCase());
         mCommands.add("schließen".toLowerCase());
         mCommands.add("ende".toLowerCase());
         mCommands.add("ja".toLowerCase());
         mCommands.add("nein".toLowerCase());
        }
         // ...
    }

    public void set(String input)
    {
        mInput = input;

        mInput = mInput.replaceAll(" " + Common.mOne.toLowerCase(), "1");
        mInput = mInput.replaceAll(" " + Common.mTwo.toLowerCase(), "2");
        mInput = mInput.replaceAll(" " + Common.mThree.toLowerCase(), "3");
        mInput = mInput.replaceAll(" " + Common.mFour.toLowerCase(), "4");
        mInput = mInput.replaceAll(" " + Common.mFive.toLowerCase(), "5");
        mInput = mInput.replaceAll(" " + Common.mSix.toLowerCase(), "6");
        mInput = mInput.replaceAll(" " + Common.mSeven.toLowerCase(), "7");
        mInput = mInput.replaceAll(" " + Common.mEight.toLowerCase(), "8");
    }

    public boolean check()
    {
        // Als erstes spliten wir den String in einzel wörter

        String[] words = mInput.trim().split(" ");

        //System.out.println("check() " + words);

        for(String word : words) {
            if (mCommands.contains(word))
              mActionList.add(new ActionCommand(word));
            else if (mFields.contains(word))
              mActionList.add(new ActionField(word));
            else if (mFigures.contains(word))
              mActionList.add(new ActionFigure(word));
        }

        return true;
    }


    public ArrayList<Action> getActionList() {

        return mActionList;
    }

    public void clear() {
        mActionList.clear();
    }
}
