package org.hollowcraft.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
/**
 * Represents the actual alpha level.
 * @author Caleb Champlin
 */
public class AlphaEnvironment extends Environment {
	protected byte snowCovered = 0;
	protected int spawnX = 0;
	protected int spawnY = 64;
	protected int spawnZ = 0;
	protected long timeOfDay = 0;
	protected long sizeOnDisk = 0;
	protected long randomSeed = 0;
	protected long lastPlayed = 0;
	public AlphaEnvironment() { }

	public Object clone() throws CloneNotSupportedException { return super.clone(); }

	
	public byte getSnowCovered()
	{
		return snowCovered;
	}
	public void setSnowCovered(byte value)
	{
		snowCovered = value;
	}
	public int getSpawnX()
	{
		return spawnX;
	}
	public void setSpawnX(int value)
	{
		spawnX = value;
	}
	public int getSpawnY()
	{
		return spawnY;
	}
	public void setSpawnY(int value)
	{
		spawnY = value;
	}
	public int getSpawnZ()
	{
		return spawnZ;
	}
	public void setSpawnZ(int value)
	{
		spawnZ = value;
	}
	public long getLastPlayed()
	{
		return lastPlayed;
	}
	public void setLastPlayed(long value)
	{
		lastPlayed = value;
	}
	
	public long getSizeOnDisk()
	{
		return sizeOnDisk;
	}
	public void setSizeOnDisk(long value)
	{
		sizeOnDisk = value;
	}
	public long getRandomSeed()
	{
		return randomSeed;
	}
	public void setRandomSeed(long value)
	{
		randomSeed = value;
	}
	
	// Mutators for TimeofDay
	public long getTimeOfDay() {
		return timeOfDay;
	}

	public void setTimeOfDay(long value) {
		timeOfDay = 6000;// value;
	}
}
