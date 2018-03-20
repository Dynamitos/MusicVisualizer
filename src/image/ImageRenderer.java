package image;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_RED;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage1D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.nio.FloatBuffer;
import java.util.List;

import engine.entities.Camera;
import engine.math.Matrix4f;
import engine.math.Vector3f;
import engine.model.RawModel;
import engine.renderEngine.DisplayManager;
import engine.renderEngine.Loader;
import engine.sound.MasterSound;
import engine.toolbox.Maths;

public class ImageRenderer extends Thread {
	private Matrix4f transformationMatrix;
	private Matrix4f projectionMatrix;
	private ImageShader shader;
	private boolean isScaling;
	private RawModel rawModel;
	private int texID;
	private Camera cam;

	private static final float FOV = 70;
	private static final float NEAR_PLANE = 1f;
	private static final float FAR_PLANE = 1000f;

	public ImageRenderer(Loader loader, String image, boolean isScaling, float intensityScale,
			float intensityOffset) {
		this.isScaling = isScaling;
		rawModel = loader.loadToVAO(MasterRenderer.vertices, MasterRenderer.texCoords);
		texID = loader.loadTexture(image);
		cam = new Camera(new Vector3f(0, 0, -1f));
		createProjectionMatrix();
		shader = new ImageShader();
		shader.start();
		shader.loadProjection(projectionMatrix);
		shader.loadIntensity(intensityScale, intensityOffset);
		shader.stop();
		transformationMatrix = new Matrix4f();
	}

	public void render(float mean) {
		shader.start();
		cam.move();
		if (isScaling) {
			Maths.createTransformationMatrix(transformationMatrix, 0, 0, 1 + mean/10f, 1 + mean/10f);
		} else {
			Maths.createTransformationMatrix(transformationMatrix, 0, 0, 1, 1);
		}
		shader.loadTransformationMatrix(transformationMatrix);
		shader.loadViewMatrix(Maths.createViewMatrix(cam));

		glBindVertexArray(rawModel.getVaoID());
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texID);

		shader.loadTextures();
		shader.loadIntensity(mean);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindTexture(GL_TEXTURE_2D, 0);
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
