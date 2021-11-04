package in.pratanumandal.fractalstudio.expression;

import in.pratanumandal.expr4j.parser.ExpressionParser;
import in.pratanumandal.expr4j.token.Operator;
import in.pratanumandal.expr4j.token.Operator.OperatorType;
import org.apache.commons.math3.complex.Complex;

import java.util.Arrays;

public class ComplexParser extends ExpressionParser<Complex> {

    @Override
    protected void initialize() {
        this.addExecutable(Arrays.asList(
                new Operator<>("+", OperatorType.INFIX, 1, (operands) -> operands.get(0).add(operands.get(1))),
                new Operator<>("-", OperatorType.INFIX, 1, (operands) -> operands.get(0).subtract(operands.get(1))),

                new Operator<>("*", OperatorType.INFIX, 2, (operands) -> operands.get(0).multiply(operands.get(1))),
                new Operator<>("/", OperatorType.INFIX, 2, (operands) -> operands.get(0).divide(operands.get(1))),

                new Operator<>("^", OperatorType.INFIX, 3, (operands) -> {
                    if (operands.get(0).equals(Complex.ZERO)) return Complex.ZERO;
                    if (operands.get(1).equals(Complex.ZERO)) return Complex.ONE;
                    return operands.get(0).pow(operands.get(1));
                }),

                new Operator<>("atanh", OperatorType.PREFIX, 10, (operands) -> (Complex.ONE.add(operands.get(0)).divide(Complex.ONE.subtract(operands.get(0)))).log().divide(2)),

                new Operator<>("exp", OperatorType.PREFIX, 10, (operands) -> operands.get(0).exp())
        ));

        addConstant("i", Complex.I);
    }

    @Override
    protected Complex unaryPlus(Complex complex) {
        return complex;
    }

    @Override
    protected Complex unaryMinus(Complex complex) {
        return Complex.ZERO.subtract(complex);
    }

    @Override
    protected Complex implicitMultiplication(Complex complex0, Complex complex1) {
        return complex0.multiply(complex1);
    }

    @Override
    protected Complex stringToOperand(String s) {
        return new Complex(Double.parseDouble(s), 0);
    }

    @Override
    protected String operandToString(Complex complex) {
        StringBuilder representation = new StringBuilder();
        double real = complex.getReal();
        double imaginary = complex.getImaginary();
        if (real != 0.0) {
            if (real == (int) real) representation.append((int) real);
            else representation.append(real);
        }
        if (imaginary != 0.0) {
            if (imaginary == (int) imaginary) representation.append((int) imaginary);
            else representation.append(imaginary);
        }
        return representation.toString();
    }

}
