package board;

import java.util.ArrayList;

public class descDiagonal {

    private ArrayList<BoardSquare> squareList = new ArrayList<BoardSquare>();
    private int nrOfStones=0;
    private int x;
    private int y;


    public descDiagonal(int xIn, int yIn){
        x = xIn;
        y = yIn;
    }


    public void incrementNumber(){              nrOfStones++;    }
    public void decrementNumber(){              nrOfStones--;    }

    public int getNrOfStones(){                 return nrOfStones;  }
    public ArrayList<BoardSquare> getList(){    return squareList;  }
    public int getX() {                         return x;           }
    public int getY() {                         return y;           }

}
