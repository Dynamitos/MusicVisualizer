package engine.entities;

import engine.math.Matrix4f;
import engine.math.Vector3f;
import engine.renderEngine.DisplayManager;

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
		lookAt(position, new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
	}

	private void lookAt(Vector3f position, Vector3f center, Vector3f upVector) {
		/*
		 * Vector3f forward = center.copy().subtract(position).normalize();
		 * Vector3f side = forward.copy().cross(upVector).normalize(); upVector
		 * = side.copy().cross(forward);
		 *
		 * viewMatrix.m00 = side.x; viewMatrix.m01 = side.y; viewMatrix.m02 =
		 * side.z; viewMatrix.m10 = upVector.x; viewMatrix.m11 = upVector.y;
		 * viewMatrix.m12 = upVector.z; viewMatrix.m20 = -forward.x;
		 * viewMatrix.m21 = -forward.y; viewMatrix.m22 = -forward.z;
		 */
		viewMatrix.setIdentity();
		Matrix4f.translate(position, viewMatrix, viewMatrix);
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
