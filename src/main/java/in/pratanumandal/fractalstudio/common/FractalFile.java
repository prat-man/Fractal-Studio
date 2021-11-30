package in.pratanumandal.fractalstudio.common;

import in.pratanumandal.fractalstudio.core.Point;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="fractal")
@XmlRootElement(name="fractal")
@XmlAccessorType(XmlAccessType.FIELD)
public class FractalFile {

    private Type type;
    private String function;
    private boolean smooth;
    private boolean inverted;
    private boolean monochrome;
    private Point center;
    private double scale;
    private double zoom;
    private double iterationLimit;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public boolean isSmooth() {
        return smooth;
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public boolean isMonochrome() {
        return monochrome;
    }

    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public double getIterationLimit() {
        return iterationLimit;
    }

    public void setIterationLimit(double iterationLimit) {
        this.iterationLimit = iterationLimit;
    }

    public enum Type {
        MANDELBROT,
        BURNING_SHIP,
        JULIA,
        NEWTON_RAPHSON
    }

}
