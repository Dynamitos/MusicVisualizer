package image;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import static image.MasterRenderer.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.BufferUtils;

import engine.entities.Camera;
import engine.math.Matrix4f;
import engine.math.Vector2f;
import engine.math.Vector3f;
import engine.math.Vector4f;
import engine.model.RawModel;
import engine.renderEngine.DisplayManager;
import engine.renderEngine.Loader;
import engine.sound.MasterSound;
import engine.toolbox.Input;
import engine.toolbox.Maths;
import image.Line;

public class ImageRenderer extends Thread {
	private Matrix4f transformationMatrix;
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;
	private ImageShader shader;
	private boolean isScaling;
	private RawModel rawModel;
	private int texID;
	private int arrayID;
	private Camera cam;

	private static final float FOV = 70;
	private static final float NEAR_PLANE = 1f;
	private static final float FAR_PLANE = 1000f;

	// Nekomonogatari
	public ImageRenderer(Loader loader, String image, List<Line> lines, boolean isScaling, float intensityScale,
			float intensityOffset) {
		this.isScaling = isScaling;
		rawModel = loader.loadToVAO(MasterRenderer.vertices, MasterRenderer.texCoords);
		texID = loader.loadTexture(image);
		arrayID = glGenTextures();
		cam = new Camera(new Vector3f(0, 0, -1));
		createProjectionMatrix();
		shader = new ImageShader();
		shader.start();
		shader.loadLines(lines);
		shader.loadProjection(projectionMatrix);
		shader.loadIntensity(intensityScale, intensityOffset);
		shader.stop();

	}

	public void render(float mean, FloatBuffer data) {
		shader.start();
		cam.move();
		if (isScaling) {
			transformationMatrix = Maths.createTransformationMatrix(new Vector2f(0, 0),
					new Vector2f(1 + mean, 1 + mean));
		} else {
			transformationMatrix = Maths.createTransformationMatrix(new Vector2f(0, 0), new Vector2f(1, 1));
		}
		shader.loadTransformationMatrix(transformationMatrix);
		shader.loadViewMatrix(Maths.createViewMatrix(cam));

		// System.out.println(String.format("%.5f | %.5f", minAVG, maxAVG));
		glBindVertexArray(rawModel.getVaoID());
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texID);
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_1D, arrayID);
		glTexImage1D(GL_TEXTURE_1D, 0, GL_RED, MasterRenderer.NUM_SAMPLES * MasterSound.TESS_LEVEL, 0, GL_RED, GL_FLOAT,
				data);

		shader.loadTextures();
		shader.loadIntensity(mean);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindTexture(GL_TEXTURE_1D, 0);
		glBindVertexArray(0);
		shader.stop();
	}
	private void createProjectionMatrix() {
		float aspectRatio = (float)DisplayManager.WIDTH/(float)DisplayManager.HEIGHT;
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustrum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustrum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustrum_length);
		projectionMatrix.m33 = 0;

	}
}
