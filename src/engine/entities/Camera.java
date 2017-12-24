package engine.entities;

import engine.math.Matrix4f;
import engine.math.Vector2f;
import engine.math.Vector3f;
import engine.renderEngine.DisplayManager;
import engine.toolbox.Maths;
import image.MasterRenderer;

public class Camera {

	private float angleAroundPlayer = 0;
	private Matrix4f viewMatrix;

	private Vector3f panDirection;
	private Vector3f position;
	private float pitch = 0;
	private float yaw = 0;
	private float roll = 0;

	public Camera(Vector3f position) {
		this.position = position;
		viewMatrix = new Matrix4f();
	}

	public void move() {
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
