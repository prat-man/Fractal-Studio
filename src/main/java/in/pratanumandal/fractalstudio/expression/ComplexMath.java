package in.pratanumandal.fractalstudio.expression;

import org.apache.commons.math3.complex.Complex;

import java.util.List;

public class ComplexMath {

    public static Complex sin(Complex complex) {
        return complex.sin();
    }

    public static Complex cos(Complex complex) {
        return complex.cos();
    }

    public static Complex tan(Complex complex) {
        return complex.tan();
    }

    public static Complex asin(Complex complex) {
        return complex.asin();
    }

    public static Complex acos(Complex complex) {
        return complex.acos();
    }

    public static Complex atan(Complex complex) {
        return complex.atan();
    }

    public static Complex sinh(Complex complex) {
        return complex.sinh();
    }

    public static Complex cosh(Complex complex) {
        return complex.cosh();
    }

    public static Complex tanh(Complex complex) {
        return complex.tanh();
    }

    public static Complex asinh(Complex complex) {
        return (complex
                .add((complex.multiply(complex).add(Complex.ONE)).sqrt()))
                .log();
    }

    public static Complex acosh(Complex complex) {
        return (complex
                .add((complex.add(Complex.ONE).sqrt())
                        .multiply(complex.subtract(Complex.ONE).sqrt())))
                .log();
    }

    public static Complex atanh(Complex complex) {
        return ((Complex.ONE.add(complex).log())
                        .subtract(Complex.ONE.subtract(complex).log()))
                .divide(2);
    }

    public static Complex log(Complex radix) {
        return radix.log();
    }

    public static Complex log10(Complex radix) {
        return log(radix, new Complex(10));
    }

    public static Complex log(Complex radix, Complex base) {
        return radix.log().divide(base.log());
    }

    public static Complex sqrt(Complex complex) {
        return complex.sqrt();
    }

    public static Complex cbrt(Complex complex) {
        return complex.pow(1.0 / 3.0);
    }

    public static Complex exp(Complex complex) {
        return complex.exp();
    }

    public static Complex max(List<Complex> complexList) {
        Complex max = null;
        for (Complex complex : complexList) {
            if (max == null || complex.abs() > max.abs()) {
                max = complex;
            }
        }
        return max;
    }

    public static Complex min(List<Complex> complexList) {
        Complex min = null;
        for (Complex complex : complexList) {
            if (min == null || complex.abs() < min.abs()) {
                min = complex;
            }
        }
        return min;
    }

}
