package org.hollowcraft.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
/**
 * Represents a point
 * @author Caleb champlin
 */
public class Point {
	protected int x;
	protected int y;
	public Point (int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	public int getX()
	{
		return x;
	}
	public void setX(int value)
	{
		this.x = value;
	}
	public int getY()
	{
		return y;
	}
	public void setY(int value)
	{
		this.y = value;
	}
	public boolean equals(Object value) {
		return ((value instanceof Point) &&
				(this.y == ((Point)value).y) &&
				(this.x == ((Point)value).x));
	}
	public int hashCode()
	{
		return x * 1000 + y;
	}
	
}
