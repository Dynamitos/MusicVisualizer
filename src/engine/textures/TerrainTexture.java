package engine.textures;

import java.io.Serializable;

public class TerrainTexture implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = -2763080848694485223L;
	private int textureID;
	private int displacementID;

	public int getTextureID() {
		return textureID;
	}

	public TerrainTexture(int textureID) {
		this.textureID = textureID;
	}

	public int getDisplacementID() {
		return displacementID;
	}

	public void setDisplacementID(int displacementID) {
		this.displacementID = displacementID;
	}



}
