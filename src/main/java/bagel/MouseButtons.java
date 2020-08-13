package bagel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Represents all keyboard keys that are recognised by BAGEL.
 *
 * @author Eleanor McMurtry
 */
public enum MouseButtons {
    LEFT(GLFW_MOUSE_BUTTON_LEFT),
    MIDDLE(GLFW_MOUSE_BUTTON_MIDDLE),
    RIGHT(GLFW_MOUSE_BUTTON_RIGHT);

    final int code;

    private static final Map<Integer, MouseButtons> reverseLookup = new HashMap<>();

    /*
     * Set up the reverse lookup map.
     */
    static {
        for (MouseButtons key : MouseButtons.values()) {
            reverseLookup.put(key.code, key);
        }
    }

    /**
     * Returns the key that matches the given GLFW code, or empty() if there is no such key.
     */
    static Optional<MouseButtons> fromCode(int code) {
        return Optional.ofNullable(reverseLookup.get(code));
    }

    MouseButtons(int code) {
        this.code = code;
    }
}
