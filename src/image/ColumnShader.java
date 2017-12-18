package image;

import engine.math.Vector4f;
import engine.shaders.ShaderProgram;

public class ColumnShader extends ShaderProgram{

    private static final String VERT_FILE = MusicController.SHADER_PATH + "/shaders/columnVertex.shader",
            FRAG_FILE = MusicController.SHADER_PATH + "/shaders/columnFragment.shader";
    private int location_color;

    public ColumnShader() {
        super(VERT_FILE, FRAG_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "basePosition");
        super.bindAttribute(1, "position");
        super.bindAttribute(2, "displacement");
    }

    @Override
    protected void getAllUniformLocations() {
        location_color = super.getUniformLocation("color");
    }

    public void loadColor(Vector4f color) {
        super.loadVector(location_color, color);
    }

}
