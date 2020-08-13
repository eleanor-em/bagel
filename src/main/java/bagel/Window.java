package bagel;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import java.util.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Represents the window the game is displayed in.
 *
 * @author Eleanor McMurtry
 */
public class Window {
    private final long hWindow;
    private final int width;
    private final int height;

    private static float rClear = 0.4f;
    private static float gClear = 0.6f;
    private static float bClear = 0.9f;

    private List<RenderInfo> renderQueue = new ArrayList<>();

    private static final List<Runnable> actionsOnLoad = new ArrayList<>();
    static void onLoad(Runnable r) {
        actionsOnLoad.add(r);
    }

    // It makes perfect sense to use an Optional as a field type. I don't care if Oracle says it doesn't.
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Optional<Window> currentWindow = Optional.empty();

    /**
     * Get the singleton instance.
     */
    static Window get() {
        return currentWindow.orElseThrow(() -> new BagelError("Must create window first"));
    }

    /**
     * Find the width of the window.
     */
    public static int getWidth() {
        return get().width;
    }

    /**
     * Find the height of the window.
     */
    public static int getHeight() {
        return get().height;
    }

    /**
     * Closes the current window, if one is open.
     */
    public static void close() {
        currentWindow.ifPresent(Window::closeWindow);
    }

    /**
     * Set the colour the screen is cleared to each frame.
     * Arguments are floats where 0 is entirely dark in the channel and 1 is entirely bright in the channel.
     */
    public static void setClearColour(float r, float g, float b) {
        rClear = r;
        gClear = g;
        bClear = b;
    }

    /**
     * Overload of {@link #setClearColour(float, float, float)} to easily support double literals.
     */
    public static void setClearColour(double r, double g, double b) {
        setClearColour((float) r, (float) g, (float) b);
    }

    /**
     * Removes the throttle on the game's FPS. Should be used to work around certain platforms that have driver bugs
     * resulting in poor performance.
     */
    public static void removeFrameThrottle() {
        glfwSwapInterval(0);
    }

    /**
     * Close this window.
     */
    private void closeWindow() {
        glfwSetWindowShouldClose(hWindow, true);
    }

    /**
     * Create a window with the specified attributes.
     */
    Window(int width, int height, String title) {
        this.width = width;
        this.height = height;

        close();
        currentWindow = Optional.of(this);

        hWindow = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (hWindow == MemoryUtil.NULL) {
            throw new BagelError("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(hWindow);

        GL.createCapabilities();
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        show();

        for (Runnable r : actionsOnLoad) {
            r.run();
        }
    }

    /**
     * Set input handlers for this window.
     */
    void setInputHandlers(Input input) {
        glfwSetKeyCallback(hWindow, (window, key, scancode, action, mode) -> input.keyboardEvent(key, action));
        glfwSetCursorPosCallback(hWindow, input::cursorEvent);
        glfwSetMouseButtonCallback(hWindow, (window, button, action, mods) -> input.mouseEvent(button, action));
    }

    /**
     * Shows the window on-screen.
     */
    private void show() {
        glfwSwapInterval(1);
        glfwShowWindow(hWindow);
    }

    /**
     * Submit a rendering job to the queue with a specified texture and details.
     */
    void submitRenderJob(RenderInfo info) {
        renderQueue.add(info);
    }

    /**
     * Runs the main loop for the window.
     */
    void loop(Runnable action) {
        glClearColor(rClear, gClear, bClear, 1.0f);

        long time = System.nanoTime();
        while (!glfwWindowShouldClose(hWindow)) {
            glfwPollEvents();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            action.run();

            renderQueue.forEach(RenderInfo::render);
            renderQueue.clear();

            glfwSwapBuffers(hWindow);

//            long now = System.nanoTime();
//            System.out.format("update: %.2f\n", 1000.0 / (((double) (now - time)) / 1e6));
//            time = System.nanoTime();
        }

        // Clean up
        glfwFreeCallbacks(hWindow);
        glfwDestroyWindow(hWindow);
        TextureManager.destroy();

        glfwTerminate();
        Optional.ofNullable(glfwSetErrorCallback(null))
                .ifPresent(GLFWErrorCallback::free);
    }
}
