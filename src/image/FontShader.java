package image;

import engine.math.Vector2f;
import engine.math.Vector3f;
import engine.shaders.ShaderProgram;

public class FontShader extends ShaderProgram{

	private static final String VERTEX_FILE = "/shaders/fontVertex.shader";
	private static final String FRAGMENT_FILE = "/shaders/fontFragment.shader";

	private int location_colour;
	private int location_translation;

	public FontShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_colour = super.getUniformLocation("colour");
		location_translation = super.getUniformLocation("translation");
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
		super.bindAttribute(1, "textureCoords");
	}

	protected void loadColour(Vector3f colour){
		super.loadVector(location_colour, colour);
	}

	protected void loadTranslation(Vector2f translation){
		super.loadVector(location_translation, translation);
	}


}
