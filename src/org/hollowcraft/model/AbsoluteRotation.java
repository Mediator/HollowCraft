package org.hollowcraft.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */

/**
 * Represents an absolute rotation in the game world.
 * @author Caleb champlin
 */
public final class AbsoluteRotation {
	
	/**
	 * The rotation.
	 */
	private final float rotation;
	
	/**
	 * The look.
	 */
	private final float look;
	
	/**
	 * Creates the rotation.
	 * @param rotation The rotation.
	 * @param look The look value.
	 */
	public AbsoluteRotation(float rotation, float look) {
		this.rotation = rotation;
		this.look = look;
	}
	
	/**
	 * Gets the rotation.
	 * @return The rotation.
	 */
	public float getRotation() {
		return rotation;
	}
	
	/**
	 * Gets the look value.
	 * @return The look value.
	 */
	public float getLook() {
		return look;
	}
	
}
