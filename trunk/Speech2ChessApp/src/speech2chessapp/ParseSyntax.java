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

    public enum eAction {GENERIC, FIELD, FIGURES, };

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

    public class ActionFigures extends Action {
        public ActionFigures(String inner) {
            super(inner);
            type = eAction.FIGURES;
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


    public ParseSyntax() {
        createFieldsList();
        createFiguresList();
    }


    private void createFieldsList() {
        for(int i = '1'; i <= '8'; i++) {
            for(int j = 'a'; j <= 'h'; j++) {
                mFields.add(String.format("%c%c", j, i));
            }
        }
    }

    private void createFiguresList() {

         mFigures.add("Bauer".toLowerCase());
         mFigures.add("König".toLowerCase());
         // ...
    }

    public void set(String input)
    {
        mInput = input;
    }

    public boolean check()
    {
        // Als erstes spliten wir den String in einzel wörter

        String[] words = mInput.trim().split(" ");

        for(String word : words) {
            if (mFields.contains(word))
              mActionList.add(new ActionField(word));
            else if (mFigures.contains(word))
              mActionList.add(new ActionFigures(word));
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
