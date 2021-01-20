package board;

import java.util.ArrayList;

public class Column {

    private int x;
    private ArrayList<BoardSquare> squareList = new ArrayList<BoardSquare>();
    private int nrOfStones = 0;


    public Column(int xIn) {
        x = xIn;
    }


    public void incrementNumber(){              nrOfStones++;         }
    public void decrementNumber(){              nrOfStones--;         }
    public int getNrOfStones(){                 return nrOfStones;    }
    public ArrayList<BoardSquare> getList() {   return squareList;    }
}
    