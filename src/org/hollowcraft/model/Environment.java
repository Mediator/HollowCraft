package org.hollowcraft.model;

/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */

/**
 * Represents a world environment
 * @author Caleb champlin
 */
public abstract class Environment implements Cloneable {

	public Environment() { }

	public Object clone() throws CloneNotSupportedException { return super.clone(); }
}
