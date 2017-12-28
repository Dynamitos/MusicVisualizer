package image;

import data.Profile;
import engine.renderEngine.Loader;
import engine.sound.MasterSound;

import java.util.List;

public abstract class RenderMode{
	protected boolean shouldTerminate = false;
	protected ImageRenderer image;
	protected List<LineRenderer> lines;
	protected List<ColumnRenderer> columns;
	protected PostRenderer postRenderer;
	protected ParticleRenderer particles;
	protected MasterSound sound;
	protected Loader loader;
	protected abstract void init(Profile p);
	public abstract void launch();
	public abstract void terminate();
}
