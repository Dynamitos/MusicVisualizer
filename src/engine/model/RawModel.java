package engine.model;

import java.io.Serializable;

public class RawModel implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = -9181581240285344870L;
	private int vaoID;
	private int vertexCount;
	private String name;

	public RawModel(int vaoID, int vertexCount, String name) {
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
		this.name = name;
	}
	public int getVaoID() {
		return vaoID;
	}
	public void setVaoID(int vaoID) {
		this.vaoID = vaoID;
	}
	public int getVertexCount() {
		return vertexCount;
	}
	public void setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}
	public String getName() {
		return name;
	}
}
