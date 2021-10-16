package in.pratanumandal.fractalstudio.fractals;

import in.pratanumandal.fractalstudio.core.Fractal;
import in.pratanumandal.fractalstudio.core.FractalUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;

import java.util.HashMap;
import java.util.Map;

public class NewtonRaphson extends Fractal {

    private static final double MAX_ITERATIONS = 100.0;
    private static final double EPSILON = Math.pow(10, -6);

    private static final Color[] COLORS = {
            Color.RED,
            Color.rgb(57,125,209),
            Color.GREEN,
            Color.rgb(255,170,0)
    };

    private Map<Complex, Color> colorMap;
    private double scalingFactor;

    public NewtonRaphson(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void run() {
        this.colorMap = new HashMap<>();
        this.scalingFactor = Math.log(this.getScale() > 1 ? this.getScale() : 1.0 / this.getScale()) / Math.log(2);
        super.run();
    }

    private Complex function(Complex z) {
        //return z.pow(8).add(z.pow(4).multiply(15)).subtract(16);
        return z.pow(3).subtract(1);
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
        while (z.subtract(last).abs() > EPSILON && iteration < MAX_ITERATIONS);

        if (function(z).equals(Complex.NaN) || iteration > MAX_ITERATIONS) return null;

        if (last != null && secondLast != null && this.isSmooth()) {
            double prevR = Math.log10(last.subtract(secondLast).abs());
            double delta = (Math.log10(EPSILON) - prevR) / (Math.log10(z.subtract(last).abs()) - prevR);
            iteration += Math.max(Math.min(delta, 1), 0);
        }

        Complex root = new Complex(FractalUtils.precision(z.getReal(), ((int) -Math.log10(EPSILON)) - 1), FractalUtils.precision(z.getImaginary(), ((int) -Math.log10(EPSILON)) - 1));

        return new Root(root, iteration);
    }

    @Override
    public Color getColor(Complex z) {
        Root root = this.newtonRaphson(z);

        if (root == null) return null;

        Color color;
        synchronized (colorMap) {
            if (colorMap.containsKey(root.root)) {
                color = colorMap.get(root.root);
            } else {
                color = generateColor();
                colorMap.put(root.root, color);
            }
        }

        return Color.color(color.getRed(), color.getGreen(), color.getBlue(), getAlpha(root.iteration));
    }

    private Color generateColor() {
        if (this.isMonochrome()) return Color.WHITE;
        if (this.colorMap.size() < COLORS.length) return COLORS[this.colorMap.size()];
        return Color.hsb(360.0 * Math.random(), 0.2 + 0.8 * Math.random(), 0.7 + 0.3 * Math.random());
    }

    private double getAlpha(double iteration) {
        double scaledIter = iteration - this.scalingFactor;

        if (this.isMonochrome()) {
            if (this.isInverted())
                return Math.max(Math.min(1.0 - scaledIter / 30.0, 1.0), 0.0);

            return Math.max(Math.min(scaledIter / 30.0, 1.0), 0.0);
        }

        if (this.isInverted())
            return Math.max(Math.min(scaledIter / 30.0, 1.0), 0.0);

        return Math.max(Math.min(1.0 - scaledIter / 30.0, 1.0), 0.0);
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
