package board;


import org.bytedeco.javacpp.opencv_stitching;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javax.swing.JFrame;


public class LOA extends JFrame {
    Board board;

    public LOA(String title) {
        super(title);
        super.setLocation(500,250);
        board = new Board();
        //putting the stones on the board
        for(int idx = 2; idx < 8; idx++){
            board.add(new Stone(StoneType.RED_REGULAR), idx, 1);
            board.add(new Stone(StoneType.RED_REGULAR), idx, 8);
            board.add(new Stone(StoneType.BLACK_REGULAR), 1, idx);
            board.add(new Stone(StoneType.BLACK_REGULAR), 8, idx);
        }
        board.initBoard();
        setContentPane(board);
        pack();     //sets the frame so that all of its content fits in it.
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }


    public void closeGame(){
        this.remove(board);
        this.setVisible(false);
        this.dispose();
    }

    public Board getBoard(){
        return board;
    }

}