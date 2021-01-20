package mlp;

public class SigmoidActivation implements iActivation{

    //Derivative of the activation function is: g'(x)=x(1-x)
    public double getDerivative(double x) {
        return (x * (1.0 - x));
    }


    //sigmoid activation function is: g(x)=1/(1+e^-x)
    public double getActivation(double x) {
        double expVal = Math.exp(-x);
        return 1.0/(1.0 + expVal);
    }
}
