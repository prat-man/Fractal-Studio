package in.pratanumandal.fractalstudio.mandelbrot;

import in.pratanumandal.fractalstudio.core.Fractal;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;

import java.util.HashMap;
import java.util.Map;

public class Mandelbrot extends Fractal {

    private Map<Complex, Color> colorMap;

    public Mandelbrot(Canvas canvas) {
        super(canvas);
        this.colorMap = new HashMap<>();
    }

    private Double mandlebrot(Complex c) {
        double iteration = 0;
        Complex last = null;
        Complex z = Complex.ZERO;

        while (z.abs() <= 2.0 && iteration <= 100) {
            last = z;
            z = z.multiply(z).add(c);
            iteration++;
        }

        if (z.equals(Complex.NaN) || iteration > 100) return null;

        if (last != null && this.isSmooth()) {
            double delta = 1 - Math.log(Math.log(z.abs())) / Math.log(2);
            iteration += delta;
        }

        return iteration;
    }

    @Override
    public Color getColor(Complex c) {
        Double iteration = this.mandlebrot(c);
        if (iteration == null) return Color.BLACK;

        if (this.isInverted() && this.isMonochrome())
            return Color.hsb(0.0, 0.0, 1.0, Math.min(iteration / 100.0, 1.0));

        if (this.isInverted())
            return Color.hsb(50.0 + 200.0 * (1.0 - iteration / 100.0), 0.7, 0.7);

        return Color.hsb(360.0 * (iteration / 100.0), 0.7, 0.7);
    }

}
