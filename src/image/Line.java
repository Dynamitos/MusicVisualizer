package image;

import engine.math.Vector2f;
import engine.math.Vector4f;

public class Line {
	public Vector2f start, end;
	public float height;
	public Vector4f color;
	@Override
	public String toString()
	{
		return start.x+"#"+start.y+"#"+end.x+"#"+end.y+"#"+height+"#"+color.x+"#"+color.y+"#"+color.z+"#"+color.w+"#";
	}
}
