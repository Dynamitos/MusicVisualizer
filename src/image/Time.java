package image;

public class Time {
	private float startTime, duration;

	public Time(float startTime, float duration) {
		super();
		this.startTime = startTime;
		this.duration = duration;
	}

	public float getStartTime() {
		return startTime;
	}

	public void setStartTime(float startTime) {
		this.startTime = startTime;
	}

	public float getDuration() {
		return duration;
	}

	public void setDuration(float duration) {
		this.duration = duration;
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
		Time other = (Time) obj;
		if(other.startTime<startTime || other.startTime > (startTime+duration))
			return false;
		return true;
	}

}
