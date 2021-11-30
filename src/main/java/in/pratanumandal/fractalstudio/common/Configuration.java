package in.pratanumandal.fractalstudio.common;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.File;
import java.nio.file.Files;

@XmlType(name = "fractal-studio")
@XmlRootElement(name = "fractal-studio")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {

    private static Configuration instance;

    @XmlElement(name = "canvas-size")
    private Integer canvasSize;

    @XmlElement(name = "thread-count")
    private Integer threadCount;

    @XmlElement(name = "mode")
    private Mode mode;

    @XmlElement(name = "directory")
    private File directory;

    private static synchronized Configuration getInstance() {
        if (instance == null) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                try {
                    instance = (Configuration) jaxbUnmarshaller.unmarshal(Constants.CONFIGURATION_FILE);
                    instance.marshal();
                } catch (Exception e) {
                    Files.createDirectories(Constants.CONFIGURATION_FILE.toPath().getParent());
                    instance = new Configuration();
                    instance.marshal();
                }
            } catch (Exception e) {
                e.printStackTrace();

                instance = new Configuration();
                instance.sanitize();
            }
        }

        return instance;
    }

    public static Integer getCanvasSize() {
        Configuration configuration = Configuration.getInstance();
        return configuration.canvasSize;
    }

    public static void setCanvasSize(Integer canvasSize) {
        Configuration configuration = Configuration.getInstance();
        configuration.canvasSize = canvasSize;
        configuration.marshal();
    }

    public static Integer getThreadCount() {
        Configuration configuration = Configuration.getInstance();
        return configuration.threadCount;
    }

    public static void setThreadCount(Integer threadCount) {
        Configuration configuration = Configuration.getInstance();
        configuration.threadCount = threadCount;
        configuration.marshal();
    }

    public static Mode getMode() {
        Configuration configuration = Configuration.getInstance();
        return configuration.mode;
    }

    public static void setMode(Mode mode) {
        Configuration configuration = Configuration.getInstance();
        configuration.mode = mode;
        configuration.marshal();
    }

    public static File getDirectory() {
        Configuration configuration = Configuration.getInstance();
        return configuration.directory;
    }

    public static void setDirectory(File directory) {
        Configuration configuration = Configuration.getInstance();
        configuration.directory = directory;
        configuration.marshal();
    }

    private void sanitize() {
        if (this.canvasSize == null || this.canvasSize < 200 || this.canvasSize > 5000) {
            this.canvasSize = 500;
        }

        if (this.threadCount == null || this.threadCount < 1 || this.threadCount > 64) {
            this.threadCount = 4;
        }

        if (this.mode == null) {
            this.mode = Mode.BALANCED;
        }

        if (this.directory == null) {
            this.directory = new File(System.getProperty("user.home"));
        }
    }

    private void marshal() {
        this.sanitize();

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            jaxbMarshaller.marshal(this, Constants.CONFIGURATION_FILE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
