package mlp;

import java.util.Random;

public class MultilayerPerceptron {

    //fields
    private int inputCount; // Number of input units
    private int hidden1Count; // Number of hidden units
    private int hidden2Count;
    private int outputCount; // Number of output units

    // Weight matrices
    private double[][] W1; // First Layer of Weights
    private double[][] W2; // Second Layer of Weights
    private double[][] W3; // Third Layer of Weights

    // Delta weight matrices
    private double[][] DW1; // First Layer of Delta Weights
    private double[][] DW2; // Second Layer of Delta Weights
    private double[][] DW3; // Third Layer of Delta Weights

    // The values at the time of activation (the values input to the hidden and output units)
    private double[] Z1; // First Layer pre-activation values
    private double[] Z2; // Second Layer pre-activation values
    private double[] Z3; // Third Layer pre-activation values

    iActivation activation=null; // Activation Class

    // The values actually output by the units in each layer after being squashed by the
    // activation function
    private double[] InputValues; // Inputs layer Values
    private double[] Hidden1Values; // Hidden layer 1 Values
    private double[] Hidden2Values; // Hidden layer 2 Values
    private double[] OutputValues; // Output layer Values

    //Bias
    private int Bias;
    //end fields


    //constructor
    public MultilayerPerceptron(int inputCount, int hiddenCount1, int hiddenCount2, int outputCount, iActivation activation, int bias){
        this.inputCount=inputCount;
        this.hidden1Count=hiddenCount1;
        this.hidden2Count=hiddenCount2;
        this.outputCount=outputCount;
        this.activation=activation;

        // Initialise the first layer weight and delta weight matrices (accounting for bias unit)
        W1 = initialiseWeights(new double[hidden1Count][this.inputCount+1]);
        DW1 =initialiseDeltaWeights(new double[hidden1Count][this.inputCount+1]);

        // Initialise the second layer weight and delta weight matrices (accounting for bias unit)
        W2 = initialiseWeights(new double[hidden2Count][hidden1Count+1]);
        DW2 = initialiseDeltaWeights(new double[hidden2Count][hidden1Count+1]);

        // Initialise the second layer weight and delta weight matrices (accounting for bias unit)
        W3 = initialiseWeights(new double[this.outputCount][hidden2Count+1]);
        DW3 = initialiseDeltaWeights(new double[this.outputCount][hidden2Count+1]);

        // Initialise the activation vectors
        Z1 = initialiseActivations(new double[hidden1Count]);
        Z2 = initialiseActivations(new double[hidden2Count]);
        Z3 = initialiseActivations(new double[this.outputCount]);

        // Initialise the hidden and output value vectors (same dimensions as activation vectors)
        Hidden1Values=Z1.clone();
        Hidden2Values=Z2.clone();
        OutputValues=Z3.clone();

        //Initialize bias
        Bias = bias;
    }


    private double[][] initialiseWeights(double w[][]){
        Random rn = new Random();
        double offset = 1/(Math.sqrt(inputCount));

        for(int i = 0; i < w.length; i++){
            for(int j=0; j < w[i].length; j++){ // No bias unit
                w[i][j] = offset - rn.nextDouble();

            }
        }
        return w;
    }


    private double[][] initialiseDeltaWeights(double w[][]){
        for(int i=0; i < w.length; i++){
            for(int j=0; j < w[i].length; j++){
                w[i][j]=0;
            }
        }
        return w;
    }


    private double[] initialiseDelta(double d[]){
        for(int i=0; i < d.length; i++){
            d[i]=0;
        }
        return d;
    }


    private double[] initialiseActivations(double z[]){
        for(int i=0; i < z.length; i++){
            z[i]=0;
        }
        return z;
    }


    public void kick(){
        System.out.println("Kicking all weight and re-initializing weights");
        // Kick the candidate solution into a new neighbourhood (random restart)
        System.out.println("Kicking");
        W1 = initialiseWeights(new double[hidden1Count][inputCount+1]); // Account for bias unit
        W2 = initialiseWeights(new double[hidden2Count][hidden1Count+1]); //Account for bias unit
        W3 = initialiseWeights(new double[outputCount][hidden2Count+1]); // Account for bias unit

    }


    public double[] forwardProp(double[] inputs){
        this.InputValues = new double[(inputs.length+1)];

        // Add bias unit to inputs
        InputValues[0] = 1;
        for(int i = 1; i < InputValues.length; i++){
            this.InputValues[i] = inputs[i-1];
        }

        Hidden1Values = new double[hidden1Count+1];
        Hidden1Values[0]=1; // Add bias unit

        // Get hidden layer 1 activations
        for(int i = 0; i < Z1.length; i++){
            Z1[i] = 0;
            for(int j = 0; j < InputValues.length; j++){
                // Hidden Layer Activation
                Z1[i] += W1[i][j] * (InputValues[j] * Bias);
            }
            // Hidden Layer Output value
            Hidden1Values[i+1] = activation.getActivation(Z1[i]);
        }

        Hidden2Values = new double[hidden1Count+1];
        Hidden2Values[0]=1; // Add bias unit

        // Get1 hidden layer 2 activations
        for(int i = 0; i < Z2.length; i++){
            Z2[i] = 0;
            for(int j = 0; j < Hidden1Values.length; j++){
                // Hidden Layer Activation
                Z2[i] += W2[i][j] * (Hidden1Values[j] * Bias);
            }
            // Hidden Layer Output value
            Hidden2Values[i+1] = activation.getActivation(Z2[i]);
        }

        // Get output layer activations
        for(int i = 0; i < Z3.length; i++){
            Z3[i] = 0;
            for(int j = 0; j < Hidden2Values.length; j++){
                // Get output layer Activation
                Z3[i] += W3[i][j] * (Hidden2Values[j] * Bias);
            }
            // Get output layer output Value
            OutputValues[i] = activation.getActivation(Z3[i]);
        }
        return OutputValues;
    }


    // Support a single double value as the target
    public double backProp(double targets){
        double[] t = new double[1];
        t[0]=targets;
        return backProp(t);
    }

    public double backProp(double[] targets){
        double error=0;
        double errorSum=0;
        double[] D1; // First Layer Deltas
        double[] D2; // Second Layer Deltas
        double[] D3; // Third Layer Deltas
        D1 = initialiseDelta(new double[hidden1Count]);
        D2 = initialiseDelta(new double[hidden2Count]);
        D3 = initialiseDelta(new double[outputCount]);

        // Calculate Deltas for the third layer and the error
        for(int i = 0; i < D3.length; i++){
            D3[i] = (targets[i] - OutputValues[i]) * activation.getDerivative(OutputValues[i]);

            errorSum += Math.pow((targets[i] - OutputValues[i]),2); // Squared Error
        }
        error = errorSum / 2;

        // Update Delta Weights for third layer
        for(int i = 0; i < outputCount; i++){
            for(int j = 0; j < hidden2Count + 1; j++){
                DW3[i][j] += D3[i] * Hidden2Values[j];
            }
        }

        // Calculate Deltas for second layer of weights
        for(int j = 0; j < hidden2Count; j++){
            for(int k = 0; k < outputCount; k++){
                D2[j] += (D3[k] * W3[k][j+1]) * activation.getDerivative(Hidden2Values[j+1]);
            }
        }

        // Update second layer deltas
        for(int i = 0; i < hidden2Count; i++){
            for(int j = 0; j < hidden1Count + 1; j++){ // Account for bias unit
                DW2[i][j] += D2[i] * Hidden1Values[j];
            }
        }
        // Calculate Deltas for second layer of weights
        for(int j = 0; j < hidden1Count; j++){
            for(int k = 0; k < hidden2Count; k++){
                D1[j] += (D2[k] * W2[k][j+1]) * activation.getDerivative(Hidden1Values[j+1]);
            }
        }

        // Update second layer deltas
        for(int i = 0; i < hidden1Count; i++){
            for(int j = 0; j < inputCount + 1; j++){ // Account for bias unit
                DW1[i][j] += D1[i] * InputValues[j];
            }
        }
        return error;
    }


    public void updateWeights(double learningRate){
        // Update third layer of weights
        for(int i = 0; i < W3.length; i++){
            for(int j = 0; j < W3[i].length; j++){
                W3[i][j] += learningRate * DW3[i][j];
            }
        }
        // Update second layer of weights
        for(int i = 0; i < W2.length; i++){
            for(int j = 0; j < W2[i].length; j++){
                W2[i][j] += learningRate * DW2[i][j];
            }
        }

        // Update first layer of weights
        for(int i = 0; i < W1.length; i++){
            for(int j = 0; j < W1[i].length; j++){
                W1[i][j] += learningRate * DW1[i][j];
            }
        }
        // Reset delta weights
        DW1 = initialiseDeltaWeights(new double[hidden1Count][inputCount+1]);
        DW2 = initialiseDeltaWeights(new double[hidden2Count][hidden1Count+1]);
        DW3 = initialiseDeltaWeights(new double[outputCount][hidden2Count+1]);
    }
}

