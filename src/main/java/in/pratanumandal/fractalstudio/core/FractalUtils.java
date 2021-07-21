package in.pratanumandal.fractalstudio.core;

public class FractalUtils {

    public static double precision(double d, int p) {
        double pVal = Math.pow(10, p);
        return Math.round(d * pVal) / pVal;
    }

}
