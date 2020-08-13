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
public enum Keys {
    A(GLFW_KEY_A),
    B(GLFW_KEY_B),
    C(GLFW_KEY_C),
    D(GLFW_KEY_D),
    E(GLFW_KEY_E),
    F(GLFW_KEY_F),
    G(GLFW_KEY_G),
    H(GLFW_KEY_H),
    I(GLFW_KEY_I),
    J(GLFW_KEY_J),
    K(GLFW_KEY_K),
    L(GLFW_KEY_L),
    M(GLFW_KEY_M),
    N(GLFW_KEY_N),
    O(GLFW_KEY_O),
    P(GLFW_KEY_P),
    Q(GLFW_KEY_Q),
    R(GLFW_KEY_R),
    S(GLFW_KEY_S),
    T(GLFW_KEY_T),
    U(GLFW_KEY_U),
    V(GLFW_KEY_V),
    W(GLFW_KEY_W),
    X(GLFW_KEY_X),
    Y(GLFW_KEY_Y),
    Z(GLFW_KEY_Z),
    ESCAPE(GLFW_KEY_ESCAPE),
    F1(GLFW_KEY_F1),
    F2(GLFW_KEY_F2),
    F3(GLFW_KEY_F3),
    F4(GLFW_KEY_F4),
    F5(GLFW_KEY_F5),
    F6(GLFW_KEY_F6),
    F7(GLFW_KEY_F7),
    F8(GLFW_KEY_F8),
    F9(GLFW_KEY_F9),
    F10(GLFW_KEY_F10),
    F11(GLFW_KEY_F11),
    F12(GLFW_KEY_F12),
    PRINT_SCREEN(GLFW_KEY_PRINT_SCREEN),
    INSERT(GLFW_KEY_INSERT),
    DELETE(GLFW_KEY_DELETE),
    BACKTICK(GLFW_KEY_GRAVE_ACCENT),
    NUM_0(GLFW_KEY_0),
    NUM_1(GLFW_KEY_1),
    NUM_2(GLFW_KEY_2),
    NUM_3(GLFW_KEY_3),
    NUM_4(GLFW_KEY_4),
    NUM_5(GLFW_KEY_5),
    NUM_6(GLFW_KEY_6),
    NUM_7(GLFW_KEY_7),
    NUM_8(GLFW_KEY_8),
    NUM_9(GLFW_KEY_9),
    MINUS(GLFW_KEY_MINUS),
    EQUALS(GLFW_KEY_EQUAL),
    BACKSPACE(GLFW_KEY_BACKSPACE),
    TAB(GLFW_KEY_TAB),
    BACKSLASH(GLFW_KEY_BACKSLASH),
    CAPS_LOCK(GLFW_KEY_CAPS_LOCK),
    SEMICOLON(GLFW_KEY_SEMICOLON),
    APOSTROPHE(GLFW_KEY_APOSTROPHE),
    ENTER(GLFW_KEY_ENTER),
    LEFT_SHIFT(GLFW_KEY_LEFT_SHIFT),
    COMMA(GLFW_KEY_COMMA),
    PERIOD(GLFW_KEY_PERIOD),
    SLASH(GLFW_KEY_SLASH),
    RIGHT_SHIFT(GLFW_KEY_RIGHT_SHIFT),
    LEFT_CTRL(GLFW_KEY_LEFT_CONTROL),
    LEFT_ALT(GLFW_KEY_LEFT_ALT),
    SPACE(GLFW_KEY_SPACE),
    RIGHT_CTRL(GLFW_KEY_RIGHT_CONTROL),
    LEFT(GLFW_KEY_LEFT),
    RIGHT(GLFW_KEY_RIGHT),
    DOWN(GLFW_KEY_DOWN),
    UP(GLFW_KEY_UP);

    final int code;

    private static final Map<Integer, Keys> reverseLookup = new HashMap<>();

    /*
     * Set up the reverse lookup map.
     */
    static {
        for (Keys key : Keys.values()) {
            reverseLookup.put(key.code, key);
        }
    }

    /**
     * Returns the key that matches the given GLFW code, or empty() if there is no such key.
     */
    static Optional<Keys> fromCode(int code) {
        return Optional.ofNullable(reverseLookup.get(code));
    }

    Keys(int code) {
        this.code = code;
    }
}
