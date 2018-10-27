package image;

import engine.math.Matrix4f;
import engine.math.Vector3f;
import engine.math.Vector4f;
import engine.shaders.ShaderProgram;

import java.util.List;

public class ImageShader extends ShaderProgram {
	private class LineLocation {
		private int location_start, location_end, location_height;
		private int location_color;
	}

	private static final String VERT_FILE = MusicController.SHADER_PATH + "/shaders/imageVertex.shader",
			FRAG_FILE = MusicController.SHADER_PATH + "/shaders/imageFragment.shader";
	private int location_texture;
	private int location_transformationMatrix;
	private int location_lightPosition;
	private int location_lightColor;
	private int location_intensity;
	private int location_lineColor;
	private int location_intensityScale;
	private int location_intensityOffset;
	private int location_lineWidth;
	private int location_array;
	private int location_numLines;
	private int location_projection;
	private LineLocation lineLocations[];
	private int location_view;

	public ImageShader() {
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
		location_transformationMatrix = super.getUniformLocation("transformationMatrix");
		location_lightPosition = super.getUniformLocation("lightPosition");
		location_lightColor = super.getUniformLocation("lightColor");
		location_intensity = super.getUniformLocation("intensity");
		location_lineColor = super.getUniformLocation("lineColor");
		location_intensityScale = super.getUniformLocation("intensityScale");
		location_intensityOffset = super.getUniformLocation("intensityOffset");
		location_lineWidth = super.getUniformLocation("lineWidth");
		location_numLines = super.getUniformLocation("numLines");
		location_array = super.getUniformLocation("music");
		location_projection = super.getUniformLocation("projection");
		location_view = super.getUniformLocation("viewMatrix");
	}

	public void loadIntensity(float intensityScale, float intensityOffset) {
		super.loadFloat(location_intensityScale, intensityScale);
		super.loadFloat(location_intensityOffset, intensityOffset);
	}

	public void loadLineColor(Vector4f lineColor) {
		super.loadVector(location_lineColor, lineColor);
	}

	public void loadTransformationMatrix(Matrix4f transformationMatrix) {
		super.loadMatrix(location_transformationMatrix, transformationMatrix);
	}

	public void loadViewMatrix(Matrix4f viewMatrix) {
		super.loadMatrix(location_view, viewMatrix);
	}

	public void loadTextures() {
		super.loadInt(location_texture, 0);
		super.loadInt(location_array, 1);
	}

	public void loadLight(Vector3f lightPosition, Vector4f lightColor) {
		super.loadVector(location_lightPosition, lightPosition);
		super.loadVector(location_lightColor, lightColor);
	}

	public void loadIntensity(float intensity) {
		super.loadFloat(location_intensity, intensity);
	}

	public void loadLineWidth(float lineWidth) {
		super.loadFloat(location_lineWidth, lineWidth);
	}

	public void loadLines(List<Line> lines) {
		lineLocations = new LineLocation[(lines.size())];
		super.loadInt(location_numLines, lines.size());
		for (int i = 0; i < lines.size(); i++) {
			lineLocations[i] = new LineLocation();
			lineLocations[i].location_start = super.getUniformLocation("lines[" + i + "].start");
			lineLocations[i].location_end = super.getUniformLocation("lines[" + i + "].end");
			lineLocations[i].location_height = super.getUniformLocation("lines[" + i + "].height");
			lineLocations[i].location_color = super.getUniformLocation("lines[" + i + "].color");
			super.loadVector(lineLocations[i].location_start, lines.get(i).start);
			super.loadVector(lineLocations[i].location_end, lines.get(i).end);
			super.loadFloat(lineLocations[i].location_height, lines.get(i).height);
			super.loadVector(lineLocations[i].location_color, lines.get(i).color);
		}
	}

	public void loadProjection(Matrix4f projectionMatrix) {
		super.loadMatrix(location_projection, projectionMatrix);
	}

}
