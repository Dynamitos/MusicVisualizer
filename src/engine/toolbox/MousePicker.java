package engine.toolbox;

import engine.entities.Camera;
import engine.math.Matrix4f;
import engine.math.Vector2f;
import engine.math.Vector3f;
import engine.math.Vector4f;
import engine.renderEngine.DisplayManager;

public class MousePicker {
	private Vector3f currentRay;
	
	private Matrix4f projectionMatrix;
	private Matrix4f viewMatrix;
	private Camera camera;
	
	public MousePicker(Camera cam, Matrix4f projectionMatrix){
		this.camera = cam;
		this.projectionMatrix = projectionMatrix;
		this.viewMatrix = Maths.createViewMatrix(camera);
	}
	public Vector3f getCurrentRay(){
		return currentRay;
	}
	public void update(){
		viewMatrix = Maths.createViewMatrix(camera);
		currentRay = calculateMouseRay();
	}
	private Vector3f calculateMouseRay(){
		float mouseX = DisplayManager.WIDTH/2;
		float mouseY = DisplayManager.HEIGHT/2;
		Vector2f normalizedCoords = getNormalizedDeviceCoords(mouseX, mouseY);
		Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1f, 1f); 
		Vector4f eyeCoords = toEyeCoords(clipCoords);
		Vector3f worldRay = toWorldCoords(eyeCoords);
		return worldRay;
	}
	private Vector3f toWorldCoords(Vector4f eyeCoords){
		Matrix4f invertexView = Matrix4f.invert(viewMatrix, null);
		Vector4f rayWorld = Matrix4f.transform(invertexView, eyeCoords, null);
		Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
		mouseRay = mouseRay.normalize();
		return mouseRay;
	}
	private Vector4f toEyeCoords(Vector4f clipCoords){
		Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
		Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
		return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
	}
	private Vector2f getNormalizedDeviceCoords(float mouseX, float mouseY){
		float x = (2f * mouseX) / DisplayManager.WIDTH - 1;
		float y = (2f * mouseY) / DisplayManager.HEIGHT -1;
		return new Vector2f(x, y);
	}
}
