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
    private static final double EPSILON = 0.000001;

    private Map<Complex, Color> colorMap;

    public NewtonRaphson(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void run() {
        this.colorMap = new HashMap<>();
        super.run();
    }

    private Complex function(Complex z) {
        return z.pow(3).subtract(1);
    }

    private Complex derivative(Complex z) {
        return function(z.add(EPSILON)).subtract(function(z.subtract(EPSILON))).divide(2 * EPSILON);
    }

    private Root newtonRaphson(Complex z) {
        double iteration = 0;
        Complex last = null;
        Complex f;

        while ((f = function(z)).abs() >= EPSILON && iteration <= MAX_ITERATIONS) {
            last = z;
            Complex h = function(z).divide(derivative(z));
            z = z.subtract(h);
            iteration++;
        }

        if (f.equals(Complex.NaN) || iteration > MAX_ITERATIONS) return null;

        if (last != null && this.isSmooth()) {
            Complex root = new Complex(FractalUtils.precision(z.getReal(), 7), FractalUtils.precision(z.getImaginary(), 7));

            double delta = (Math.log10(EPSILON) - Math.log10(Math.abs(last.subtract(root).abs()))) /
                    (Math.log10(Math.abs(z.subtract(root).abs())) - Math.log10(Math.abs(last.subtract(root).abs())));

            iteration += delta;
        }

        Complex root = new Complex(FractalUtils.precision(z.getReal(), 5), FractalUtils.precision(z.getImaginary(), 5));

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

        return Color.color(color.getRed(), color.getBlue(), color.getGreen(), getAlpha(root.iteration));
    }

    private Color generateColor() {
        if (this.isMonochrome()) return Color.WHITE;
        return Color.hsb(360.0 * Math.random(), 0.2 + 0.8 * Math.random(), 0.7 + 0.3 * Math.random());
    }

    private double getAlpha(double iteration) {
        if (this.isMonochrome()) {
            if (this.isInverted())
                return 1.0 - Math.max(Math.min(iteration / 30.0, 0.9), 0.0);

            return Math.max(Math.min(iteration / 30.0, 1.0), 0.1);
        }

        if (this.isInverted())
            return Math.max(Math.min(iteration / 30.0, 1.0), 0.1);

        return 1.0 - Math.max(Math.min(iteration / 30.0, 0.9), 0.0);
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
