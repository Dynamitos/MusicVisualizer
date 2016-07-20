package engine.textures;

import java.io.Serializable;

public class TerrainTexturePack implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 8547923165558095501L;
	private TerrainTexture backgroundTexture;
	private TerrainTexture rTexture;
	private TerrainTexture gTexture;
	private TerrainTexture bTexture;
	public TerrainTexturePack(TerrainTexture backgroundTexture, TerrainTexture rTexture, TerrainTexture gTexture,
			TerrainTexture bTexture) {
		this.backgroundTexture = backgroundTexture;
		this.rTexture = rTexture;
		this.gTexture = gTexture;
		this.bTexture = bTexture;
	}
	public TerrainTexture getBackgroundTexture() {
		return backgroundTexture;
	}
	public TerrainTexture getrTexture() {
		return rTexture;
	}
	public TerrainTexture getgTexture() {
		return gTexture;
	}
	public TerrainTexture getbTexture() {
		return bTexture;
	}
}
