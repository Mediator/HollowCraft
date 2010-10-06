package org.hollowcraft.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */

/**
 * Represents an absolute location in the game world.
 * @author Caleb Champlin
 */
public final class AbsolutePosition {
	
	/**
	 * X position.
	 */
	private final double x;
	
	/**
	 * Y position.
	 */
	private final double y;
	
	private final double stance;
	
	/**
	 * Z position.
	 */
	private final double z;
	
	/**
	 * Creates a new position.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 */
	public AbsolutePosition(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.stance = 15;
	}
	
	public AbsolutePosition(double x, double y, double z, double stance) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.stance = stance;
	}
	
	/**
	 * Gets the x coordinate.
	 * @return The x coordinate.
	 */
	public double getX() {
		return x;
	}
	
	/**
	 * Gets the y coordinate.
	 * @return The y coordinate.
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * Gets the z coordinate.
	 * @return The z coordinate.
	 */
	public double getZ() {
		return z;
	}
	
	public double getStance() {
		return z;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof AbsolutePosition) {
			AbsolutePosition pos = (AbsolutePosition)other;
			return pos.z == z && pos.y == y && pos.x == x;
		}
		return false;
	}
}
