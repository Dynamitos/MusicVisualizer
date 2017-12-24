package engine.toolbox;

import engine.entities.Camera;
import engine.math.Matrix4f;
import engine.math.Vector2f;
import engine.math.Vector3f;

public class Maths {
    public static void lookAt(Matrix4f viewMatrix, Vector3f position, Vector3f center, Vector3f upVector) {
        Vector3f forward = center.copy().subtract(position).normalize();
        Vector3f side = forward.copy().cross(upVector).normalize();
        Vector3f up = side.copy().cross(forward);

        viewMatrix.setIdentity();
        viewMatrix.m00 = side.x;
        viewMatrix.m01 = side.y;
        viewMatrix.m02 = side.z;
        viewMatrix.m10 = up.x;
        viewMatrix.m11 = up.y;
        viewMatrix.m12 = up.z;
        viewMatrix.m20 = -forward.x;
        viewMatrix.m21 = -forward.y;
        viewMatrix.m22 = -forward.z;

        Matrix4f.translate(new Vector3f(-position.x, -position.y, -position.z), viewMatrix, viewMatrix);


    }

    public static float barryCentric(Vector3f p1, Vector3f p2, Vector3f p3, Vector2f pos) {
        float det = (p2.z - p3.z) * (p1.x - p3.x) + (p3.x - p2.x) * (p1.z - p3.z);
        float l1 = ((p2.z - p3.z) * (pos.x - p3.x) + (p3.x - p2.x) * (pos.y - p3.z)) / det;
        float l2 = ((p3.z - p1.z) * (pos.x - p3.x) + (p1.x - p3.x) * (pos.y - p3.z)) / det;
        float l3 = 1.0f - l1 - l2;
        return l1 * p1.y + l2 * p2.y + l3 * p3.y;
    }

    public static Matrix4f createTransformationMatrix(Vector3f translation, float rx, float ry, float rz, float scale) {
        Matrix4f matrix = new Matrix4f();
        Matrix4f.translate(translation, matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rx), new Vector3f(1, 0, 0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(ry), new Vector3f(0, 1, 0), matrix, matrix);
        Matrix4f.rotate((float) Math.toRadians(rz), new Vector3f(0, 0, 1), matrix, matrix);
        return matrix;
    }

    public static Matrix4f createViewMatrix(Camera camera) {
        Matrix4f viewMatrix = new Matrix4f();
        Vector3f cameraPos = camera.getPosition();
        Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
        Matrix4f.rotate(camera.getPitch(), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
        Matrix4f.rotate(camera.getYaw(), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
        Matrix4f.rotate(camera.getRoll(), new Vector3f(0, 0, 1), viewMatrix, viewMatrix);
        return viewMatrix;
    }

    public static Matrix4f createTransformationMatrix(Vector2f translation, Vector2f scale) {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        Matrix4f.translate(translation, matrix, matrix);
        Matrix4f.scale(new Vector3f(scale.x, scale.y, 1f), matrix, matrix);
        return matrix;
    }

    private static Vector2f translation = new Vector2f();
    private static Vector3f scale = new Vector3f(0, 0, 1);

    public static void createTransformationMatrix(Matrix4f transformationMatrix, int rx, int ry, float sx,
                                                  float sy) {
        transformationMatrix.setIdentity();
        translation.x = rx;
        translation.y = ry;
        scale.x = sx;
        scale.y = sy;
        Matrix4f.translate(translation, transformationMatrix, transformationMatrix);
        Matrix4f.scale(scale, transformationMatrix, transformationMatrix);
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min)) + min;
    }
}
