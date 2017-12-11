package image;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import data.Profile;
import org.lwjgl.BufferUtils;

import engine.renderEngine.Loader;
import engine.sound.MasterSound;

public class LineMode extends RenderMode {
	private ImageRenderer image;
	private List<LineRenderer> lines;
	private PostRenderer postRenderer;
	private Loader loader;
	private FloatBuffer musicBuffer;
	@Override
	public void init(Profile p) {
		image = new ImageRenderer(loader, p.getImage(), p.getLines(), p.isScaling(), p.getIntensityScale(),
				p.getIntensityOffset());
		lines = new ArrayList<>(8);
		for (int i = 0; i < p.getLines().size(); i++) {
			lines.add(new LineRenderer(MasterRenderer.NUM_SAMPLES * MasterSound.TESS_LEVEL, p.getLines().get(i)));
		}
		if (p.getOverlay() == null) {
			postRenderer = new PostRenderer(loader, "");
		} else {
			postRenderer = new PostRenderer(loader, p.getOverlay());
		}
		musicBuffer = BufferUtils.createFloatBuffer(MasterRenderer.NUM_SAMPLES * MasterSound.TESS_LEVEL);

	}

	@Override
	public void render(float[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
