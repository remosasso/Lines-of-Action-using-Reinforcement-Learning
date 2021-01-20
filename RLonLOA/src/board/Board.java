package board;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

import static java.lang.Math.abs;


public class Board extends JComponent implements ActionListener{

    // dimension of board square (25% bigger than stone)
    private final static int SQUAREDIM = (int) (Stone.getDimension() * 1.25);
    int count =0;
    // dimension of board (width of 8 squares)
    private final int BOARDDIM = 8 * SQUAREDIM;

    // preferred size of Board component
    private Dimension dimPrefSize;

    // dragging flag -- set to true when user presses mouse button over the stone
    // and cleared to false when user releases mouse button
    private boolean inDrag = false;

    // displacement between drag start coordinates and stone center coordinates
    private int deltax, deltay;

    // reference to positioned stone at start of drag
    private PosCheck posCheck;


    // center location of stone at start of drag
    private int oldcx, oldcy;
    private boolean drawMenu = true;
    private boolean aiVSai = false;

    //declare players here, so that they can be created in the menu method
    private Player player1;
    private Player player2;
    private Player winner;

    //define lists
    private ArrayList<Row>          rows = new ArrayList<Row>();
    private ArrayList<Column>       cols = new ArrayList<Column>();
    private ArrayList<descDiagonal> descLeft = new ArrayList<descDiagonal>();
    private ArrayList<descDiagonal> descRight = new ArrayList<descDiagonal>();
    private ArrayList<ascDiagonal>  ascLeft = new ArrayList<ascDiagonal>();
    private ArrayList<ascDiagonal>  ascRight = new ArrayList<ascDiagonal>();
    private ArrayList<Stone>        checkList = new ArrayList<Stone>();

    // list of Stone objects and their initial positions
    private ArrayList<PosCheck> posChecks;

    //initialize a grid with squares that may contain stones
    private ArrayList<ArrayList<BoardSquare>> squareGrid;

    private int nrRedStones = 12;
    private int nrBlackStones = 12;

    private boolean firstTurn = true;

    //CONSTRUCTOR
    public Board() {
        posChecks = new ArrayList<>();
        dimPrefSize = new Dimension(BOARDDIM, BOARDDIM);
        squareGrid = initGrid();
        player1 = new Player(true,false,true,"Neural Net");
        player2 = new Player(true,true,true,"Random Opponent");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                // Obtain mouse coordinates at time of press.
                int x = me.getX();
                int y = me.getY();
                Player player = determinePlayer();
                // Locate positioned stone under mouse press.
                for (PosCheck posCheck : posChecks)
                    if (Stone.contains(x, y, posCheck.cx, posCheck.cy) && posCheck.stone.isRed() == player.isRed()) {
                        Board.this.posCheck = posCheck;
                        oldcx = posCheck.cx;
                        oldcy = posCheck.cy;
                        deltax = x - posCheck.cx;
                        deltay = y - posCheck.cy;
                        inDrag = true;
                        return;
                    }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                // When mouse released, clear inDrag (to indicate no drag in progress) if inDrag is already set.
                if (inDrag)
                    inDrag = false;
                else
                    return;
                // Snap stone to center of square.
                int x = (me.getX() - deltax) / SQUAREDIM * SQUAREDIM + SQUAREDIM / 2;
                int y = (me.getY() - deltay) / SQUAREDIM * SQUAREDIM + SQUAREDIM / 2;
                Stone movedStone = Board.this.posCheck.stone;
                posCheck.cx = oldcx;
                posCheck.cy = oldcy;
                if (validMove(x, y, oldcx, oldcy, movedStone)) {
                    Move(x, y, oldcx, oldcy, movedStone);
                    changeTurns(determinePlayer());
                    Player nextPlayer = determinePlayer();
                    if (nextPlayer.isAI()) {
                        AImove(nextPlayer);
                    }
                }
                repaint();
                posCheck = null;
            }

            //helper method for the user. You can click on a stone and find out what moves are possible
            @Override
            public void mouseClicked(MouseEvent me){
                int x = me.getX();
                int y = me.getY();
                for (PosCheck posCheck : posChecks) {
                    if (Stone.contains(x, y, posCheck.cx, posCheck.cy)){
                        int xtemp = getIndex(posCheck.cx);
                        int ytemp = getIndex(posCheck.cy);
                        determinePossibleMoves(player2);
                    }
                }
            }

        });


        addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent me) {
                    if (inDrag) {
                         posCheck.cx = me.getX() - deltax;
                        posCheck.cy = me.getY() - deltay;
                        repaint();
                    }
                }
            });
    }



    private void printPos(){
        for(PosCheck x : posChecks){
            if(x.stone.isRed())
                System.out.println(x.cx);
        }
    }

    public void AImove(Player AIPlayer) {
        /*try {
            TimeUnit.SECONDS.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        int move;
        Random rand = new Random();
        determinePossibleMoves(AIPlayer);
        ArrayList<PossibleMove> moveList = AIPlayer.getPossibleMoves();
        PossibleMove option;
        move = rand.nextInt(AIPlayer.getPossibleMoves().size()) + 0;
        option = moveList.get(move);
        int oldx = getObject(option.getStone()).cx;
        int oldy = getObject(option.getStone()).cy;
        int newx = option.getRow() * 62 + 31;
        int newy = option.getCol() * 62 + 31;
        //System.out.println("Random AI Moved (" + getIndex(oldx) + "," + getIndex(oldy) +") to (" + getIndex(newx) + "," + getIndex(newy)+ ")");
        Move(newx, newy, oldx, oldy, option.getStone());
        AIPlayer.getPossibleMoves().clear();
        changeTurns(AIPlayer);
    }


    public int getIndex(int x) {        return (x - 31) / 62;    }

    public int reverseIndex(int x){
        return (x*62)+31;
    }

    public PosCheck getObject(Stone stone) {
        for (PosCheck object : posChecks) {
            if (object.stone == stone)
                return object;
        }
        return null;
    }


    //adds a stone to the board on given location
    public void add(Stone stone, int row, int col) {
        if (row < 1 || row > 8)
            throw new IllegalArgumentException("row out of range: " + row);
        if (col < 1 || col > 8)
            throw new IllegalArgumentException("col out of range: " + col);
        PosCheck posCheck = new PosCheck();
        posCheck.stone = stone;
        posCheck.cx = (col - 1) * SQUAREDIM + SQUAREDIM / 2;
        posCheck.cy = (row - 1) * SQUAREDIM + SQUAREDIM / 2;
        for (PosCheck _posCheck : posChecks)
            if (posCheck.cx == _posCheck.cx && posCheck.cy == _posCheck.cy)
                throw new AlreadyOccupiedException("square at (" + row + "," + col + ") is occupied");
        posChecks.add(posCheck);
        getSquare(posCheck.cx, posCheck.cy).setPiece(stone);
    }


    private BoardSquare getSquare(int x, int y) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if ((int) squareGrid.get(i).get(j).getRec().getCenterX() == x && (int) squareGrid.get(i).get(j).getRec().getCenterY() == y) {
                    return squareGrid.get(i).get(j);
                }
            }
        }
        return null;
    }

    public ArrayList<ArrayList<BoardSquare>> getSquareList(){
        return squareGrid;
    }


    private boolean gameWon(int x, int y, Stone stone, boolean isRed) {
        int nrOfColour;
        if (!isRed) {
            nrOfColour = nrRedStones;
        } else {
            nrOfColour = nrBlackStones;
        }
        if (stone == null || x < 0 || x > 7 || y < 0 || y > 7) {
            return false;
        }
        if (stone.isRed() == isRed && !checkList.contains(stone)) {
            checkList.add(stone);
            if (x + 1 < 8) {
                if (y + 1 < 8)
                    gameWon(x + 1, y + 1, squareGrid.get(x + 1).get(y + 1).getPiece(), isRed);
                if (y - 1 > -1)
                    gameWon(x + 1, y - 1, squareGrid.get(x + 1).get(y - 1).getPiece(), isRed);
                gameWon(x + 1, y, squareGrid.get(x + 1).get(y).getPiece(), isRed);
            }
            if (x - 1 > -1) {
                if (y + 1 < 8)
                    gameWon(x - 1, y + 1, squareGrid.get(x - 1).get(y + 1).getPiece(), isRed);
                if (y - 1 > -1)
                    gameWon(x - 1, y - 1, squareGrid.get(x - 1).get(y - 1).getPiece(), isRed);
                gameWon(x - 1, y, squareGrid.get(x - 1).get(y).getPiece(), isRed);
                if (y + 1 < 8)
                    gameWon(x, y + 1, squareGrid.get(x).get(y + 1).getPiece(), isRed);
                if (y - 1 > -1)
                    gameWon(x, y - 1, squareGrid.get(x).get(y - 1).getPiece(), isRed);
            }
        } else {
            return false;
        }

        if (checkList.size() == nrOfColour) {
            return true;
        } else {
            return false;
        }
    }


    private boolean validMove(int endX, int endY, int startX, int startY, Stone stone) {
        if (startX == endX) {
            return (abs(startY - endY) / 62 == nrStonesCol((endX - 31) / 62, startY, endY, stone));
        } else if (startY == endY) {
            return (abs(startX - endX) / 62 == nrStonesRow((endY - 31) / 62, startX, endX, stone));
        } else if (((endX > startX && endY > startY) && sameDiagonalDesc(getIndex(startX), getIndex(endX), getIndex(startY), getIndex(endY))) || ((endX < startX && endY < startY) && sameDiagonalDesc(getIndex(endX), getIndex(startX), getIndex(endY), getIndex(startY)))) {
            return (abs(startX - endX) / 62 == nrStonesDiagDesc((endX - 31) / 62, (endY - 31) / 62, startX, endX, startY, endY, stone));
        } else if (((endX > startX && endY < startY) && sameDiagonalAsc(getIndex(startX), getIndex(endX), getIndex(startY), getIndex(endY))) || ((endX < startX && endY > startY) && sameDiagonalAsc(getIndex(endX), getIndex(startX), getIndex(endY), getIndex(startY)))) {
            return (abs(startX - endX) / 62 == nrStonesDiagAsc((endX - 31) / 62, (endY - 31) / 62, startX, endX, startY, endY, stone));
        }
        return false;
    }


    private boolean sameDiagonalAsc(int x1, int x2, int y1, int y2) {
        while (x1 < 7 && y1 > 0) {
            x1++;
            y1--;
            if (x1 == x2 && y1 == y2) {
                return true;
            }
        }
        return false;
    }


    private boolean sameDiagonalDesc(int x1, int x2, int y1, int y2) {
        while (x1 < 7 && y1 < 7) {
            x1++;
            y1++;
            if (x1 == x2 && y1 == y2) {
                return true;
            }
        }
        return false;
    }



    private int nrStonesRow(int indexY, int start, int end, Stone movedStone) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            Stone piece = squareGrid.get(i).get(indexY).getPiece();
            if (piece != null) {
                int pieceX = (int) squareGrid.get(i).get(indexY).getRec().getCenterX();
                //If the stone is of the opponent and is in the path of moving, it is considered an invalid move.
                if (movedStone.isRed() != piece.isRed() && ((pieceX > start && pieceX < end) || (pieceX < start) && (pieceX > end))) {
                    return -1;
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    private int nrStonesCol(int indexX, int start, int end, Stone movedStone) {
        int count = 0;
        for (int i = 0; i < 8; i++) {
            Stone piece = squareGrid.get(indexX).get(i).getPiece();
            if (piece != null) {
                int pieceY = (int) squareGrid.get(indexX).get(i).getRec().getCenterY();
                //If the stone is of the opponent and is in the path of moving it is an invalid move.
                if (movedStone.isRed() != piece.isRed() && ((pieceY > start && pieceY < end) || (pieceY < start) && (pieceY > end))) {
                    return -1;
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    private int nrStonesDiagDesc(int x, int y, int startX, int endX, int startY, int endY, Stone movedStone) {
        int startIndexX = 0;
        int startIndexY = 0;
        int count = 0;

        if (x - y < 0) {
            startIndexY = abs(x - y);
        } else if (x - y > 0) {
            startIndexX = abs(x - y);
        }

        while ((startIndexX) < 8 && (startIndexY < 8)) {
            Stone piece = squareGrid.get(startIndexX).get(startIndexY).getPiece();
            if (piece != null) {
                int pieceY = (int) squareGrid.get(startIndexX).get(startIndexY).getRec().getCenterY();
                int pieceX = (int) squareGrid.get(startIndexX).get(startIndexY).getRec().getCenterX();
                //If the stone is of the opponent and is in the path of moving it is an invalid move.
                if (movedStone.isRed() != piece.isRed() && ((pieceY > startY && pieceY < endY && pieceX > startX && pieceX < endX) || (pieceY < startY && pieceY > endY && pieceX < startX && pieceX > endX))) {
                    return -1;
                } else {
                    count++;
                }
            }
            startIndexX++;
            startIndexY++;
        }
        return count;
    }

    private int nrStonesDiagAsc(int x, int y, int startX, int endX, int startY, int endY, Stone movedStone) {
        int startIndexX = 0;
        int startIndexY = 7;
        int count = 0;
        if (x + y > 7) {
            while (y != 7) {
                x--;
                y++;
            }
            startIndexX = x;
        } else if (x + y < 7) {
            startIndexY = x + y;
        }

        while ((startIndexX) < 8 && (startIndexY >= 0)) {
            Stone piece = squareGrid.get(startIndexX).get(startIndexY).getPiece();
            if (piece != null) {
                int pieceY = (int) squareGrid.get(startIndexX).get(startIndexY).getRec().getCenterY();
                int pieceX = (int) squareGrid.get(startIndexX).get(startIndexY).getRec().getCenterX();
                //If the stone is of the opponent and is in the path of moving it is an invalid move.
                if (movedStone.isRed() != piece.isRed() && ((pieceY > startY && pieceY < endY && pieceX < startX && pieceX > endX) || (pieceY < startY && pieceY > endY && pieceX > startX && pieceX < endX))) {
                    return -1;
                } else {
                    count++;
                }
            }
            startIndexX++;
            startIndexY--;
        }
        return count;
    }


    public Player determinePlayer() {
        if (player1.itsTurn()) {
            return player1;
        } else {
            return player2;
        }
    }


    public void changeTurns(Player player) {
        if (player1 == player) {
            player1.setTurn(false);
            player2.setTurn(true);
        } else {
            player1.setTurn(true);
            player2.setTurn(false);
        }
    }


    @Override
    public Dimension getPreferredSize() {        return dimPrefSize;    }
/*
    ArrayList<Number> data = new ArrayList<>();

    public void addToGraph(ArrayList<Number> x,boolean draw){
        data = x;
        drawGraph = draw;
        repaint();
    }
    boolean drawGraph = false;

    @Override
    protected void paintComponent(Graphics g) {
        if(drawGraph){
            final int PAD = 20;

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        // Draw ordinate.
        g2.draw(new Line2D.Double(PAD, PAD, PAD, h-PAD));
        // Draw abcissa.
        g2.draw(new Line2D.Double(PAD, h-PAD, w-PAD, h-PAD));
        double xInc = (double)(w - 2*PAD)/(data.size()-1);
        double scale = (double)(h - 2*PAD)/getMax();
        // Mark data points.
        g2.setPaint(Color.red);
        for(int i = 0; i < data.size(); i++) {
            double x = PAD + i*xInc;
            double y = h - PAD - scale*data.get(i).intValue();
            g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));
        }
        }
    }

    private int getMax() {
        int max = -Integer.MAX_VALUE;
        for(int i = 0; i < data.size(); i++) {
            if(data.get(i).intValue() > max)
                max = data.get(i).intValue();
        }
        return max;
    }

*/
        /*
        //paintMenu();
        paintBoard(g);
        if(aiVSai && firstTurn){
            paintNextTurnButton();
            firstTurn = false;
        }
        //paintWinner();
        for (PosCheck posCheck : posChecks)
            if (posCheck != Board.this.posCheck)
                posCheck.stone.draw(g, posCheck.cx, posCheck.cy);

        // Draw dragged stone last so that it appears over any underlying stone.
        if (posCheck != null)
            posCheck.stone.draw(g, posCheck.cx, posCheck.cy);
    }

    }*/

    public boolean someoneWon(){
        return winner != null;
    }

    public Player getWinner(){
        return winner;
    }

    private void paintWinner() {
        if (winner != null) {
            changeTurns(determinePlayer());
            JOptionPane.showMessageDialog(this, winner.getName() + " won the game.");
        }
    }


    //create an extra frame for the next turn button
    private void paintNextTurnButton(){
        JFrame frame = new JFrame("Next Turn Button");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton b = new JButton("Next Turn");
        b.addActionListener(this);
        frame.getContentPane().add(b, BorderLayout.CENTER);
        frame.pack();
        frame.setLocation(1000,250);
        frame.setVisible(true);
    }


    //action that is performed when the button next turn is pressed
    public void actionPerformed(ActionEvent e){
        Player nextPlayer = determinePlayer();
        AImove(nextPlayer);
        repaint();
    }


    private void paintMenu() {
        if (drawMenu) {
            Object[] options = {"Player vs Player",
                    "Player vs AI",
                    "AI vs AI"};
            int n = JOptionPane.showOptionDialog(null,
                    "Choose the player species:",
                    "Lines of Action",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]);
            switch (n) {
                case 0:
                    player1 = new Player(false,false,true,"Real Player Black");
                    player2 = new Player(false,true,false,"Real Player Red");
                    break;
                case 1:
                    player1 = new Player(false,false,true,"Real Player Black");
                    player2 = new Player(true,true,false, "AI Player Red");
                    break;
                case 2:
                    player1 = new Player(true,false,true, "AI Player Black");
                    player2 = new Player(true,true,false, "AI Player Red");
                    aiVSai = true;
                    break;
            }
            drawMenu = false;
        }
    }


    private void paintBoard(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Paint the board.
        for (int row = 0; row < 8; row++) {
            g.setColor(((row & 1) != 0) ? Color.BLACK : Color.WHITE);
            for (int col = 0; col < 8; col++) {
                g.fillRect(col * SQUAREDIM, row * SQUAREDIM, SQUAREDIM, SQUAREDIM);
                g.setColor((g.getColor() == Color.BLACK) ? Color.WHITE : Color.BLACK);
            }
        }
    }


    private ArrayList<ArrayList<BoardSquare>> initGrid() {
        ArrayList<ArrayList<BoardSquare>> list = new ArrayList<ArrayList<BoardSquare>>();
        for (int i = 0; i < 8; i++) {
            ArrayList<BoardSquare> sublist = new ArrayList<BoardSquare>();
            for (int j = 0; j < 8; j++) {
                sublist.add(j, new BoardSquare(i * 62, j * 62, 62, 62, null));
            }
            list.add(i, sublist);
        }
        return list;
    }


    public void NeuralNetMove(int x, int y, int oldx, int oldy, Stone movedStone) {
        //System.out.println("Neural Network Moved (" + getIndex(oldx) + "," + getIndex(oldy) +") to (" + getIndex(x) + "," + getIndex(y)+ ")");
        Move(x,y,oldx,oldy,movedStone);
        player1.getPossibleMoves().clear();
        changeTurns(player1);
        Player nextPlayer = determinePlayer();
        //System.out.println(nextPlayer.getName());
        if(winner == null) {
            AImove(nextPlayer);
        }
    }

    public void Move(int x, int y, int oldx, int oldy, Stone movedStone) {
        PosCheck posCheck = getObject(movedStone);
        Stone possibleStone = squareGrid.get(getIndex(x)).get(getIndex(y)).getPiece();
        boolean removed = false;
        if (possibleStone == null || (possibleStone != null && possibleStone.isRed() != movedStone.isRed())) {
            squareGrid.get(getIndex(oldx)).get(getIndex(oldy)).setPiece(null);
            squareGrid.get(getIndex(x)).get(getIndex(y)).setPiece(movedStone);
            if (possibleStone != null) {
                posChecks.remove(getObject(possibleStone));
                removed = true;
                if (posCheck.stone.isRed()) {
                    nrRedStones--;
                } else {
                    nrBlackStones--;
                }
            }
            updateBoard(getIndex(oldx), getIndex(oldy), getIndex(x), getIndex(y), removed);
            posCheck.updateLocation(x, y, movedStone);
            Player player = determinePlayer();
            if (gameWon(getIndex(x), getIndex(y), movedStone, player.isRed()) || nrBlackStones == 1 || nrRedStones == 1) {
                checkList.clear();
                winner = player;
            }
            checkList.clear();
        } else if(!determinePlayer().isAI()){
                Board.this.posCheck.cx = oldx;
                Board.this.posCheck.cy = oldy;
        }
        repaint();
    }


    public void initBoard(){
        for(int i=0; i<8; i++){
            rows.add(new Row(i));
            cols.add(new Column(i));
            for(int j=0; j<8; j++){
                rows.get(i).getList().add(squareGrid.get(j).get(i));
                cols.get(i).getList().add(squareGrid.get(i).get(j));
                if(squareGrid.get(j).get(i).getPiece() != null) {
                    rows.get(i).incrementNumber();
                }
                if(squareGrid.get(i).get(j).getPiece() != null){
                     cols.get(i).incrementNumber();
                }
            }
        }
        int tempX;
        int tempY;
        descRight.add(null);
        for(int i=0; i<8; i++){
            descLeft.add(new descDiagonal(0,i));
            tempX = descLeft.get(i).getX();
            tempY = descLeft.get(i).getY();
            while(tempX <8 && tempY <8){
                descLeft.get(i).getList().add(squareGrid.get(tempX).get(tempY));
                if(squareGrid.get(tempX).get(tempY).getPiece() != null) {
                    descLeft.get(i).incrementNumber();
                }
                tempX++;
                tempY++;
            }
        }

        for(int i=1; i<8; i++){
            descRight.add(new descDiagonal(i,0));
            tempX = descRight.get(i).getX();
            tempY = descRight.get(i).getY();
            while(tempX <8 && tempY <8){
                descRight.get(i).getList().add(squareGrid.get(tempX).get(tempY));
                if(squareGrid.get(tempX).get(tempY).getPiece() != null) {
                    descRight.get(i).incrementNumber();
                }
                tempX++;
                tempY++;
            }
        }
        ascRight.add(null);
        for(int i=0; i<8; i++){
            ascLeft.add(new ascDiagonal(0,i));
            tempX = ascLeft.get(i).getX();
            tempY = ascLeft.get(i).getY();
            while(tempX <8 && tempY >=0){
                ascLeft.get(i).getList().add(squareGrid.get(tempX).get(tempY));
                if(squareGrid.get(tempX).get(tempY).getPiece() != null) {
                    ascLeft.get(i).incrementNumber();
                }
                tempX++;
                tempY--;
            }

        }
        for(int i=1; i<8; i++){
            ascRight.add(new ascDiagonal(i,7));
            tempX = ascRight.get(i).getX();
            tempY = ascRight.get(i).getY();
            while(tempX <8 && tempY >=0){
                ascRight.get(i).getList().add(squareGrid.get(tempX).get(tempY));
                if(squareGrid.get(tempX).get(tempY).getPiece() != null) {
                    ascRight.get(i).incrementNumber();
                }
                tempX++;
                tempY--;
            }

        }
    }


    //Determines possible moves for a player and updates the list of possible moves
    private void determinePossibleMoves(Player player){
        int x,y;
        for(PosCheck PossibleStone : posChecks){
            if(PossibleStone.stone != null && PossibleStone.stone.isRed() == player.isRed()){
                x = getIndex(PossibleStone.cx);
                y = getIndex(PossibleStone.cy);
                checkRow(PossibleStone.stone,x,y, player);
                checkCol(PossibleStone.stone,x,y, player);
                checkDesc(PossibleStone.stone,x,y,player);
                checkAsc(PossibleStone.stone,x,y,player);
            }
        }
    }


    private void checkRow(Stone stone, int x, int y, Player player){
        int steps = rows.get(y).getNrOfStones();
        Stone possibleStone;
        if(steps != nrStonesRow(y,x,x+steps,stone)){
            winner=player1;
        }
        if(x + steps<8) {
            possibleStone = squareGrid.get(x+steps).get(y).getPiece();
            if ((possibleStone == null || (possibleStone!=null && possibleStone.isRed() != stone.isRed())) && validMove(reverseIndex(x+steps),reverseIndex(y),reverseIndex(x),reverseIndex(y),stone) ) {
                player.updatePossibleMoves(stone, x +steps, y);
            }
        }

        if(x - steps >= 0) {
            possibleStone = squareGrid.get(x - steps).get(y).getPiece();
            if ((possibleStone == null ||(possibleStone!=null && possibleStone.isRed() != stone.isRed()))  && validMove(reverseIndex(x-steps),reverseIndex(y),reverseIndex(x),reverseIndex(y),stone)) {
                player.updatePossibleMoves(stone, x - steps,y);
            }
        }
    }


    private void checkCol(Stone stone, int x, int y, Player player){
        int steps = cols.get(x).getNrOfStones();
        Stone possibleStone;
        if(y + steps < 8) {
            possibleStone = squareGrid.get(x).get(y+steps).getPiece();
            if ((possibleStone == null ||(possibleStone!=null && possibleStone.isRed() != stone.isRed()))  && validMove(reverseIndex(x),reverseIndex(y+steps),reverseIndex(x),reverseIndex(y),stone)) {     player.updatePossibleMoves(stone, x , y+steps);
            }
        }

        if(y - steps >= 0) {
            possibleStone = squareGrid.get(x).get(y-steps).getPiece();
            if ((possibleStone == null ||(possibleStone!=null && possibleStone.isRed() != stone.isRed()))  && validMove(reverseIndex(x),reverseIndex(y-steps),reverseIndex(x),reverseIndex(y),stone)) {    player.updatePossibleMoves(stone, x,y-steps);
            }
        }
    }


    private void checkDesc(Stone stone, int x, int y, Player player){
        int steps;
        if(x-y<=0){
            steps = descLeft.get(backTrack(x,y,true,true)).getNrOfStones();
        }else{
            steps = descRight.get(backTrack(x,y,true,false)).getNrOfStones();
        }
        Stone possibleStone;
        if(x + steps<8 && y+steps<8) {
            possibleStone = squareGrid.get(x+steps).get(y+steps).getPiece();
            if ((possibleStone == null ||(possibleStone!=null && possibleStone.isRed() != stone.isRed()))  && validMove(reverseIndex(x+steps),reverseIndex(y+steps),reverseIndex(x),reverseIndex(y),stone)) {     player.updatePossibleMoves(stone, x + steps, y+steps);
            }
        }

        if(x - steps >= 0 && y - steps >= 0) {
            possibleStone = squareGrid.get(x - steps).get(y - steps).getPiece();
            if ((possibleStone == null ||(possibleStone!=null && possibleStone.isRed() != stone.isRed()))  && validMove(reverseIndex(x-steps),reverseIndex(y-steps),reverseIndex(x),reverseIndex(y),stone)) {     player.updatePossibleMoves(stone, x - steps,y - steps);
            }
        }
    }


    private void checkAsc(Stone stone, int x, int y, Player player){
        int steps;
        if(x+y<=7){
            steps = ascLeft.get(backTrack(x,y,false,true)).getNrOfStones();
        }else{
            steps = ascRight.get(backTrack(x,y,false,false)).getNrOfStones();
        }
        Stone possibleStone;
        if(x + steps<8 && y-steps>=0) {
            possibleStone = squareGrid.get(x+steps).get(y-steps).getPiece();
            if ((possibleStone == null ||(possibleStone!=null && possibleStone.isRed() != stone.isRed()))  && validMove(reverseIndex(x+steps),reverseIndex(y-steps),reverseIndex(x),reverseIndex(y),stone)) {      player.updatePossibleMoves(stone, x + steps, y-steps);
            }
        }

        if(x - steps >= 0 && y + steps < 8) {
            possibleStone = squareGrid.get(x - steps).get(y + steps).getPiece();
            if ((possibleStone == null ||(possibleStone!=null && possibleStone.isRed() != stone.isRed()))  && validMove(reverseIndex(x-steps),reverseIndex(y+steps),reverseIndex(x),reverseIndex(y),stone)) {    player.updatePossibleMoves(stone, x - steps,y + steps);
            }
        }
    }


    private void updateBoard(int oldx, int oldy, int newx, int newy, boolean removed) {
        if (!removed) {
            //If row movement
            if (oldy == newy) {
                //update old and new column
                cols.get(oldx).decrementNumber();
                cols.get(newx).incrementNumber();
                updateDescDiagonals(oldx, oldy, newx, newy, removed);
                updateAscDiagonals(oldx, oldy, newx, newy, removed);
            } else if (oldx == newx) {
                rows.get(oldy).decrementNumber();
                rows.get(newy).incrementNumber();
                updateDescDiagonals(oldx, oldy, newx, newy, removed);
                updateAscDiagonals(oldx, oldy, newx, newy, removed);
            } else if (((newx > oldx && newy > oldy) ) || ((newx < oldx && newy < oldy))){
                cols.get(oldx).decrementNumber();
            cols.get(newx).incrementNumber();
            rows.get(oldy).decrementNumber();
            rows.get(newy).incrementNumber();
            updateAscDiagonals(oldx, oldy, newx, newy, removed);
        } else if ((((newx > oldx && newy < oldy)) || ((newx < oldx && newy > oldy)))) {
            cols.get(oldx).decrementNumber();
            cols.get(newx).incrementNumber();
            rows.get(oldy).decrementNumber();
            rows.get(newy).incrementNumber();
            updateDescDiagonals(oldx, oldy, newx, newy, removed);
        }
        }else if(removed){
            if(oldy == newy || oldx==newx){
                //update old and new column
                cols.get(oldx).decrementNumber();
                rows.get(oldy).decrementNumber();
                updateDescDiagonals(oldx,oldy,newx,newy,removed);
                updateAscDiagonals(oldx,oldy,newx,newy,removed);
            }else if(((((newx > oldx && newy > oldy) )) || ((newx < oldx && newy < oldy) ))) {
                cols.get(oldx).decrementNumber();
                rows.get(oldy).decrementNumber();
                updateAscDiagonals(oldx,oldy,newx,newy,removed);
            }else if ((((newx > oldx && newy < oldy)) || ((newx < oldx && newy > oldy)))) {
                cols.get(oldx).decrementNumber();
                rows.get(oldy).decrementNumber();
                updateDescDiagonals(oldx,oldy,newx,newy,removed);
            }
        }

    }


    public void updateDescDiagonals(int oldx, int oldy, int newx, int newy, boolean removed){
        int index;
        //update the old left or right descending diagonal
        if(oldx-oldy<=0){
            index = backTrack(oldx,oldy,true,true);
            descLeft.get(index).decrementNumber();
        }else{
            index = backTrack(oldx,oldy,true,false);
            descRight.get(index).decrementNumber();
        }
        if(!removed) {
            //update the new left or right descending diagonal
            if (newx - newy <= 0) {
                index = backTrack(newx, newy, true, true);
                descLeft.get(index).incrementNumber();
            } else {
                index = backTrack(newx, newy, true, false);
                descRight.get(index).incrementNumber();
            }
        }
    }


    public void updateAscDiagonals(int oldx, int oldy, int newx, int newy, boolean removed) {
        int index;
        //update left or right ascending diagonal
        if (oldx + oldy <= 7) {
            index = backTrack(oldx, oldy, false, true);
            ascLeft.get(index).decrementNumber();
        } else {
            index = backTrack(oldx, oldy, false, false);
            ascRight.get(index).decrementNumber();
        }
        if(!removed) {
            //update the new left or right ascending diagonal
            if (newx + newy <= 7) {
                index = backTrack(newx, newy, false, true);
                ascLeft.get(index).incrementNumber();
            } else {
                index = backTrack(newx, newy, false, false);
                ascRight.get(index).incrementNumber();
            }
        }
    }


    //finds the origin index of the diagonal given by x and y
    public int backTrack(int x, int y, boolean desc, boolean left){
        if(desc){
            while(x>0 && y>0){
                x--;
                y--;
            }
            if(left) {
                return y;
            }else{
                return x;
            }
        }else{
            while(x>0 && y<7){
                x--;
                y++;
            }
            if(left) {
                return y;
            }else{
                return x;
            }
        }
    }

    public ArrayList<PossibleMove> getPossibleMovesNetwork(){
        determinePossibleMoves(player1);
        return player1.getPossibleMoves();
    }

    //helper class
    public class PosCheck {
        public Stone stone;
        public int cx;
        public int cy;


        public void updateLocation(int x, int y, Stone stone){
            this.stone = stone;
            this.cx = x;
            this.cy = y;
        }
    }
}