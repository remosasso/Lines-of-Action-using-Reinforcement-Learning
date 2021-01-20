package board;

import java.awt.*;

public class BoardSquare {

    private Stone piece;
    private Rectangle rec = new Rectangle();


    public BoardSquare(int x, int y, int width, int height, Stone p){
        piece = p;
        rec.setBounds(x, y, width, height);
    }


    public void setPiece(Stone p){    piece = p;    }
    public Rectangle getRec() {       return rec;   }
    public Stone getPiece() {         return piece; }
}