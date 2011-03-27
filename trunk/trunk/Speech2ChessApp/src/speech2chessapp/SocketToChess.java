/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package speech2chessapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author brandma31324
 */
public class SocketToChess {

    //public enum eSocketCommandType
    public static final int REQ_MOVE = 1;

    public static final int REQ_QUIT = 20;
    public static final int REQ_RESTART = 21;
    public static final int REQ_SURRENDER = 22;

    public static final int REQ_VERIFY = 10;
    public static final int REQ_FIGURES = 12;
    public static final int REQ_PRINT = 13;
    public static final int REQ_PRINT2 = 14;
    public static final int REQ_SPEECHACTIVITY = 15;

    private static Socket socketToChess = null;
    private static Socket socketFromChess = null;

    public static boolean sendCMD(SocketCommand cmd) {
        //System.out.println("-> sendCMD");

        try{
            socketToChess = new Socket( "localhost", 54000 );
            OutputStream dataOutput = socketToChess.getOutputStream();

            byte[] bPrefix = new byte[1];
            bPrefix[0] = (byte)cmd.type;
            dataOutput.write(bPrefix, 0, 1);
            int size = cmd.data.length;
            int ib0 = size >> 8;
            int ib1 = size &0xff;
            byte[] bl = new byte[2];
            bl[0] = (byte)(ib1%0x100);
            bl[1] = (byte)(ib0%0x100);
            dataOutput.write(bl, 0, 2);
            dataOutput.write(cmd.data, 0, cmd.data.length);

            dataOutput.close();
            socketToChess.close();
            return true;
        } catch(Exception ex) {
            //System.out.println(ex.toString());
            return false;
        }
        //System.out.println("<- sendCMD");
    }


    //};

    public static class SocketCommand {
        public int/*eSocketCommandType*/ type;
        public byte[] data;
    }

    public static SocketCommand receiveCMD() {
        //System.out.println("-> receiveCMD");

        SocketCommand s = new SocketCommand();

        try {
            socketFromChess = new Socket( "localhost", 54001 );
            
            InputStream dataInput = socketFromChess.getInputStream();

            byte[] b = new byte[1];
            dataInput.read(b, 0, 1);
            s.type = b[0];
            byte[] bl = new byte[2];
            dataInput.read(bl, 0, 1);
            dataInput.read(bl, 1, 1);
            int len = (bl[0] << 8) + bl[1];
            if(len > 0) {
                s.data = new byte[len];
                dataInput.read(s.data, 0, len);
            }
            else s.data = null;
            dataInput.close();
            socketFromChess.close();

        } catch(Exception ex) {
            //System.out.println(ex.toString());
            s = null;
        }


        //System.out.println("<- receiveCMD");

        return s;
    }
}
