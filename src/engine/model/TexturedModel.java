package engine.model;

import java.io.Serializable;

import engine.textures.ModelTexture;

public class TexturedModel implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = -1513028102713175752L;
	private RawModel rawModel;
	private ModelTexture texture;

	public TexturedModel(RawModel model, ModelTexture texture){
		this.rawModel = model;
		this.texture = texture;
	}

	public RawModel getRawModel() {
		return rawModel;
	}

	public ModelTexture getTexture() {
		return texture;
	}
}
