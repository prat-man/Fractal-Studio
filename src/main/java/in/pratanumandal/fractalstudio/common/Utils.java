package in.pratanumandal.fractalstudio.common;

import in.pratanumandal.fractalstudio.gui.GUI;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

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

                URL iconURL = Utils.class.getClassLoader().getResource("img/icon-large.png");
                Image image = new ImageIcon(iconURL).getImage();

                setDockImage.invoke(object, image);
            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

}
