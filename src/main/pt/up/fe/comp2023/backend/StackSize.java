package pt.up.fe.comp2023.backend;

public class StackSize {
    int currentSize = 0;
    int maxSize = 0;

    public void increaseSize(int increment) {
        currentSize += increment;
        maxSize = Math.max(maxSize, currentSize);
    }

    public void decreaseSize(int decrement) {
        currentSize = Math.max(0, currentSize - decrement);
    }

    public int getMaxSize() {
        return maxSize;
    }
}
