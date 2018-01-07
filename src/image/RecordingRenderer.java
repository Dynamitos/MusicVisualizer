package image;

import data.Profile;
import ddf.minim.AudioBuffer;
import engine.renderEngine.DisplayManager;
import engine.renderEngine.Loader;
import engine.sound.MasterSound;
import engine.toolbox.Input;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.lwjgl.glfw.GLFW.*;

public class RecordingRenderer extends RenderMode {
    private static class BandSample implements Comparable<BandSample>
    {
        @Override
        public int compareTo(BandSample o) {
            return Integer.compare(index, o.getIndex());
        }

        private long timeStamp;
        private int index;
        private float[] bands;

        public BandSample(int sampleIndex, long timeStamp, float[] bands) {
            this.index = sampleIndex;
            this.timeStamp = timeStamp;
            this.bands = bands;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public float[] getBands() {
            return bands;
        }

        public void setBands(float[] bands) {
            this.bands = bands;
        }

        public long getTimeStamp() {
            return timeStamp;
        }

         public void setTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
        }

         @Override
        public String toString() {
            return "BandSample{" +
                    "timeStamp=" + timeStamp +
                    '}';
        }
    }
    private static class BandQueue
    {
        public volatile boolean isFinished = false;
        private Queue<BandSample> queue;
        private int index;
        public BandQueue()
        {
            queue = new PriorityQueue<>();
            index = 0;
        }

        public void put(long timeStamp, float[] data)
        {
            //System.out.println("Adding: " + timeStamp);
            queue.add(new BandSample(index++,timeStamp, data));
        }
        public boolean isEmpty()
        {
            return queue.isEmpty();
        }
        public BandSample get()
        {
            BandSample sample = null;
            try {
                sample = queue.poll();
            } catch (NullPointerException e)
            { }
            return sample;
        }
    }
    private BandQueue bandQueue;
    private OutputRenderer outputRenderer;
    private File musicFile;
    private ExecutorService soundExecutor;
    private ExecutorService renderExecutor;
    private LoadingScreen loadingScreen;

    @Override
    public void init(Profile p) {
        soundExecutor = Executors.newSingleThreadExecutor();
        renderExecutor = Executors.newSingleThreadExecutor();
        Future f = renderExecutor.submit(() -> {
            DisplayManager.createDisplay(p.isvSync(), p.isRecording());

            sound = new MasterSound(p.getMusicFile());
            musicFile = p.getMusicFile();

            loader = new Loader();
            image = new ImageRenderer(loader, p.getImage(), p.isScaling(), p.getIntensityScale(), p.getIntensityOffset());
            lines = new ArrayList<>(p.getLines().size());
            columns = new ArrayList<>(p.getLines().size());
            for (int i = 0; i < p.getLines().size(); ++i) {
                lines.add(new LineRenderer(sound.getDataLength(), p.getLines().get(i)));
                columns.add(new ColumnRenderer(sound.getDataLength(), p.getLines().get(i)));
            }
            particles = new ParticleRenderer(loader);
            postRenderer = new PostRenderer(loader, p.getOverlay());
            outputRenderer = new OutputRenderer(loader, musicFile.getAbsolutePath());
            bandQueue = new BandQueue();
            //DisplayManager.showWindow();
        });
        while(!f.isDone());
    }

    @Override
    public void launch() {
        sound.play();
        soundExecutor.submit(() -> {
            float fps = 60;
            long startTime = System.nanoTime();
            while(sound.isPlaying())
            {
                float[] bands = sound.calcFFT().clone();
                bandQueue.put(System.nanoTime()-startTime, bands);
                try{
                    Thread.sleep((long) (1000f/fps));
                } catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
            sound.end();
            bandQueue.isFinished = true;
        });
        openLoadingScreen();
        renderExecutor.submit(() -> {
            long lastTime = 0;
            while (!bandQueue.isFinished || !bandQueue.isEmpty()) {
                long start = System.nanoTime();
                BandSample b = bandQueue.get();
                System.out.println("Remaining: " + bandQueue.queue.size());
                if (b == null)
                    continue;
                float delta = (float) ((b.getTimeStamp() - lastTime) / Math.pow(10, 9));
                DisplayManager.setFrameTime(delta);
                lastTime = b.getTimeStamp();
                updateLoadingScreen((b.getTimeStamp() / Math.pow(10, 6)) / sound.getDuration());
                render(b);
                long end = System.nanoTime();
                System.out.println("Render time: " + (end-start)/Math.pow(10,9));
            }
            System.out.println("Finish rendering");
            outputRenderer.encodeAudio();
            outputRenderer.finish();
            updateLoadingScreen(1);
        });
    }



    public void render(BandSample b)
    {
        float[] data = sound.smoothFFT(b.getBands());
        float bassGain = sound.calcBass(b.getBands());
        postRenderer.beginMainPass();
        image.render(bassGain);
        particles.render(sound.getRawBassGain());

        for (ColumnRenderer c : columns) {
            c.render(data);
        }

        for(LineRenderer l : lines)
        {
            l.render(data);
        }
        outputRenderer.bindTarget();
        //postRenderer.unbindCurrentFramebuffer();
        postRenderer.render();
        outputRenderer.render(b.getTimeStamp());
        postRenderer.unbindCurrentFramebuffer();
    }
    private void updateLoadingScreen(double value) {
        loadingScreen.setProgress(value);
    }

    private void openLoadingScreen() {
        Stage stage = new Stage();
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(MusicController.class.getResource("/gui/LoadingScreen.fxml"));
            root = loader.load();
            loadingScreen = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        loadingScreen.init();
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
