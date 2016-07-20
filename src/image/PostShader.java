package image;

import engine.shaders.ShaderProgram;

public class PostShader extends ShaderProgram {
	private static String VERT_FILE = MusicMain.RES_PATH + "/shaders/postVertex.shader",
			FRAG_FILE = MusicMain.RES_PATH + "/shaders/postFragment.shader";
	private int location_texture;
	private int location_time;
	private int location_overlay;
	private int location_text;

	public PostShader() {
		super(VERT_FILE, FRAG_FILE);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "texCoords");
	}

	@Override
	protected void getAllUniformLocations() {
		location_texture = super.getUniformLocation("textureSampler");
		location_time = super.getUniformLocation("time");
		location_overlay = super.getUniformLocation("overlay");
		location_text = super.getUniformLocation("text");
	}
	public void loadTime(float time)
	{
		super.loadFloat(location_time, time);
	}
	public void loadTexture() {
		super.loadInt(location_texture, 0);
		super.loadInt(location_overlay, 1);
		super.loadInt(location_text, 2);
	}
}
