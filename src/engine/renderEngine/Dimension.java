
package engine.renderEngine;

/**
 * @author Dynamitos5
 *
 */
public class Dimension {
	private int WIDTH, HEIGHT;

	public Dimension(){}

	public Dimension(int width, int height) {
		super();
		WIDTH = width;
		HEIGHT = height;
	}

	public int getWIDTH() {
		return WIDTH;
	}

	public void setWIDTH(int width) {
		WIDTH = width;
	}

	public int getHEIGHT() {
		return HEIGHT;
	}

	public void setHEIGHT(int height) {
		HEIGHT = height;
	}

	@Override
	public String toString() {
		return WIDTH+"x"+HEIGHT;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dimension other = (Dimension) obj;
		if (HEIGHT != other.HEIGHT)
			return false;
		if (WIDTH != other.WIDTH)
			return false;
		return true;
	}

}
