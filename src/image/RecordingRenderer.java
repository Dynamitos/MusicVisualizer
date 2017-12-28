package image;

import data.Profile;
import engine.renderEngine.DisplayManager;
import engine.renderEngine.Loader;
import engine.sound.MasterSound;
import engine.toolbox.Input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class RecordingRenderer extends RenderMode {
    private static class BandSample implements Comparable<BandSample>
    {
        @Override
        public int compareTo(BandSample o) {
            return Long.compare(timeStamp, o.timeStamp);
        }

        private long timeStamp;
        private float[] bands;

        public BandSample(long timeStamp, float[] bands) {
            this.timeStamp = timeStamp;
            this.bands = bands;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

        public float[] getBands() {
            return bands;
        }

        public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }

        public void setBands(float[] bands) {
            this.bands = bands;
        }

        @Override
        public String toString() {
            return "BandSample{" +
                    "timeStamp=" + timeStamp +
                    ", bands=" + Arrays.toString(bands) +
                    '}';
        }
    }
    private static class BandQueue
    {
        public volatile boolean isFinished = false;
        private Queue<BandSample> queue;
        public BandQueue()
        {
            queue = new PriorityQueue<>();
        }
        public void put(long timeStamp, float[] data)
        {
            System.out.println("Adding: " + timeStamp);
            queue.add(new BandSample(timeStamp, data));
        }
        public boolean isEmpty()
        {
            return queue.isEmpty();
        }
        public BandSample get()
        {
            return queue.poll();
        }
    }
    private BandQueue bandQueue;
    private long currentTime;
    private long framesPerSecond;
    private long startTime;
    private OutputRenderer outputRenderer;
    @Override
    public void init(Profile p) {
        DisplayManager.createDisplay(p.isvSync());
        sound = new MasterSound(p.getMusicFile());
        loader = new Loader();
        image = new ImageRenderer(loader, p.getImage(), p.isScaling(), p.getIntensityScale(), p.getIntensityOffset());
        lines = new ArrayList<>(p.getLines().size());
        columns = new ArrayList<>(p.getLines().size());
        for(int i = 0; i < p.getLines().size(); ++i)
        {
            lines.add(new LineRenderer(sound.getDataLength(), p.getLines().get(i)));
            columns.add(new ColumnRenderer(sound.getDataLength(), p.getLines().get(i)));
        }
        particles = new ParticleRenderer(loader);
        postRenderer = new PostRenderer(loader, p.getOverlay());
        outputRenderer = new OutputRenderer(loader);
        bandQueue = new BandQueue();
    }

    @Override
    public void launch() {
        sound.play();
        Thread t = new Thread(() -> {
            startTime = System.currentTimeMillis();
            framesPerSecond = 60;
            while(currentTime-startTime < 1000 && !Input.keys[GLFW_KEY_ESCAPE])
            {
                currentTime = System.currentTimeMillis();
                float[] bands = sound.calcFFT();
                bandQueue.put(currentTime - startTime, bands);
                try {
                    Thread.sleep((long) (1000f/framesPerSecond)-(System.currentTimeMillis()-currentTime));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                //System.out.println("Sampling time: " + (System.currentTimeMillis() - currentTime));
            }
            sound.end();
            bandQueue.isFinished = true;
        });
        t.start();
        while(!bandQueue.isFinished || !bandQueue.isEmpty())
        {
            BandSample b = bandQueue.get();
            if(b == null)
                continue;
            System.out.println("Processing: " + b.getTimeStamp());
            float[] data = sound.smoothFFT(b.getBands());
            float bassGain = sound.calcBass(b.getBands());
            //System.out.println(b);
            postRenderer.beginMainPass();
            image.render(bassGain);
            for(ColumnRenderer c : columns)
            {
                c.render(data);
            }
            for(LineRenderer l : lines)
            {
                l.render(data);
            }
            particles.render(bassGain);
            outputRenderer.bindTarget();
            //postRenderer.unbindCurrentFramebuffer();
            postRenderer.render();
            outputRenderer.render(b.getTimeStamp());
            //DisplayManager.updateDisplay();
        }
        outputRenderer.finish();
    }

    @Override
    public void terminate() {

    }
    public static void main(String[] args) throws FileNotFoundException {
        Profile currentProfile = new MusicController().loadProfile(new FileInputStream(new File("../Badman.prof")));
        DisplayManager.setDimension(currentProfile.getResolution());
        RecordingRenderer renderer = new RecordingRenderer();
        renderer.init(currentProfile);
        renderer.launch();
        renderer.terminate();
    }
}
