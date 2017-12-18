package image;

import engine.math.Vector4f;
import engine.shaders.ShaderProgram;

public class ColumnShader extends ShaderProgram{

    private static final String VERT_FILE = MusicController.SHADER_PATH + "/shaders/lineVertex.shader",
            FRAG_FILE = MusicController.SHADER_PATH + "/shaders/lineFragment.shader";
    private int location_Color;

    public ColumnShader() {
        super(VERT_FILE, FRAG_FILE);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "basePosition");
        super.bindAttribute(1, "displacement");
    }

    @Override
    protected void getAllUniformLocations() {
        location_Color = super.getUniformLocation("color");
    }

    public void loadColor(Vector4f color) {
        super.loadVector(location_Color, color);
    }

}
