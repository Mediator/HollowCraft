package org.hollowcraft.model;

public class Animation {
	private int position;
	private int id;
	private int oldPosition;
	private String name;
	public Animation(String name, int id, int position)
	{
		this.name = name;
		this.position = position;
		resetPosition();
	}
	public String getName()
	{
		return this.name;
	}
	public int getAnimationID()
	{
		return id;
	}
	public int getPosition()
	{
		return position;
	}
	public int getOldPosition()
	{
		return oldPosition;
	}
	public void setPosition(int value)
	{
		position = value;
	}
	public void resetPosition()
	{
		position = 0;
		oldPosition = position;
	}
}
