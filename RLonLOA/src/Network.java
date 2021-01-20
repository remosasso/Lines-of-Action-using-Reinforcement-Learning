import board.*;
import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.awt.EventQueue;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;


public class Network extends JFrame {
    private int maxIndex;
    public INDArray convertGameData(ArrayList<ArrayList<BoardSquare>> gameData){
        double [] inputData = new double[64];
        int index = 0;
        for(int i=0; i<8;i++){
            for(int j=0; j<8;j++) {
                if (gameData.get(i).get(j).getPiece() != null) {
                    if (gameData.get(i).get(j).getPiece().isRed()) {
                        inputData[index] = -1;
                    } else {
                        inputData[index] = 1;
                    }
                }else{
                    inputData[index] = 0;
                }
                index++;
            }
        }
        INDArray board = Nd4j.create(inputData);
        return board;
    }

    public Network() {
        // Set up network configuration
        NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder();
        // how often should the training set be run, we need something above
        // 1000, or a higher learning-rate - found this values just by trial and
        // error

        builder.iterations(1);
        // learning rate
        builder.learningRate(0.001);//SET TO 0.001 EVENTUALLY

        // fixed seed for the random generator, so any run of this program
        // brings the same results - may not work if you do something like
        // ds.shuffle()
        builder.seed(123);
        // not applicable, this network is to small - but for bigger networks it
        // can help that the network will not only recite the training data
        builder.useDropConnect(false);
        // a standard algorithm for moving on the error-plane, this one works
        // best for me, LINE_GRADIENT_DESCENT or CONJUGATE_GRADIENT can do the
        // job, too - it's an empirical value which one matches best to
        // your problem
        builder.optimizationAlgo(OptimizationAlgorithm.LINE_GRADIENT_DESCENT);
        // init the bias with 0 - empirical value, too
        builder.biasInit(0);
        // from "http://deeplearning4j.org/architecture": The networks can
        // process the input more quickly and more accurately by ingesting
        // minibatches 5-10 elements at a time in parallel.
        // this example runs better without, because the dataset is smaller than
        // the mini batch size
        builder.miniBatch(true);

        // create a multilayer network with 2 layers (including the output
        // layer, excluding the input layer)
        NeuralNetConfiguration.ListBuilder listBuilder = builder.list();
        DenseLayer.Builder hiddenLayerBuilder = new DenseLayer.Builder();
        //DenseLayer.Builder hiddenLayerBuilder2 = new DenseLayer.Builder();
        // two input connections - simultaneously defines the number of input
        // neurons, because it's the first non-input-layer
        hiddenLayerBuilder.nIn(64);
        //hiddenLayerBuilder2.nIn(32);
        // number of outgooing connections, nOut simultaneously defines the
        // number of neurons in this layer
        hiddenLayerBuilder.nOut(50);
        //hiddenLayerBuilder2.nOut(32);
        // put the output through the sigmoid function, to cap the output
        // valuebetween 0 and 1
        hiddenLayerBuilder.activation(Activation.SIGMOID);
        //hiddenLayerBuilder2.activation(Activation.RELU);
        // random initialize weights with values between 0 and 1
        hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        //hiddenLayerBuilder2.weightInit(WeightInit.DISTRIBUTION);
        hiddenLayerBuilder.dist(new UniformDistribution(-0.5, 0.5));
       // hiddenLayerBuilder2.dist(new UniformDistribution(0, 1));

        // build and set as layer 0
        listBuilder.layer(0, hiddenLayerBuilder.build());
        //listBuilder.layer(1, hiddenLayerBuilder2.build());

        // MCXENT or NEGATIVELOGLIKELIHOOD (both are mathematically equivalent) work ok for this example - this
        // function calculates the error-value (aka 'cost' or 'loss function value'), and quantifies the goodness
        // or badness of a prediction, in a differentiable way
        // For classification (with mutually exclusive classes, like here), use multiclass cross entropy, in conjunction
        // with softmax activation function
        OutputLayer.Builder outputLayerBuilder = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD);
        // must be the same amout as neurons in the layer before
        outputLayerBuilder.nIn(50);
        // two neurons in this layer
        outputLayerBuilder.nOut(1);
        outputLayerBuilder.activation(Activation.SIGMOID);
        outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION);
        outputLayerBuilder.dist(new UniformDistribution(0, 1));
        listBuilder.layer(1, outputLayerBuilder.build());

        // no pretrain phase for this network
        listBuilder.pretrain(true);

        // seems to be mandatory
        // according to agibsonccc: You typically only use that with
        // pretrain(true) when you want to do pretrain/finetune without changing
        // the previous layers finetuned weights that's for autoencoders and
        // rbms
        listBuilder.backprop(true);

        // build and init the network, will check if everything is configured
        // correct
        MultiLayerConfiguration conf = listBuilder.build();
        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        // add an listener which outputs the error every 100 parameter updates
        net.setListeners(new ScoreIterationListener(100));
        net.setInputMiniBatchSize(1);
        // C&P from GravesLSTMCharModellingExample
        // Print the number of parameters in the network (and for each layer)
        Layer[] layers = net.getLayers();
        int totalNumParams = 0;
        for (int i = 0; i < layers.length; i++) {
            int nParams = layers[i].numParams();
            System.out.println("Number of parameters in layer " + i + ": " + nParams);
            totalNumParams += nParams;
        }
        System.out.println("Total number of network parameters: " + totalNumParams);

        //Initialize the user interface backend
        UIServer uiServer = UIServer.getInstance();

        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);

        //Then add the StatsListener to collect this information from the network, as it trains
        net.setListeners(new StatsListener(statsStorage));

        LOA game;
        ArrayList<PossibleMove> possibleMoves;
        ArrayList<ArrayList<BoardSquare>> State;
        ArrayList<ArrayList<BoardSquare>> nextState;
        INDArray valuationBestAfterState;
        INDArray nextTargetQValue;
        int reward;
        int discount = 1;
        int nrOfGames = 0;
        int gamesWon = 0;
        double lr = 1; //learning rate
        double e = 0.1; //random parameter
        ArrayList<Number> TDerrorData = new ArrayList<>();
        Random rand = new Random();
        while (nrOfGames != 1000){
            System.out.println("Game "+ nrOfGames);
            game = new LOA("Lines of Action");
             //     targetQValue = determineGreedyActionAndQtarget(game,possibleMoves,State,net); //Choose action from S using greedy policy
            State = game.getBoard().getSquareList(); //Get current state
            possibleMoves = game.getBoard().getPossibleMovesNetwork(); //Get possible actions
            determineGreedyActionAndQtarget(game, possibleMoves, State, net);
            executeAction(possibleMoves.get(maxIndex), game);
            while (true) {
                State = game.getBoard().getSquareList(); //Get current state
                possibleMoves = game.getBoard().getPossibleMovesNetwork(); //Get possible actions
                if(!possibleMoves.isEmpty()) {
                    valuationBestAfterState = determineGreedyActionAndQtarget(game, possibleMoves, State, net); //Choose action from S using greedy policy
                }else{
                    break;
                }

                //Obtain R, S', A'
                reward = obtainReward(game);
                //nextState = game.getBoard().getSquareList();
                //possibleMoves = game.getBoard().getPossibleMovesNetwork();

                //TD0
                INDArray stateValuation = net.output(convertGameData(State)); //Compute valuation of current state
                INDArray targetValuation = valuationBestAfterState.mul(discount).add(reward).mul(lr); // Compute target value of current state
                INDArray error = targetValuation.sub(stateValuation); //Determine error
                TDerrorData.add(error.getDouble(0));
                System.out.println(stateValuation + " - " + targetValuation + " = " + error);
                net.fit(convertGameData(State),error); //Fit current state with error

                //SARSA
                /*
                if(!possibleMoves.isEmpty()) {
                    //Choose action from S' and get Q value of that Q(S',A')
                    nextTargetQValue = determineGreedyActionAndQtarget(game, possibleMoves, nextState, net);
                    //Q(S,A) + lr(R + y*Q(S',A') - Q(S,A))
                    targetQValue = targetQValue.add(nextTargetQValue.mul(discount).add(reward).sub(targetQValue).mul(lr));
                    //Train network with target
                    net.fit(convertGameData(State),targetQValue);
                }else{
                    break;
                }*/

                 if(reward != 0) { //S is terminal
                     if(reward == 1){ //Network won the game
                         gamesWon++;
                     }
                     break;
                 }
                 //S <- S', and A <- A' (which is already done in the determineGreedyActionAndQtarget function)
                 //State = nextState;
                 //targetQValue = nextTargetQValue;
                if(rand.nextDouble() < e) {
                    //Execute random action
                    executeAction(possibleMoves.get(rand.nextInt(possibleMoves.size())), game);
                }else {
                    //Execute action at maxIndex
                    executeAction(possibleMoves.get(maxIndex), game);
                }
            }
            game.closeGame();
            nrOfGames++;
        }
        int sum = 0;
        int count =0;
        double average = 50.00;
        for(Number error : TDerrorData){
            sum += error.doubleValue();
            if(count%1000==0){
                System.out.println(sum/average);
                sum = 0;
            }
        }
       System.out.println("Won " + gamesWon + " out of " + nrOfGames);
    }

    private int obtainReward(LOA game) {
        //If terminal state, return 1 for win and -1 for lose.
        if (game.getBoard().someoneWon()) {
            System.out.println(game.getBoard().getWinner().getName() + " won the game.");
            if (game.getBoard().getWinner().getName() == "Neural Net") {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }



    private INDArray determineGreedyActionAndQtarget(LOA game, ArrayList<PossibleMove> possibleMoves, ArrayList<ArrayList<BoardSquare>> gameData, MultiLayerNetwork net){
        INDArray afterState;
        double outputQ = 0;
        double max = -0.1; //For comparing the possible move values to get the maximum
        maxIndex = -1; //To store the index of the move with the maximum value
        int index = 0; //Normal index counter
        INDArray targetValue = null;
        for(PossibleMove move : possibleMoves) {
            //tabs mean SARSA implementation comment
            //Create after state (simulate possible move)
            afterState = createAfterState(game, gameData, move);
            // Feed after state into network
            INDArray output = net.output(afterState);
            //Obtain network Q value and compare it to the current maximum Q value
            outputQ = output.getDouble(0);
            if(outputQ > max){
                max = outputQ;
                maxIndex = index;
                targetValue = output; //Valuation of greedy afterstate
            }
            index++;
        }
        if(targetValue == null){
            System.out.println(outputQ);
        }
        return targetValue;
    }

    private void executeAction(PossibleMove bestMove, LOA game){
       /* //Sleep for n seconds
        try {
            TimeUnit.SECONDS.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        Stone bestStone = bestMove.getStone();
        Board.PosCheck toBeMoved = game.getBoard().getObject(bestStone);

        //Execute the move in the game
        game.getBoard().NeuralNetMove(game.getBoard().reverseIndex(bestMove.getRow()), game.getBoard().reverseIndex(bestMove.getCol()),toBeMoved.cx, toBeMoved.cy, bestStone);

    }

    private INDArray createAfterState(LOA game, ArrayList<ArrayList<BoardSquare>> currentState, PossibleMove move){
            //Create a deep-copy of the current state
            ArrayList<ArrayList<BoardSquare>> simulatedState = copyArrayList(currentState);

            //Simulate the possible move in the copied state
            Board board = game.getBoard();
            int oldx = game.getBoard().getObject(move.getStone()).cx;
            int oldy = game.getBoard().getObject(move.getStone()).cy;
            int newx = move.getRow();
            int newy = move.getCol();
            simulatedState.get(board.getIndex(oldx)).get(board.getIndex(oldy)).setPiece(null);
            simulatedState.get(newx).get(newy).setPiece(move.getStone());

            //Return the simulated state converted into -1, 0, and 1 style
            return convertGameData(simulatedState);
    }

    private ArrayList<ArrayList<BoardSquare>> copyArrayList(ArrayList<ArrayList<BoardSquare>> original ){
        ArrayList<ArrayList<BoardSquare>> copy = new ArrayList<>();
        for(int i=0; i<original.size(); i++) {
            ArrayList<BoardSquare> subcopy = new ArrayList<>();
            for(int j=0; j<original.get(i).size(); j++){
                BoardSquare square = original.get(i).get(j);
                subcopy.add(j, new BoardSquare(square.getRec().x, square.getRec().y, square.getRec().width, square.getRec().height, square.getPiece()));
            }
            copy.add(i,subcopy);
        }
        return copy;
    }
        public static void main(String[] args) {
            new Network();
        }
}