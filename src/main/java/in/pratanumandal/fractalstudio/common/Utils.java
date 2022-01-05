package in.pratanumandal.fractalstudio.common;

import in.pratanumandal.expr4j.Expression;
import in.pratanumandal.fractalstudio.core.Fractal;
import in.pratanumandal.fractalstudio.expression.ComplexParser;
import in.pratanumandal.fractalstudio.fractals.BurningShip;
import in.pratanumandal.fractalstudio.fractals.Julia;
import in.pratanumandal.fractalstudio.fractals.Mandelbrot;
import in.pratanumandal.fractalstudio.fractals.NewtonRaphson;
import in.pratanumandal.fractalstudio.gui.GUI;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.math3.complex.Complex;

import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Optional;

public class Utils {

    public static void browseURL(String url) {
        GUI.hostServices.showDocument(url);
    }

    public static void setDockIconIfMac() {
        if (SystemUtils.IS_OS_MAC) {
            try {
                Class clazz = Utils.class.getClassLoader().loadClass("com.apple.eawt.Application");
                Method getApplication = clazz.getMethod("getApplication");
                Object object = getApplication.invoke(null);
                Method setDockImage = clazz.cast(object).getClass().getMethod("setDockIconImage", Image.class);

                URL iconURL = Utils.class.getClassLoader().getResource("img/icon.png");
                Image image = new ImageIcon(iconURL).getImage();

                setDockImage.invoke(object, image);
            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static Alert setDefaultButton(Alert alert, ButtonType defBtn) {
        DialogPane pane = alert.getDialogPane();
        for (ButtonType t : alert.getButtonTypes()) {
            ((Button) pane.lookupButton(t)).setDefaultButton(t == defBtn);
        }
        return alert;
    }

    public static void saveFractal(Fractal fractal, File file) throws JAXBException {
        FractalFile fractalFile = new FractalFile();

        if (fractal instanceof Mandelbrot) {
            fractalFile.setType(FractalFile.Type.MANDELBROT);
        }
        else if (fractal instanceof BurningShip) {
            fractalFile.setType(FractalFile.Type.BURNING_SHIP);
        }
        else if (fractal instanceof Julia) {
            fractalFile.setType(FractalFile.Type.JULIA);
            fractalFile.setFunction(((Julia) fractal).expression.toString());
        }
        else if (fractal instanceof NewtonRaphson) {
            fractalFile.setType(FractalFile.Type.NEWTON_RAPHSON);
            fractalFile.setFunction(((NewtonRaphson) fractal).expression.toString());
        }

        fractalFile.setSmooth(fractal.isSmooth());
        fractalFile.setInverted(fractal.isInverted());
        fractalFile.setMonochrome(fractal.isMonochrome());
        fractalFile.setCenter(fractal.getCenter());
        fractalFile.setScale(fractal.getScale());
        fractalFile.setZoom(fractal.getZoom());
        fractalFile.setIterationLimit(fractal.getIterationLimit());

        JAXBContext jaxbContext = JAXBContext.newInstance(FractalFile.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(fractalFile, file);
    }

    public static Fractal loadFractal(File file) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(FractalFile.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        FractalFile fractalFile = (FractalFile) jaxbUnmarshaller.unmarshal(file);

        Fractal fractal = null;

        ComplexParser parser = new ComplexParser();
        Expression<Complex> expression = null;

        switch (fractalFile.getType()) {
            case MANDELBROT:
                fractal = new Mandelbrot(Configuration.getCanvasSize());
                break;

            case BURNING_SHIP:
                fractal = new BurningShip(Configuration.getCanvasSize());
                break;

            case JULIA:
                expression = parser.parse(fractalFile.getFunction());
                fractal = new Julia(Configuration.getCanvasSize(), expression);
                break;

            case NEWTON_RAPHSON:
                expression = parser.parse(fractalFile.getFunction());
                fractal = new NewtonRaphson(Configuration.getCanvasSize(), expression);
                break;
        }

        fractal.setSmooth(fractalFile.isSmooth());
        fractal.setInverted(fractalFile.isInverted());
        fractal.setMonochrome(fractalFile.isMonochrome());
        fractal.setCenter(fractalFile.getCenter());
        fractal.setScale(fractalFile.getScale());
        fractal.setZoom(fractalFile.getZoom());
        fractal.setIterationLimit(fractalFile.getIterationLimit());

        return fractal;
    }

    public static Optional<ButtonType> showAndWait(Alert alert) {
        if (Utils.isKDE()) {
            alert.setResizable(true);

            alert.setOnShowing(event -> {
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    Platform.runLater(() -> {
                        alert.setResizable(false);

                        if (alert.getOwner() != null) {
                            alert.getOwner().requestFocus();
                        }
                    });
                });

                thread.start();
            });
        }

        return alert.showAndWait();
    }

    public static void showAndWait(Dialog dialog) {
        if (Utils.isKDE()) {
            dialog.setResizable(true);

            dialog.setOnShowing(event -> {
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }

                    Platform.runLater(() -> {
                        dialog.setResizable(false);

                        if (dialog.getOwner() != null) {
                            dialog.getOwner().requestFocus();
                        }
                    });
                });

                thread.start();
            });
        }

        dialog.showAndWait();
    }

    public static boolean isKDE() {
        return System.getenv("KDE_SESSION_VERSION") != null;
    }

}
