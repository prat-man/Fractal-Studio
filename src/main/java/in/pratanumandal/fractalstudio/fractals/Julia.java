package in.pratanumandal.fractalstudio.fractals;

import in.pratanumandal.fractalstudio.core.Fractal;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;

public class Julia extends Fractal {

    private Complex c;

    public Julia(Canvas canvas, Complex c) {
        super(canvas);
        this.c = c;
    }

    private Double julia(Complex z) {
        double iteration = 0;
        Complex last = null;

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
    public Color getColor(Complex z) {
        Double iteration = this.julia(z);
        if (iteration == null) return Color.BLACK;

        if (this.isInverted() && this.isMonochrome())
            return Color.hsb(0.0, 0.0, 1.0, Math.min(iteration / 100.0, 1.0));

        if (this.isInverted())
            return Color.hsb(50.0 + 200.0 * (1.0 - iteration / 100.0), 0.7, 0.7);

        return Color.hsb(360.0 * (iteration / 100.0), 0.7, 0.7);
    }

}
