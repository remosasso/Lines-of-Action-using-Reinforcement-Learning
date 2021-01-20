package board;

import board.Stone;

public class PossibleMove {

    private Stone stone;
    private int row;
    private int col;


    public PossibleMove(Stone stone, int row, int col){
        this.stone = stone;
        this.row = row;
        this.col = col;
    }


    public int getRow() {                   return row;         }
    public int getCol() {                   return col;         }
    public Stone getStone() {               return this.stone;  }
    public void setChecker(Stone stone) {   this.stone = stone; }
    public void setRow(int row) {           this.row = row;     }
    public void setCol(int col) {           this.col = col;     }
}
