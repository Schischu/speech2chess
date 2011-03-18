/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import java.util.List;

/**
 *
 * @author i7
 */
public abstract class SpeechEngine {
    public abstract void finish();
    public abstract List<String> record();
}
