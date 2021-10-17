package in.pratanumandal.fractalstudio.fractals;

import in.pratanumandal.fractalstudio.core.Fractal;
import in.pratanumandal.fractalstudio.expression.ComplexProcessor;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.apache.commons.math3.complex.Complex;
import org.kobjects.expressionparser.ExpressionParser;

public class Julia extends Fractal {

    private static final double MAX_ITERATIONS = 100.0;

    private static final Color[] COLORS = {
            Color.rgb(66, 30, 15),
            Color.rgb(25, 7, 26),
            Color.rgb(9, 1, 47),
            Color.rgb(4, 4, 73),
            Color.rgb(0,7, 100),
            Color.rgb(12,44,138),
            Color.rgb(24,82,177),
            Color.rgb(57,125,209),
            Color.rgb(134,181,229),
            Color.rgb(211,236,248),
            Color.rgb(241,233,191),
            Color.rgb(248,201,95),
            Color.rgb(255,170,0),
            Color.rgb(204,128,0),
            Color.rgb(153,87,0),
            Color.rgb(106,52,3)
    };

    private static final Color[] BASE_COLORS = {
            Color.rgb(0,7, 100),
            Color.rgb(12,44,138),
            Color.rgb(24,82,177),
            Color.rgb(134,181,229),
            Color.rgb(211,236,248),
            Color.rgb(241,233,191),
            Color.rgb(248,201,95),
            Color.rgb(255,170,0),
            Color.rgb(204,128,0),
            Color.rgb(153,87,0),
            Color.rgb(106,52,3)
    };

    private static final Color[] SMOOTH_COLORS = new Color[1000];

    static {
        for (int i = 0; i < BASE_COLORS.length - 1; i++) {
            SMOOTH_COLORS[i * 100] = BASE_COLORS[i];
            Color primary = BASE_COLORS[i];
            Color secondary = BASE_COLORS[i + 1];
            for (int j = 1; j < 100; j++) {
                SMOOTH_COLORS[i * 100 + j] = Color.color(
                        primary.getRed() * (1.0 - j / 99.0) + secondary.getRed() * (j / 99.0),
                        primary.getGreen() * (1.0 - j / 99.0) + secondary.getGreen() * (j / 99.0),
                        primary.getBlue() * (1.0 - j / 99.0) + secondary.getBlue() * (j / 99.0));
            }
        }
    }

    public final String function;

    public Julia(Canvas canvas, String function) {
        super(canvas);
        this.function = function;
    }

    private Complex function(Complex z) {
        ComplexProcessor processor = new ComplexProcessor();
        processor.variables.put("z", z);

        ExpressionParser<Complex> parser = processor.createParser();
        return parser.parse(this.function);
    }

    private Double julia(Complex z) {
        double iteration = 0;
        Complex last = null;

        while (z.abs() < 2.0 && iteration < MAX_ITERATIONS) {
            last = z;
            z = function(z);
            iteration++;
        }

        if (z.equals(Complex.NaN) || iteration == MAX_ITERATIONS) return null;

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

        if (this.isMonochrome()) {
            if (this.isInverted())
                return Color.hsb(0.0, 0.0, Math.max(1.0 - iteration / MAX_ITERATIONS, 0.0));

            return Color.hsb(0.0, 0.0, Math.min(iteration / MAX_ITERATIONS, 1.0));
        }

        if (this.isInverted()) {
            if (this.isSmooth())
                return SMOOTH_COLORS[Math.max(SMOOTH_COLORS.length - (int) (iteration * 10) - 1, 0)];

            return COLORS[COLORS.length - (int) (iteration % COLORS.length) - 1];
        }

        if (this.isSmooth())
            return SMOOTH_COLORS[Math.min((int) (iteration * 10), SMOOTH_COLORS.length - 1)];

        return COLORS[(int) (iteration % COLORS.length)];
    }

}
