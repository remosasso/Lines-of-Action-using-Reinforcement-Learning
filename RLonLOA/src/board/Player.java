package board;

import board.Stone;

import java.util.ArrayList;

public class Player {

    private boolean isAI;
    private boolean isRed;
    private boolean itsTurn;
    private String name;
    private ArrayList<PossibleMove> possibleMoves = new ArrayList<PossibleMove>();


    public Player (boolean isAi, boolean Red, boolean turn, String givenName){
        isAI = isAi;
        isRed = Red;
        itsTurn = turn;
        name = givenName;
    }


    public void updatePossibleMoves(Stone checker, int row, int col){
        this.possibleMoves.add(new PossibleMove(checker, row, col));
    }


    //getters and setters
    public ArrayList<PossibleMove> getPossibleMoves (){     return possibleMoves;   }
    public String getName(){                                return name;            }
    public boolean itsTurn(){                               return itsTurn;         }
    public boolean isRed(){                                 return isRed;           }
    public boolean isAI(){                                  return isAI;            }
    public void setWhetherAI(boolean AI){                   isAI = AI;              }
    public void setTurn(boolean play){                      itsTurn = play;         }


    //the methods below can be used for printing and debugging
    public void printPlayerInfo(){
        System.out.println( "isAI=" + isAI + ", isRed=" + isRed + ", itsTurn=" + itsTurn + ", name=" + name);
    }

    public void printPossibleMoves(){
        for(PossibleMove move : possibleMoves){
            System.out.println("posMove=(" + move.getRow() + ", " + move.getCol() + ")");
        }
        System.out.println("\n");
    }


}
