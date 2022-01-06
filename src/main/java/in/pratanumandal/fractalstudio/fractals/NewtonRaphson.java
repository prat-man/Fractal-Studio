package in.pratanumandal.fractalstudio.fractals;

import in.pratanumandal.expr4j.Expression;
import in.pratanumandal.fractalstudio.core.Fractal;
import in.pratanumandal.fractalstudio.core.FractalUtils;
import in.pratanumandal.fractalstudio.core.Point;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class NewtonRaphson extends Fractal {

    private static final double EPSILON = Math.pow(10, -6);

    private static final Color[] COLORS = {
            Color.rgb(250,  40,  70),
            Color.rgb(170, 240,  32),
            Color.rgb( 34, 161, 239),
            Color.rgb(250, 200,  80),
            Color.rgb(225, 155, 202),
            Color.rgb( 44, 237, 205),
            Color.rgb(255, 204, 204),
            Color.rgb(199, 199, 199)
    };

    public final Expression<Complex> expression;

    private Map<Complex, Color> colorMap;
    private Root[][] roots;

    private AtomicReference<Double> minIterations;
    private AtomicReference<Double> maxIterations;

    public NewtonRaphson(double size, Expression<Complex> expression) {
        super(size);
        this.expression = expression;
    }

    @Override
    public void run() {
        this.colorMap = new HashMap<>();
        this.roots = new Root[(int) this.getSize()][(int) this.getSize()];
        this.minIterations = new AtomicReference<>(Double.MAX_VALUE);
        this.maxIterations = new AtomicReference<>(0.0);

        super.run();

        this.colorMap = null;
        this.roots = null;
        this.minIterations = null;
        this.maxIterations = null;
        System.gc();
    }

    private Complex function(Complex z) {
        Map<String, Complex> variables = new HashMap<>();
        variables.put("z", z);

        return expression.evaluate(variables);
    }

    private Complex derivative(Complex z) {
        return function(z.add(EPSILON)).subtract(function(z.subtract(EPSILON))).divide(2 * EPSILON);
    }

    private Root newtonRaphson(Complex z) {
        double iteration = 0;
        Complex last = z;
        Complex secondLast;

        do {
            secondLast = last;
            last = z;
            Complex h = function(z).divide(derivative(z));
            z = z.subtract(h);
            iteration++;
        }
        while (z.subtract(last).abs() > EPSILON && iteration <= this.getIterationLimit());

        if (function(z).equals(Complex.NaN) || iteration > this.getIterationLimit()) return null;

        if (last != null && secondLast != null && this.isSmooth() && iteration != 1) {
            double prevR = Math.log10(last.subtract(secondLast).abs());
            double delta = (Math.log10(EPSILON) - prevR) / (Math.log10(z.subtract(last).abs()) - prevR);
            iteration += Math.max(Math.min(delta, 1), 0);
        }

        Complex root = new Complex(FractalUtils.precision(z.getReal(), ((int) -Math.log10(EPSILON)) - 1), FractalUtils.precision(z.getImaginary(), ((int) -Math.log10(EPSILON)) - 1));

        return new Root(root, iteration);
    }

    @Override
    public void compute(Point point, Complex z) {
        Root root = this.newtonRaphson(z);

        if (root == null) return;

        this.roots[(int) point.x][(int) point.y] = root;

        synchronized (colorMap) {
            if (!colorMap.containsKey(root.root)) {
                Color color = generateColor();
                colorMap.put(root.root, color);
            }
        }

        minIterations.getAndUpdate(min -> min <= root.iteration ? min : root.iteration);
        maxIterations.getAndUpdate(max -> max >= root.iteration ? max : root.iteration);
    }

    @Override
    public Color getColor(Point point) {
        Root root = this.roots[(int) point.x][(int) point.y];
        if (root == null) return null;
        Color color = colorMap.get(root.root);
        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), getAlpha(root.iteration));
    }

    private Color generateColor() {
        if (this.isMonochrome()) return Color.WHITE;
        if (this.colorMap.size() < COLORS.length) return COLORS[this.colorMap.size()];
        return Color.hsb(360.0 * Math.random(), 0.2 + 0.8 * Math.random(), 0.7 + 0.3 * Math.random());
    }

    private double getAlpha(double iteration) {
        double maxValue = 20.0 + maxIterations.get() - minIterations.get();

        double iterValue = iteration - minIterations.get();
        iterValue += iterValue < 10 ? 2.0 * iterValue : 20.0;

        double alpha = this.isInverted() ? iterValue / maxValue : 1.0 - iterValue / maxValue;
        return Math.max(Math.min(alpha, 1.0), 0.0);
    }

    class Root {

        public Complex root;
        public double iteration;

        public Root(Complex root, double iteration) {
            this.root = root;
            this.iteration = iteration;
        }

    }

}
