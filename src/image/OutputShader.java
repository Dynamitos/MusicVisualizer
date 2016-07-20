package image;

import engine.shaders.ShaderProgram;

public class OutputShader extends ShaderProgram {
	private static String VERT_FILE = MusicMain.RES_PATH + "/shaders/outputVertex.shader",
			FRAG_FILE = MusicMain.RES_PATH + "/shaders/outputFragment.shader";
	private int location_texture;

	public OutputShader() {
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
	}

	public void loadTexture() {
		super.loadInt(location_texture, 0);
	}
}
