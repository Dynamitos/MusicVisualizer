package image;

import engine.math.Matrix4f;
import engine.math.Vector4f;
import engine.shaders.ShaderProgram;

public class ParticleShader extends ShaderProgram {
	private static final String VERT_FILE = MusicController.SHADER_PATH + "/shaders/particleVertex.shader",
			FRAG_FILE = MusicController.SHADER_PATH + "/shaders/particleFragment.shader";
	private int location_texture;
	private int location_projectionMatrix;

	public ParticleShader() {
		super(VERT_FILE, FRAG_FILE);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "vertex_VS_in");
		super.bindAttribute(1, "position_VS_in");
		super.bindAttribute(2, "rotation_VS_in");
		super.bindAttribute(3, "atlasCoords_VS_in");
	}

	@Override
	protected void getAllUniformLocations() {
		location_texture = super.getUniformLocation("tex");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
	}

	public void loadProjectionMatrix(Matrix4f projectionMatrix) {
		super.loadMatrix(location_projectionMatrix, projectionMatrix);
	}

	public void loadTexture() {
		super.loadInt(location_texture, 0);
	}
}
