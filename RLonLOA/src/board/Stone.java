package board;

import java.awt.Color;
import java.awt.Graphics;

public final class Stone {

    private final static int DIMENSION = 50;
    private StoneType checkerType;


    public Stone(StoneType checkerType) {
        this.checkerType = checkerType;
    }


    public void draw(Graphics g, int cx, int cy) {
        int x = cx - DIMENSION / 2;
        int y = cy - DIMENSION / 2;

        // Set checker color.
        g.setColor(checkerType == StoneType.BLACK_REGULAR ? Color.BLACK : Color.RED);

        // Paint checker.
        g.fillOval(x, y, DIMENSION, DIMENSION);
        g.setColor(Color.WHITE);
        g.drawOval(x, y, DIMENSION, DIMENSION);
    }


    public static boolean contains(int x, int y, int cx, int cy) {
        return (cx - x) * (cx - x) + (cy - y) * (cy - y) < DIMENSION / 2 * DIMENSION / 2;
    }


    public static int getDimension() {      return DIMENSION;                               }
    public boolean isRed(){                 return checkerType == StoneType.RED_REGULAR;    }

    public StoneType getCheckerType(){
        return checkerType;
    }

    @Override
    public String toString(){        return "\ncheckerType = " + this.checkerType;    }
}