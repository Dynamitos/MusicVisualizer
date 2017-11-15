package image;

public abstract class RenderMode {
	public abstract void init(Profile p);
	public abstract void render(float[] data);
	public abstract void destroy();
}
