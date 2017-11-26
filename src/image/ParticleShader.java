package image;

import engine.math.Matrix4f;
import engine.math.Vector4f;
import engine.shaders.ShaderProgram;

public class ParticleShader extends ShaderProgram {
	private static final String VERT_FILE = MusicController.SHADER_PATH + "/shaders/particleVertex.shader",
			FRAG_FILE = MusicController.SHADER_PATH + "/shaders/particleFragment.shader";
	private int location_color;
	private int location_projectionMatrix;

	public ParticleShader() {
		super(VERT_FILE, FRAG_FILE);
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

	@Override
	protected void getAllUniformLocations() {
		location_color = super.getUniformLocation("color");
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
	}

	public void loadProjectionMatrix(Matrix4f projectionMatrix) {
		super.loadMatrix(location_projectionMatrix, projectionMatrix);
	}

	public void loadColor(Vector4f color) {
		super.loadVector(location_color, color);
	}
}
