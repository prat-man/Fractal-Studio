package in.pratanumandal.fractalstudio.expression;

import in.pratanumandal.expr4j.parser.ExpressionParser;
import in.pratanumandal.expr4j.token.Function;
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

                new Operator<>("sin", OperatorType.PREFIX, 4, (operands) -> ComplexMath.sin(operands.get(0))),
                new Operator<>("cos", OperatorType.PREFIX, 4, (operands) -> ComplexMath.cos(operands.get(0))),
                new Operator<>("tan", OperatorType.PREFIX, 4, (operands) -> ComplexMath.tan(operands.get(0))),

                new Operator<>("asin", OperatorType.PREFIX, 4, (operands) -> ComplexMath.asin(operands.get(0))),
                new Operator<>("acos", OperatorType.PREFIX, 4, (operands) -> ComplexMath.acos(operands.get(0))),
                new Operator<>("atan", OperatorType.PREFIX, 4, (operands) -> ComplexMath.atan(operands.get(0))),

                new Operator<>("sinh", OperatorType.PREFIX, 4, (operands) -> ComplexMath.sinh(operands.get(0))),
                new Operator<>("cosh", OperatorType.PREFIX, 4, (operands) -> ComplexMath.cosh(operands.get(0))),
                new Operator<>("tanh", OperatorType.PREFIX, 4, (operands) -> ComplexMath.tanh(operands.get(0))),

                new Operator<>("asinh", OperatorType.PREFIX, 4, (operands) -> ComplexMath.asinh(operands.get(0))),
                new Operator<>("acosh", OperatorType.PREFIX, 4, (operands) -> ComplexMath.acosh(operands.get(0))),
                new Operator<>("atanh", OperatorType.PREFIX, 4, (operands) -> ComplexMath.atanh(operands.get(0))),

                new Operator<>("ln", OperatorType.PREFIX, 4, (operands) -> ComplexMath.log(operands.get(0))),
                new Operator<>("log10", OperatorType.PREFIX, 4, (operands) -> ComplexMath.log10(operands.get(0))),
                new Function<>("log", 2, (operands) -> ComplexMath.log(operands.get(1), operands.get(0))),

                new Operator<>("sqrt", OperatorType.PREFIX, 4, (operands) -> ComplexMath.sqrt(operands.get(0))),
                new Operator<>("cbrt", OperatorType.PREFIX, 4, (operands) -> ComplexMath.cbrt(operands.get(0))),

                new Function<>("exp", 1, (operands) -> ComplexMath.exp(operands.get(0))),

                new Function<>("max", (operands) -> operands.isEmpty() ? Complex.ZERO : ComplexMath.max(operands)),
                new Function<>("min", (operands) -> operands.isEmpty() ? Complex.ZERO : ComplexMath.min(operands))
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
