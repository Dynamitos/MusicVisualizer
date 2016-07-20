package engine.textures;

import java.io.Serializable;

public class ModelTexture implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 4476203649816837262L;
	private int textureID;
	private int displacementTextureID = 0;
	private String name;

	private float shineDamper = 1;
	private float reflectivity = 0;

	private boolean hasTransparency = false;
	private boolean useFakeLighting = false;

	private int numberOfRows = 1;

	public int getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}

	public boolean isUseFakeLighting() {
		return useFakeLighting;
	}

	public void setUseFakeLighting(boolean useFakeLighting) {
		this.useFakeLighting = useFakeLighting;
	}

	public ModelTexture(int id){
		this.textureID = id;
	}
	public ModelTexture(int id, String name){
		this.textureID = id;
		this.name = name;
	}
	public ModelTexture(int diffuseID, int displacementID){
		this.textureID = diffuseID;
		this.displacementTextureID = displacementID;
	}


	public int getTextureID() {
		return textureID;
	}
	public int getDisplacementID(){
		return displacementTextureID;
	}
	public void setTextureID(int textureID) {
		this.textureID = textureID;
	}

	public void setDisplacementTextureID(int displacementTextureID) {
		this.displacementTextureID = displacementTextureID;
	}

	public float getShineDamper() {
		return shineDamper;
	}

	public boolean isHasTransparency() {
		return hasTransparency;
	}

	public void setShineDamper(float shineDamper) {
		this.shineDamper = shineDamper;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}

	public void setHasTransparency(boolean hasTransparency) {
		this.hasTransparency = hasTransparency;
	}

	public String getName() {
		return name;
	}

}
