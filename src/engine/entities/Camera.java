package engine.entities;

import engine.math.Matrix4f;
import engine.math.Vector3f;
import engine.renderEngine.DisplayManager;
import engine.toolbox.Maths;

public class Camera {

	private float angleAroundPlayer = 0;
	private Matrix4f viewMatrix;

	private Vector3f position;
	private float pitch;
	private float yaw = 180 - angleAroundPlayer;
	private float roll;

	public Camera(Vector3f position) {
		this.position = position;
		viewMatrix = new Matrix4f();
	}

	public void move() {
		position.x += DisplayManager.deltaX;
		position.y += DisplayManager.deltaY;
		Maths.lookAt(viewMatrix, position, new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
	}

	public Matrix4f getViewMatrix() {
		return viewMatrix;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}
}
