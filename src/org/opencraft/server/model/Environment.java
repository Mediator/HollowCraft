package org.opencraft.server.model;

import org.opencraft.server.model.BlockConstants;

public  class Environment {
	protected short SurroundingGroundHeight = 0;			// Height of surrounding ground (in blocks)
	protected byte  SurroundingGroundType = BlockConstants.ADMINIUM;// Block ID of surrounding ground
	protected short SurroundingWaterHeight = 1;			// Height of surrounding water (in blocks)
	protected byte  SurroundingWaterType = BlockConstants.WATER;	// Block ID of surrounding water
	protected short TimeOfDay = 0;					// ?? Found inspecting an official NBT file ??
	protected short CloudHeight = 50;				// Height of the cloud layer (in blocks)
	protected int   CloudColor = 0xFFFFFF;				// Hexadecimal value for the color of the clouds
	protected int   SkyColor = 0x0040FF;				// Hexadecimal value for the color of the sky
	protected int   FogColor = 0xCCCCCC;				// Hexadecimal value for the color of the fog
	protected byte  SkyBrightness = 75;				// The brightness of the sky, from 0 to 100

	public Environment() { }

	// Mutators for SurroundingGroundHeight
	public short getSurroundingGroundHeight() {
		return SurroundingGroundHeight;
	}

	public void setSurroundingGroundHeight(short value) {
		SurroundingGroundHeight = value;
	}

	// Mutators for SurroundingGroundType
	public byte getSurroundingGroundType() {
		return SurroundingGroundType;
	}

	public void setSurroundingGroundType(byte value) {
		SurroundingGroundType = value;
	}

	// Mutators for SurroundingWaterHeight
	public short getSurroundingWaterHeight() {
		return SurroundingWaterHeight;
	}

	public void setSurroundingWaterHeight(short value) {
		SurroundingWaterHeight = value;
	}

	// Mutators for SurroundingWaterType
	public byte getSurroundingWaterType() {
		return SurroundingWaterType;
	}

	public void setSurroundingWaterType(byte value) {
		SurroundingWaterType = value;
	}

	// Mutators for CloudHeight
	public short getCloudHeight() {
		return CloudHeight;
	}

	public void setCloudHeight(short value) {
		CloudHeight = value;
	}

	// Mutators for CloudColor
	public int getCloudColor() {
		return CloudColor;
	}

	public void setCloudColor(int value) {
		CloudColor = value;
	}

	// Mutators for SkyColor
	public int getSkyColor() {
		return SkyColor;
	}

	public void setSkyColor(int value) {
		SkyColor = value;
	}

	// Mutators for FogColor
	public int getFogColor() {
		return FogColor;
	}

	public void setFogColor(int value) {
		FogColor = value;
	}

	// Mutators for SkyBrightness
	public byte getSkyBrightness() {
		return SkyBrightness;
	}

	public void setSkyBrightness(byte value) {
		SkyBrightness = value;
	}
	
	// Mutators for TimeofDat
	public short getTimeofDay() {
		return TimeOfDay;
	}

	public void setTimeOfDay(short value) {
		TimeOfDay = value;
	}
}
