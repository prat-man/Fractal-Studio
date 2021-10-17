package in.pratanumandal.fractalstudio.expression;

import org.apache.commons.math3.complex.Complex;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.OperatorType;
import org.kobjects.expressionparser.Processor;
import org.kobjects.expressionparser.Tokenizer;

import java.util.HashMap;
import java.util.List;

public class ComplexProcessor extends Processor<Complex> {

    public final HashMap<String, Complex> variables = new HashMap<>();

    @Override
    public Complex infixOperator(Tokenizer tokenizer, String name, Complex left, Complex right) {
        switch (name) {
            case "+": return left.add(right);
            case "-": return left.subtract(right);
            case "*": return left.multiply(right);
            case "/": return left.divide(right);
            case "^": return left.pow(right);
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public Complex call(Tokenizer tokenizer, String identifier, String bracket, List<Complex> arguments) {
        switch (identifier) {
            case "sin": System.out.println("sine!");
                return arguments.get(0).sin();
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public Complex prefixOperator(Tokenizer tokenizer, String name, Complex argument) {
        switch (name) {
            case "-": return Complex.ZERO.subtract(argument);
            case "sin": return argument.sin();
            case "cos": return argument.cos();
            case "tan": return argument.tan();
            case "asin": return argument.asin();
            case "acos": return argument.acos();
            case "atan": return argument.atan();
            case "sinh": return argument.sinh();
            case "cosh": return argument.cosh();
            case "tanh": return argument.tanh();
            case "log": return argument.log();
            case "sqrt": return argument.sqrt();
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public Complex suffixOperator(Tokenizer tokenizer, String name, Complex argument) {
        switch (name) {
            case "i": return new Complex(0, argument.getReal());
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public Complex numberLiteral(Tokenizer tokenizer, String value) {
        return new Complex(Double.parseDouble(value), 0);
    }

    @Override
    public Complex identifier(Tokenizer tokenizer, String name) {
        Complex value = variables.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Undeclared variable: " + name);
        }
        return value;
    }

    @Override
    public Complex group(Tokenizer tokenizer, String paren, List<Complex> elements) {
        return elements.get(0);
    }

    /**
     * Creates a parser for this processor with matching operations and precedences set up.
     */
    public ExpressionParser<Complex> createParser() {
        variables.put("tau", new Complex(2 * Math.PI));
        variables.put("pi", new Complex(Math.PI));
        variables.put("e", new Complex(Math.E));

        ExpressionParser<Complex> parser = new ExpressionParser<>(this);
        parser.addCallBrackets("(", ",", ")");
        parser.addGroupBrackets("(", null, ")");

        parser.addOperators(OperatorType.SUFFIX, 6, "i");
        parser.addOperators(OperatorType.PREFIX, 5, "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "log", "sqrt");
        parser.addOperators(OperatorType.INFIX_RTL, 4, "^");
        parser.addOperators(OperatorType.PREFIX, 3, "+", "-");
        // 2 Reserved for implicit multiplication
        parser.addOperators(OperatorType.INFIX, 1, "*", "/");
        parser.addOperators(OperatorType.INFIX, 0, "+", "-");

        return parser;
    }

}
