package image;

import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glLineWidth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class MasterRenderer extends RenderMode{
	public static final float[] vertices = { -1f, -1f, -1f, 1f, -1f, -1f, -1f, 1f, -1f, 1f, 1f, -1f, };
	public static final float[] texCoords = { 0, 1, 1, 1, 0, 0, 1, 0 };
	private boolean paused = false;
	private int currentText;

	public MasterRenderer(){

	}
	public void init(Profile p) {
		DisplayManager.createDisplay(p.isvSync(), p.isRecording());

		sound = new MasterSound(p.getMusicFile());

		loader = new Loader();
		image = new ImageRenderer(loader, p.getImage(), p.isScaling(), p.getIntensityScale(),
				p.getIntensityOffset());
		List<String> lyrics = p.getText();
		lines = new ArrayList<>(8);
		columns = new ArrayList<>(8);
		for (int i = 0; i < p.getLines().size(); i++) {
			lines.add(new LineRenderer(sound.getDataLength(), p.getLines().get(i)));
			columns.add(new ColumnRenderer(sound.getDataLength(), p.getLines().get(i)));
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
		sound.play();
		DisplayManager.showWindow();
	}

	@Override
	public void launch() {
		while (!Input.keys[GLFW.GLFW_KEY_ESCAPE] && !DisplayManager.shouldClose() && !shouldTerminate) {
			render();
		}
		Input.keys[GLFW.GLFW_KEY_ESCAPE] = false;
	}

	public void render() {
		float[] bands = sound.calcFFT();
		float[] data = sound.smoothFFT(bands);
		float bassGain = sound.calcBass(bands);

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
		postRenderer.unbindCurrentFramebuffer();
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
		DisplayManager.updateDisplay();
	}
	public static void updateProfile(Profile currentProfile) {
		// TODO Auto-generated method stub

	}
	public void terminate() {
		sound.end();
		particles.terminate();
		DisplayManager.closeDisplay();
	}
	public static void main(String[] args) throws FileNotFoundException {
		Profile currentProfile = new MusicController().loadProfile(new FileInputStream(new File("../Badman.prof")));
		DisplayManager.setDimension(currentProfile.getResolution());
		MasterRenderer renderer = new MasterRenderer();
		renderer.init(currentProfile);
		renderer.launch();
		renderer.terminate();
	}
}
