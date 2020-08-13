package bagel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * The base class for all Bagel games.
 *
 * @author Eleanor McMurtry
 */
public abstract class AbstractGame {
    private final Input input;
    private final Window window;

    /**
     * Create the game with a default window size (1024x768) and title ("Game").
     */
    public AbstractGame() {
        this(1024, 768, "Game");
    }

    /**
     * Create the game with a default title ("Game").
     */
    public AbstractGame(int width, int height) {
        this(width, height, "Game");
    }

    /**
     * Create the game.
     */
    public AbstractGame(int width, int height, String title) {
        input = new Input();
        window = createWindow(width, height, title);
    }
    /**
     * Useful utility to restart the JVM automatically with -XstartOnFirstThread argument on MacOSX as required by GLFW.
     * Method originally written by <b>Kappa</b> on the Java-Gaming forums. This code was from shared code snippet which
     * was copied from http://www.java-gaming.org/topics/starting-jvm-on-mac-with-xstartonfirstthread-programmatically/37697/view.html
     *
     * Apple's fault. Sorry.
     */
    private static void checkForXstartOnFirstThread() {
        // get current jvm process pid
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        // get environment variable on whether XstartOnFirstThread is enabled
        String env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_" + pid);

        // if environment variable is "1" then XstartOnFirstThread is enabled
        if (env == null || !env.equals("1")) {
            // restart jvm with -XstartOnFirstThread
            String separator = System.getProperty("file.separator");
            String classpath = System.getProperty("java.class.path");
            String mainClass = System.getenv("JAVA_MAIN_CLASS_" + pid);
            String jvmPath = System.getProperty("java.home") + separator + "bin" + separator + "java";

            if (mainClass == null) {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                StackTraceElement main = stack[stack.length - 1];
                mainClass = main.getClassName();
            }

            List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

            List<String> jvmArgs = new ArrayList<>();

            jvmArgs.add(jvmPath);
            jvmArgs.add("-XstartOnFirstThread");
            jvmArgs.addAll(inputArguments);
            jvmArgs.add("-cp");
            jvmArgs.add(classpath);
            jvmArgs.add(mainClass);

            try {
                ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                String line;

                while ((line = br.readLine()) != null)
                    System.out.println(line);

                process.waitFor();
                System.exit(process.exitValue());
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.exit(-1);
        }
    }

    static {
        // Check that we won't fall apart on OS X
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            checkForXstartOnFirstThread();
        }

        if (!glfwInit()) {
            throw new BagelError("Failed to initialise LWJGL");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        // This one's important for Hi-DPI
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);

        System.out.println("Bagel v1.9.2 (May 20th, 2020)");
    }
    /**
     * Create the window.
     */
    private Window createWindow(int width, int height, String title) {
        Window window = new Window(width, height, title);
        window.setInputHandlers(input);

        return window;
    }

    /**
     * Start the game loop.
     */
    public final void run() {
        window.loop(this::step);
    }

    /**
     * Update the state of the game, potentially reading from input.
     */
    protected abstract void update(Input input);

    /**
     * Perform a single step of the game loop.
     */
    private void step() {
        update(input);
        input.updateState();
    }
}
