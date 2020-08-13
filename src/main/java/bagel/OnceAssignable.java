package bagel;

import java.util.function.Supplier;

class OnceAssignable<T> {
    private T value;
    private boolean hasValue = false;

    /**
     * Set the value, assuming that it's empty. Throws {@link RuntimeException} if it's not empty.
     */
    public void set(T value) {
        if (!hasValue) {
            this.value = value;
            hasValue = true;
        } else {
            throw new RuntimeException("Attempted to assign multiple times to OnceAssignable");
        }
    }

    /**
     * Set the value if it's empty; do nothing otherwise.
     */
    public void setIfEmpty(T value) {
        if (!hasValue) {
            this.value = value;
            hasValue = true;
        }
    }

    /**
     * Set the value to `value.get()` if it's empty; do nothing otherwise.
     *
     * In particular, `value.get()` is not called if it's not empty.
     */
    public void setIfEmpty(Supplier<T> value) {
        if (!hasValue) {
            this.value = value.get();
            hasValue = true;
        }
    }

    public T get() {
        if (hasValue) {
            return value;
        } else {
            throw new RuntimeException("Attempted to read from empty OnceAssignable");
        }
    }
}
