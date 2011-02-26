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

    //---------------------------

    public static String mPawn = "";
    public static String mKnight = "";
    public static String mBishop = "";
    public static String mRook = "";
    public static String mQueen = "";
    public static String mKing = "";

    public static String mOne = "";
    public static String mTwo = "";
    public static String mThree = "";
    public static String mFour = "";
    public static String mFive = "";
    public static String mSix = "";
    public static String mSeven = "";
    public static String mEight = "";

    public static String mSpeakNow = "";
    public static String mMove = "";

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
            mKing = mKing_EN;

            mOne = mOne_EN;
            mTwo = mTwo_EN;
            mThree = mThree_EN;
            mFour = mFour_EN;
            mFive = mFive_EN;
            mSix = mSix_EN;
            mSeven = mSeven_EN;
            mEight = mEight_EN;

            mSpeakNow = mSpeakNow_EN;
            mMove = mMove_EN;
        }
    }

    public static int strToFigureId(String strFigure, boolean isWhite) {
        int id = 0;
        if(strFigure.equals(mPawn))
            id = WHITE_PAWN;
        else if(strFigure.equals(mKnight))
            id = WHITE_KNIGHT;
        else if(strFigure.equals(mBishop))
            id = WHITE_BISHOP;
        else if(strFigure.equals(mRook))
            id = WHITE_ROOK;
        else if(strFigure.equals(mQueen))
            id = WHITE_QUEEN;
        else if(strFigure.equals(mKing))
            id = WHITE_KING;

        if(!isWhite)
            id++;
        return id;
    }
}
