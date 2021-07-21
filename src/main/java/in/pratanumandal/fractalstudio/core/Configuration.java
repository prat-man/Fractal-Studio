package in.pratanumandal.fractalstudio.core;

public class Configuration {

    private static int threadCount = 4;

    public static int getThreadCount() {
        return Configuration.threadCount;
    }

    public static void setThreadCount(int threadCount) {
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Thread count must be greater than zero");
        }
        Configuration.threadCount = threadCount;
    }

    private static int CANVAS_SIZE = 500;

    public static int getCanvasSize() {
        return CANVAS_SIZE;
    }

    public static void setCanvasSize(int canvasSize) {
        if (canvasSize < 200 || canvasSize > 5000) {
            throw new IllegalArgumentException("Thread count must be between 200 and 1000");
        }
        CANVAS_SIZE = canvasSize;
    }

}
