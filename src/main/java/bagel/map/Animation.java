package bagel.map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class Animation {
    static class Frame {

        final int id;
        final int duration;

        public Frame(int id, int duration) {
            this.id = id;
            this.duration = duration;
        }
    }
    private final List<Frame> frames = new ArrayList<>();
    private long lastTime = System.nanoTime();
    private int currentFrame = 0;
    public void addFrame(Frame frame){
        frames.add(frame);
    }

    public Frame getCurrentFrame(){
        long timeDifferenceNS = System.nanoTime() - lastTime;
        long timeDifferenceMS = TimeUnit.MILLISECONDS.convert(timeDifferenceNS, TimeUnit.NANOSECONDS);
        if (timeDifferenceMS > frames.get(currentFrame).duration){
            currentFrame = (currentFrame + 1) % frames.size();
            lastTime = System.nanoTime();
        }
        return frames.get(currentFrame);
    }
}
