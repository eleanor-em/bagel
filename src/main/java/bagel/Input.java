package bagel;

import bagel.util.Point;
import bagel.util.Vector2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.glfw.GLFW.*;

/**
 * This class provides access to input devices (keyboard and mouse).
 *
 * @author Eleanor McMurtry
 */
public class Input {
    private static final Map<Keys, ButtonState> keyStates = new HashMap<>();
    private static final Map<MouseButtons, ButtonState> mouseStates = new HashMap<>();

    private double lastMouseX;
    private double lastMouseY;
    private double xScaling = 0;
    private double yScaling = 0;

    static {
        for (Keys key : Keys.values()) {
            keyStates.put(key, ButtonState.UP);
        }
    }

    /**
     * Package-private as the instantiation of this class is handled by {@link AbstractGame}.
     */
    Input() {}

    /**
     * Called when a keyboard event occurs.
     */
    void keyboardEvent(int key, int action) {
        Keys.fromCode(key)
            .ifPresent(keyObj -> {
                if (action == GLFW_PRESS) {
                    keyStates.put(keyObj, ButtonState.PRESSED);
                } else if (action == GLFW_RELEASE) {
                    keyStates.put(keyObj, ButtonState.RELEASED);
                }
            });
    }

    void cursorEvent(long hWindow, double x, double y) {
        if (xScaling == 0) {
            // Need to check framebuffer size for hi-DPI screens
            int[] fw = new int[1];
            int[] fh = new int[1];
            glfwGetFramebufferSize(hWindow, fw, fh);

            // Sometimes, the information ends up here instead
            int[] ww = new int[1];
            int[] wh = new int[1];
            glfwGetWindowSize(hWindow, ww, wh);

            // I think usually fw[0] == fh[0], but best to be safe
            xScaling = fw[0] / ((double) Window.getWidth()) * (((double)ww[0]) / fw[0]);
            yScaling = fh[0] / ((double) Window.getHeight()) * (((double)wh[0]) / fh[0]);
        }

        lastMouseX = x / xScaling;
        lastMouseY = y / yScaling;
    }

    void mouseEvent(int button, int action) {
        MouseButtons.fromCode(button)
                .ifPresent(buttonObj -> {
                    if (action == GLFW_PRESS) {
                        mouseStates.put(buttonObj, ButtonState.PRESSED);
                    } else if (action == GLFW_RELEASE) {
                        mouseStates.put(buttonObj, ButtonState.RELEASED);
                    }
                });
    }

    /**
     * Updates the input state; this changes from "freshly changed" to a continuous state.
     */
    void updateState() {
        keyStates.forEach((key, state) -> {
            if (state == ButtonState.PRESSED) {
                keyStates.put(key, ButtonState.DOWN);
            } else if (state == ButtonState.RELEASED) {
                keyStates.put(key, ButtonState.UP);
            }
        });
        mouseStates.forEach((button, state) -> {
            if (state == ButtonState.PRESSED) {
                mouseStates.put(button, ButtonState.DOWN);
            } else if (state == ButtonState.RELEASED) {
                mouseStates.put(button, ButtonState.UP);
            }
        });
    }

    /**
     * Returns a unit vector pointing towards the mouse from the provided point.
     */
    public Vector2 directionToMouse(Point from) {
        return getMousePosition().asVector().sub(from.asVector()).normalised();
    }

    /**
     * Returns the mouse position as a {@link Point}.
     */
    public Point getMousePosition() {
        return new Point(getMouseX(), getMouseY());
    }

    /**
     * Returns the x coordinate of the mouse cursor.
     */
    public double getMouseX() {
        return lastMouseX;
    }

    /**
     * Returns the y coordinate of the mouse cursor.
     */
    public double getMouseY() {
        return lastMouseY;
    }

    /**
     * Checks whether the given key was pressed in the most recent update.
     * @param key the key to check
     * @return whether the key was just pressed by the user
     */
    public boolean wasPressed(Keys key) {
        return keyStates.get(key) == ButtonState.PRESSED;
    }

    /**
     * Checks whether the given key is being held down.
     * @param key the key to check
     * @return whether the user is holding the key down
     */
    public boolean isDown(Keys key) {
        return wasPressed(key) || keyStates.get(key) == ButtonState.DOWN;
    }

    /**
     * Checks whether the given key was released in the most recent update.
     * @param key the key to check
     * @return whether the key was just released by the user
     */
    public boolean wasReleased(Keys key) {
        return keyStates.get(key) == ButtonState.RELEASED;
    }

    /**
     * Checks whether the given key is not being held down down.
     * @param key the key to check
     * @return whether the user is not holding the key down
     */
    public boolean isUp(Keys key) {
        return wasReleased(key) || keyStates.get(key) == ButtonState.UP;
    }
    
    /**
     * Checks whether the given mouse button was pressed in the most recent update.
     * @param button the mouse button to check
     * @return whether the mouse button was just pressed by the user
     */
    public boolean wasPressed(MouseButtons button) {
        return mouseStates.get(button) == ButtonState.PRESSED;
    }

    /**
     * Checks whether the given mouse button is being held down.
     * @param button the mouse button to check
     * @return whether the user is holding the mouse button down
     */
    public boolean isDown(MouseButtons button) {
        return wasPressed(button) || mouseStates.get(button) == ButtonState.DOWN;
    }

    /**
     * Checks whether the given button was released in the most recent update.
     * @param button the button to check
     * @return whether the button was just released by the user
     */
    public boolean wasReleased(MouseButtons button) {
        return mouseStates.get(button) == ButtonState.RELEASED;
    }

    /**
     * Checks whether the given mouse button is not being held down down.
     * @param button the mouse button to check
     * @return whether the user is not holding the mouse button down
     */
    public boolean isUp(MouseButtons button) {
        return wasReleased(button) || mouseStates.get(button) == ButtonState.UP;
    }
}

/**
 * The states a key can be in.
 *
 * @author Eleanor McMurtry
 */
enum ButtonState {
    PRESSED,
    DOWN,
    RELEASED,
    UP
}