package board;

import java.util.ArrayList;

public class Row {

    private int y;
    private int nrOfStones=0;
    private ArrayList<BoardSquare> squareList = new ArrayList<BoardSquare>(

    );


    public Row(int yIn){
        y=yIn;
    }

    public void incrementNumber(){
        nrOfStones++;    }
    public void decrementNumber(){        nrOfStones--;    }

    public int getNrOfStones(){                     return nrOfStones;    }
    public ArrayList<BoardSquare> getList(){        return squareList;    }
}
