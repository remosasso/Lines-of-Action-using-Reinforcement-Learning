package board;

import java.util.ArrayList;

public class ascDiagonal {

    //FIELDS
    private ArrayList<BoardSquare> squareList = new ArrayList<>();
    private int x;
    private int y;
    private int nrOfStones=0;

    //constructor
    public ascDiagonal(int xIn, int yIn){
        this.x = xIn;
        this.y = yIn;
    }


    //getters and setters
    public void incrementNumber(){              nrOfStones++;    }
    public void decrementNumber(){              nrOfStones--;    }
    public ArrayList<BoardSquare> getList(){    return squareList;    }
    public int getNrOfStones(){                 return nrOfStones;    }
    public int getX() {                         return x;    }
    public int getY() {                         return y;    }
}
