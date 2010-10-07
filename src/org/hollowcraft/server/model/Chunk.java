package org.hollowcraft.server.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.hollowcraft.model.AlphaLevel;
import org.hollowcraft.model.Point;
import org.hollowcraft.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Handles chunks
 * @author Caleb Champlin
 */

public class Chunk {
	protected static final Logger logger = LoggerFactory.getLogger(Chunk.class);
	/** The active "thinking" blocks on the map. */
	protected Map<Integer, ArrayDeque<Position>> m_activeBlocks = new HashMap<Integer, ArrayDeque<Position>>();
	
	/** The timers for the active "thinking" blocks on the map. */
	protected Map<Integer, Long> m_activeTimers = new HashMap<Integer, Long>();
	
	/** A queue of positions to update at the next tick. */
	protected Queue<Position> m_updateQueue = new ArrayDeque<Position>();
		
	protected int x,z;
	protected long lastUpdate;
	protected int[] chunkBlocks = new int[16 * 128 * 16];
	protected int[] chunkBlocksType = new int[16 * 128 * 16];
	protected int[] chunkBlocksMeta = new int[(16 * 128 * 16) / 2];
	protected int[] chunkBlocksBlockLight = new int[(16 * 128 * 16) / 2];
	protected int[] chunkBlocksSkyLight = new int[(16 * 128 * 16) / 2];
	protected byte[] heightMap = new byte[16 * 16];
	private final Player[] playersWithChunk = new Player[64];
	private byte terrainPopulated;
	public Chunk (int x, int z)
	{
		this.x = x;
		this.z = z;
	}
	
	public void addPlayerToChunk(Player player)
	{
		for (int i = 0; i < playersWithChunk.length; i++) {
			if (playersWithChunk[i] == null) {
				playersWithChunk[i] = player;
			}
		}
	}
	public void removePlayerFromChunk(Player player)
	{
		for (int i = 0; i < playersWithChunk.length; i++) {
			if (playersWithChunk[i] == player) {
				playersWithChunk[i] = null;
			}
		}
	}
	public void calculateLighting()
	{
	
	}
	public int getX()
	{
		return x;
	}
	public void setX(int value)
	{
		this.x = value;
	}
	public int getZ()
	{
		return z;
	}
	public void setZ(int value)
	{
		this.z = value;
	}
	public long getLastUpdate()
	{
		return lastUpdate;
	}
	public void setlastUpdate(long value)
	{
		lastUpdate = value;
	}
	public byte getTerrainPopulated()
	{
		return terrainPopulated;
	}
	public void setTerrainPopulated(byte value)
	{
		terrainPopulated = value;
	}
	public byte[] getHeightMap()
	{
		return heightMap;
	}
	public void setHeightMap(byte[] value)
	{
		heightMap = value;
	}
	public int[] getChunkBlocks()
	{
		return chunkBlocks;
	}
	public int getChunkBlock(int key)
	{
		return chunkBlocks[key];
	}
	public static Point getChunkPointFromBlockPoint(Position pos)
	{
        int chunkX = pos.getX() / 16;
        int chunkZ = pos.getY() / 16;
        if ((pos.getX() % 16) < 0 && Math.abs((pos.getX() % 16)) != 0) chunkX--;
        if ((pos.getZ() % 16) < 0 && Math.abs((pos.getZ() % 16)) != 0) chunkZ--;
        return new Point(chunkX, chunkZ);
	}
	public static Position getBlockPositionInChunk(Position pos)
	{
		 int blockX = pos.getX() / 16;
		 int blockZ = pos.getZ() / 16;
		 blockX = (blockX % 16);
		 blockZ = (blockZ % 16);
		 if (blockX < 0) 
			 blockX = Math.abs(blockX + 16);
		 if (blockZ < 0) 
			 blockZ = Math.abs(blockZ + 16);
		 return new Position(blockX, pos.getY(), blockZ);
	}
	public static int getChunkKey(Position pos)
	{
		return pos.getY() + (pos.getZ() * 128 + (pos.getX() * 128 * 16));
	}
	public int getNumberOfChunkBlocks()
	{
		return chunkBlocks.length;
	}
	
	
	public void setChunkBlockType(int key, int value)
	{
		chunkBlocksType[key] = value;
	}
	public void setChunkBlockMeta(int key, int value)
	{
		chunkBlocksMeta[key] = value;
	}
	public void setChunkBlockBlockLight(int key, int value)
	{
		chunkBlocksBlockLight[key] = value;
	}
	public void setChunkBlockSkyLight(int key, int value)
	{
		chunkBlocksSkyLight[key] = value;
	}
	public int[] getChunkBlocksType()
	{
		return chunkBlocksType;
	}
	public int[] getChunkBlocksMeta()
	{
		return chunkBlocksMeta;
	}
	public int[] getChunkBlocksBlockLight()
	{
		return chunkBlocksBlockLight;
	}
	public int[] getChunkBlocksSkyLight()
	{
		return chunkBlocksSkyLight;
	}
	
	public void setChunkBlock(int key, int value)
	{
		chunkBlocks[key] = value;
	}
	public void setChunkBlock(int key, byte byte1, byte byte2, byte byte3)
	{
		chunkBlocks[key] = bytesToInt(byte1, byte2, byte3);
	}
	public void setChunkBlock(int key, byte byte1, byte byte2, byte byte3, byte byte4)
	{
		assert(key >= 0);
		chunkBlocks[key] = bytesToInt(byte1, byte2, byte3, byte4);
		//chunkBlocks[key] = bytesToInt(byte1, byte2, byte3, byte4);
	}
	private int bytesToInt(byte byte1, byte byte2, byte byte3)
	{
		return bytesToInt(byte1, byte2, byte3, (byte)0);
	}
	private int bytesToInt(byte byte1, byte byte2, byte byte3, byte byte4)
	{
		return ((byte1 & 0xFF) << 24)
	      | ((byte2 & 0xFF) << 16)
	      | ((byte3 & 0xFF) << 8)
	      | (byte4 & 0xFF);	
	}

	public boolean hasPlayers() {
		return (playersWithChunk.length > 0);
	}
	
}
