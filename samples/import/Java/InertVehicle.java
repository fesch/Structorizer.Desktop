/**
 * Demo class for Structorizer Java import
 */
package de.fhe.traffic;

/**
 * Class simulating a friction-free vehicle moving along a straight axis with
 * constant speed.
 * @author Kay Gürtzig
 */
public class InertVehicle {
	
	public enum LengthUnit {MILLIMETRE, CENTIMETRE, INCH, DECIMETRE, FOOT, YARD, METRE, KILOMETRE, MILE}
	
	public enum SpeedUnit {METRESPERSECOND, KILOMETRESPERHOUR, MILESPERHOUR}
	
	/** The factor to multiply with a length in given {@link LengthUnit} to obtain it in metres */
	private static double[] LENGTH2METRE = new double[] {
			0.001, 0.01, 0.0254, 0.1, 0.3048, 0.9144, 1.0, 1000.0, 1609.344
	};
	
	private static double[] SPEED2MPS = new double[] {
			1.0, 1/3.6, 0.44704
	};

	/** Position at last speed change in metres */
	private double lastPosition = 0;
	
	/** Speed in m/s */
	private double speed = 0;
	
	/** Time of last speed change in ms */
	private long lastTime;
	
	/**
	 * Creates a new InertVehicle at position 0 with zero speed and current time
	 */
	public InertVehicle() {
		lastTime = System.currentTimeMillis();
	}

	/**
	 * Creates a new InertVehicle at the give start position {@code startPos} with
	 * zero speed and current time
	 * @param startPos - initial position along the moving axis in the given {@code unit}
	 * @param unit - the {@link LengthUnit} {@code startPos} is given in
	 */
	public InertVehicle(double startPos, LengthUnit unit) {
		lastPosition = startPos * LENGTH2METRE[unit.ordinal()];
		lastTime = System.currentTimeMillis();
	}
	
	/**
	 * Computes the momentary position of this vehicle along the axis in the requested
	 * {@code unit}
	 * @param unit - the requested length unit, e.g. {@link LengthUnit#KILOMETRE}
	 * @return the current position of the vehicle along the way
	 */
	public double getPosition(LengthUnit unit)
	{
		long thisTime = System.currentTimeMillis();
		return (lastPosition + (thisTime - lastTime)/1000.0 * speed)/LENGTH2METRE[unit.ordinal()];
	}
	
	/**
	 * Returns the current speed of the vehicle in the requested speed {@code unit}
	 * @param unit - the requested speed unit, e.g. {@link SpeedUnit#METRESPERSECOND}
	 * @return the current speed
	 */
	public double getSpeed(SpeedUnit unit)
	{
		return speed / SPEED2MPS[unit.ordinal()];
	}
	
	/**
	 * Sets the speed to the given {@code newSpeed}
	 * @param newSpeed - the new speed to be imposed in the given {@code unit}
	 * @param unit - the speed unit the {@code newSpeed} is given in
	 */
	public void setSpeed(double newSpeed, SpeedUnit unit)
	{
		lastPosition = getPosition(LengthUnit.METRE);
		lastTime = System.currentTimeMillis();
		speed = newSpeed * SPEED2MPS[unit.ordinal()];
	}

}
