package image;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glLineWidth;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import data.Profile;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import engine.renderEngine.DisplayManager;
import engine.renderEngine.Loader;
import engine.sound.MasterSound;
import engine.toolbox.Input;

public class MasterRenderer {
	private ImageRenderer image;
	private List<LineRenderer> lines;
	private List<ColumnRenderer> columns;
	private PostRenderer postRenderer;
	private ParticleRenderer particles;
	private MasterSound sound;
	private Loader loader;
	public static final float[] vertices = { -1f, -1f, -1f, 1f, -1f, -1f, -1f, 1f, -1f, 1f, 1f, -1f, };
	public static final float[] texCoords = { 0, 1, 1, 1, 0, 0, 1, 0 };
	public static int NUM_SAMPLES;
	private FloatBuffer musicBuffer;
	private boolean paused = false;
	private int currentText;

	public MasterRenderer(Profile p) {
		NUM_SAMPLES = p.getNumSamples();
		sound = new MasterSound(p.getMusicFile());
		DisplayManager.createDisplay(p.isvSync());
		loader = new Loader();
		image = new ImageRenderer(loader, p.getImage(), p.getLines(), p.isScaling(), p.getIntensityScale(),
				p.getIntensityOffset());
		List<String> lyrics = p.getText();
		lines = new ArrayList<>(8);
		columns = new ArrayList<>(8);
		for (int i = 0; i < p.getLines().size(); i++) {
			lines.add(new LineRenderer(NUM_SAMPLES * MasterSound.TESS_LEVEL, p.getLines().get(i)));
			columns.add(new ColumnRenderer(NUM_SAMPLES * MasterSound.TESS_LEVEL, p.getLines().get(i)));
		}
		particles = new ParticleRenderer(loader);
		if (p.getOverlay() == null) {
			postRenderer = new PostRenderer(loader, "");
		} else {
			postRenderer = new PostRenderer(loader, p.getOverlay());
		}
		glClearColor(0, 0, 0, 1f);
		glLineWidth(2f);
		//glCullFace(GL_NONE);
		musicBuffer = BufferUtils.createFloatBuffer(NUM_SAMPLES * MasterSound.TESS_LEVEL);
		//sound.play();
	}

	public void render() {
		sound.run();
		float[] data = sound.getValues();
		float bassGain = sound.getBass();
		musicBuffer.put(data);
		musicBuffer.flip();
		postRenderer.bindPostProcessor();

		image.render(bassGain, musicBuffer);
		particles.render(sound.getRawBassGain());
		for (ColumnRenderer f : columns) {
			f.render(data);
		}
		postRenderer.render();

		if (Input.keys[GLFW.GLFW_KEY_SPACE]) {
			paused = !paused;
			if (paused) {
				sound.pause();
			} else {
				sound.resume();
			}
			Input.keys[GLFW.GLFW_KEY_SPACE] = false;
		}
		//System.out.println(currentText);
		DisplayManager.updateDisplay();
		data = null;
	}
	public static void updateProfile(Profile currentProfile) {
		// TODO Auto-generated method stub

	}
	public void terminate() {
		sound.end();
		DisplayManager.closeDisplay();
	}

	public static void main(String[] args) {
		Profile currentProfile = new MusicController().loadProfile(new File("../Fractures.prof"));
		DisplayManager.setDimension(currentProfile.getResolution());
		MasterRenderer renderer = new MasterRenderer(currentProfile);
		while (!Input.keys[GLFW.GLFW_KEY_ESCAPE] && !DisplayManager.shouldClose()) {
			renderer.render();
		}
		renderer.terminate();
		Input.keys[GLFW.GLFW_KEY_ESCAPE] = false;
	}
}
