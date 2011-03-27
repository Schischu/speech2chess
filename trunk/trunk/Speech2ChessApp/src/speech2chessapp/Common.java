/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;


/**
 *
 * @author i7
 */
public class Common {

    public static String mLanguage = "EN";

    private static final String mPawn_EN = "pawn";
    private static final String mKnight_EN = "knight";
    private static final String mBishop_EN = "bishop";
    private static final String mRook_EN = "rook";
    private static final String mQueen_EN = "queen";
    private static final String mKing_EN = "king";

    private static final String mOne_EN = "one";
    private static final String mTwo_EN = "two";
    private static final String mThree_EN = "three";
    private static final String mFour_EN = "four";
    private static final String mFive_EN = "five";
    private static final String mSix_EN = "six";
    private static final String mSeven_EN = "seven";
    private static final String mEight_EN = "eight";

    private static final String mSpeakNow_EN = "Speak Now";
    private static final String mMove_EN = "Move";

    private static final String mCorrect_Abort_EN = "Correct? Say No to abort";
    private static final String mCorrect_YESNO_EN = "Correct? Say Yes / No";
    private static final String mEndGame_EN = "End Game? Say Yes / No";
    private static final String mRestartGame_EN = "Restart Game? Say Yes / No";
    private static final String mSurrenderGame_EN = "Surrender Game? Say Yes / No";

    private static final String mYes_EN = "Yes";
    private static final String mNo_EN = "No";

    //---------------------------

    private static final String mPawn_DE = "Bauer";
    private static final String mKnight_DE = "Springer";
    private static final String mBishop_DE = "Läufer";
    private static final String mRook_DE = "Turm";
    private static final String mQueen_DE = "Dame";
    private static final String mQueen2_DE = "Königin";
    private static final String mKing_DE = "König";

    private static final String mOne_DE = "Eins";
    private static final String mTwo_DE = "Zwei";
    private static final String mTwo2_DE = "Zwo";
    private static final String mThree_DE = "Drei";
    private static final String mFour_DE = "Vier";
    private static final String mFive_DE = "Fünf";
    private static final String mSix_DE = "Sechs";
    private static final String mSeven_DE = "Sieben";
    private static final String mEight_DE = "Acht";

    private static final String mSpeakNow_DE = "Jetzt Sprechen";
    private static final String mMove_DE = "Zug";

    private static final String mCorrect_Abort_DE = "Richtig? Nein zum Abbrechen";
    private static final String mCorrect_YESNO_DE = "Richtig? Ja / Nein";
    private static final String mEndGame_DE = "Spiel beenden? Ja / Nein";
    private static final String mRestartGame_DE = "Spiel neustarten? Ja / Nein";
    private static final String mSurrenderGame_DE = "Spiel aufgeben? Ja / Nein";

    private static final String mYes_DE = "Ja";
    private static final String mNo_DE = "Nein";

    //---------------------------

    public static String mPawn = "";
    public static String mKnight = "";
    public static String mBishop = "";
    public static String mRook = "";
    public static String mQueen = "";
    public static String mQueen2 = "";
    public static String mKing = "";

    public static String mOne = "";
    public static String mTwo = "";
    public static String mTwo2 = "";
    public static String mThree = "";
    public static String mFour = "";
    public static String mFive = "";
    public static String mSix = "";
    public static String mSeven = "";
    public static String mEight = "";

    public static String mSpeakNow = "";
    public static String mMove = "";

    public static String mCorrect_Abort = "";
    public static String mCorrect_YESNO = "";
    public static String mEndGame = "";
    public static String mRestartGame = "";
    public static String mSurrenderGame = "";

    public static String mYes = "";
    public static String mNo = "";

    public static final int WHITE_PAWN = 0;
    public static final int BLACK_PAWN = 1;
    public static final int WHITE_KNIGHT = 2;
    public static final int BLACK_KNIGHT = 3;
    public static final int WHITE_BISHOP = 4;
    public static final int BLACK_BISHOP = 5;
    public static final int WHITE_ROOK = 6;
    public static final int BLACK_ROOK = 7;
    public static final int WHITE_QUEEN = 8;
    public static final int BLACK_QUEEN = 9;
    public static final int WHITE_KING = 10;
    public static final int BLACK_KING = 11;


    public static void load(String language) {

        mLanguage = language;

        if(mLanguage.equals("EN")) {
            mPawn = mPawn_EN;
            mKnight = mKnight_EN;
            mBishop = mBishop_EN;
            mRook = mRook_EN;
            mQueen = mQueen_EN;
            mQueen2 = "xxxxxxxxx";
            mKing = mKing_EN;

            mOne = mOne_EN;
            mTwo = mTwo_EN;
            mTwo2 = "xxxxxxxxx";
            mThree = mThree_EN;
            mFour = mFour_EN;
            mFive = mFive_EN;
            mSix = mSix_EN;
            mSeven = mSeven_EN;
            mEight = mEight_EN;

            mSpeakNow = mSpeakNow_EN;
            mMove = mMove_EN;

            mCorrect_Abort = mCorrect_Abort_EN;
            mCorrect_YESNO = mCorrect_YESNO_EN;
            mEndGame = mEndGame_EN;
            mRestartGame = mRestartGame_EN;
            mSurrenderGame = mSurrenderGame_EN;
            mYes = mYes_EN;
            mNo = mNo_EN;
        }
        else if(mLanguage.equals("DE")) {
            mPawn = mPawn_DE;
            mKnight = mKnight_DE;
            mBishop = mBishop_DE;
            mRook = mRook_DE;
            mQueen = mQueen_DE;
            mQueen2 = mQueen2_DE;
            mKing = mKing_DE;

            mOne = mOne_DE;
            mTwo = mTwo_DE;
            mTwo2 = mTwo2_DE;
            mThree = mThree_DE;
            mFour = mFour_DE;
            mFive = mFive_DE;
            mSix = mSix_DE;
            mSeven = mSeven_DE;
            mEight = mEight_DE;

            mSpeakNow = mSpeakNow_DE;
            mMove = mMove_DE;

            mCorrect_Abort = mCorrect_Abort_DE;
            mCorrect_YESNO = mCorrect_YESNO_DE;
            mEndGame = mEndGame_DE;
            mRestartGame = mRestartGame_DE;
            mSurrenderGame = mSurrenderGame_DE;
            mYes = mYes_DE;
            mNo = mNo_DE;
        }
    }

    public static int strToFigureId(String strFigure, boolean isWhite) {
        int id = -2;
        if(strFigure.equals(mPawn.toLowerCase()))
            id = WHITE_PAWN;
        else if(strFigure.equals(mKnight.toLowerCase()))
            id = WHITE_KNIGHT;
        else if(strFigure.equals(mBishop.toLowerCase()))
            id = WHITE_BISHOP;
        else if(strFigure.equals(mRook.toLowerCase()))
            id = WHITE_ROOK;
        else if(strFigure.equals(mQueen.toLowerCase()))
            id = WHITE_QUEEN;
        else if(strFigure.equals(mQueen2.toLowerCase()))
            id = WHITE_QUEEN;
        else if(strFigure.equals(mKing.toLowerCase()))
            id = WHITE_KING;

        if(!isWhite)
            id++;
        return id;
    }

    public static boolean isWhite(int figureId) {
        if(figureId%2 == 0)
            return true;
        else
            return false;
    }

    public static boolean isBlack(int figureId) {
        return !isWhite(figureId);
    }

    public static String FieldIdToStr(int id) {
        int row = id /8;
        int index = id % 8;
        return String.valueOf((char)('a' + index)) + String.valueOf((char)('1' + row));
    }
}
