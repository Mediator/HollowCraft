package org.opencraft.server.model;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, Søren Enevoldsen and Brett Russell.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *       
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *       
 *     * Neither the name of the OpenCraft nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.ArrayList;
import org.opencraft.server.task.ScheduledTask;
import org.opencraft.server.task.TaskQueue;
import java.util.logging.Logger;
import java.lang.Math;

/**
 * Represents the actual level.
 * @author Graham Edgecombe
 * @author Brett Russell
 */
public final class Level {
	
	/**
	 * The level width.
	 */
	private int width;
	
	/**
	 * The level height.
	 */
	private int height;
	
	/**
	 * The level depth.
	 */
	private int depth;
	
	/**
	 * The blocks.
	 */
	private byte[][][] blocks;
	
	/**
	 * Light depth array.
	 */
	private short[][] lightDepths;
	
	/**
	 * The spawn rotation.
	 */
	private Rotation spawnRotation;
	
	/**
	 * The spawn position.
	 */
	private Position spawnPosition;
	
	/**
	 * The active "thinking" blocks on the map.
	 */
	private Map<Integer, ArrayDeque<Position>> activeBlocks = new HashMap<Integer, ArrayDeque<Position>>();
	
	/**
	 * The timers for the active "thinking" blocks on the map.
	 */
	private Map<Integer, Long> activeTimers = new HashMap<Integer, Long>();
	
	/**
	 * A queue of positions to update at the next tick.
	 */
	private Queue<Position> updateQueue = new ArrayDeque<Position>();

	private static final Logger logger = Logger.getLogger(Level.class.getName());
	
	/**
	 * Generates a level.
	 */
	public Level() {
		this.width = 256;
		this.height = 256;
		this.depth = 64;
		this.blocks = new byte[width][height][depth];
		this.lightDepths = new short[width][height];
		this.spawnPosition = new Position(width*16, height*16, depth*32);
		this.spawnRotation = new Rotation(0, 0);
		for (int i = 0; i < 256; i++) {
			BlockDefinition b = BlockManager.getBlockManager().getBlock(i);
			if (b != null && b.doesThink()) {
				activeBlocks.put(i, new ArrayDeque<Position>());
				activeTimers.put(i, System.currentTimeMillis());
			}
		}
		
		Random random = new Random();
		int[][] heights = new int[width][height];
		int maxHeight = 1;
		int iterations = 1000;
		for(int i = 0; i < iterations; i++) {
			if (i % 1000 == 0)
				logger.info("Raising terrain: "+i+"/"+iterations);
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int ry = random.nextInt(10) + 4;
			int rx = random.nextInt(10) + 4;
			for(int j = 0; j < width; j++) {
				for(int k = 0; k < height; k++) {
					int mod = (rx * ry) - (k - x) * (k - x) - (j - y) * (j - y);
					if(mod > 0) {
						heights[j][k] += mod;
						if(heights[j][k] > maxHeight) {
							maxHeight = heights[j][k];
						}
					}
				}
			}
		}

		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				//int h = (depth / 2) + (heights[x][y] * (depth / 2) / maxHeight);
				int h = (depth/2) + (heights[x][y] * (depth /2) / maxHeight)/2 - 2;
				int d = random.nextInt(8) - 4;
				for(int z = 0; z < h; z++) {
					int type = BlockConstants.DIRT;
					if(z == (h - 1)) {
						type = BlockConstants.GRASS;
					} else if(z <= (depth / 2 + d)) {
						type = BlockConstants.STONE;
					}
					blocks[x][y][z] = (byte) type;
				}
			}
		}

		int bubbleCount = 100;
		for (int i = 0; i < bubbleCount;i++) {
			logger.info("Generating underground erosion bubbles: "+i+"/"+bubbleCount);
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int z = random.nextInt(depth/4);
			int radius = random.nextInt(70)+50;
			radius = 6;
			int type = random.nextInt(100);
			if (type > 90)
				type = BlockConstants.LAVA;
			else if (type > 45)
				type = BlockConstants.AIR;
			else
				type = BlockConstants.WATER;
			for (int m = 0;m < 2; m++) {
				BUBBLE_GEN: for(int j = x-radius;j<x+radius*2;j++) {
					if (j < 0)
						j = 0;
					if (j >= width)
						break BUBBLE_GEN;
					for(int k = y-radius;k<y+radius*2;k++) {
						if (k < 0)
							k = 0;
						if (k >= width)
							break BUBBLE_GEN;
						for (int l = z-radius;l<z+radius;l++) {
							if (l < 0)
								l = 0;
							if (l >= depth)
								break BUBBLE_GEN;
							double distance = Math.sqrt(Math.pow(j-x, 2)+Math.pow(k-y, 2)+Math.pow(l-z, 2));
							if (Math.abs(distance/radius) <= Math.abs(random.nextGaussian())) {
								blocks[j][k][l] = (byte) type;
							}
						}
					}
				}
				x++;
			}
		}

		for (int x = 0;x < width; x++) {
			logger.info("Activating ocean: "+(x*2)+"/"+(width*2+height*2));
			queueTileUpdate(x, 0, depth/2-1);
			queueTileUpdate(x, height-1, depth/2-1);
		}

		for (int y = 0;y < height; y++) {
			logger.info("Activating ocean: "+(y*2+width*2)+"/"+(width*2+height*2));
			queueTileUpdate(0, y, depth/2-1);
			queueTileUpdate(width-1, y, depth/2-1);
		}

		/*for (int z = 0; z < depth / 2; z++) {
			logger.info("Building ocean: "+z+"/"+(depth/2));
			for (int x = 0;x < width; x++) {
				blocks[x][0][z] = (byte) BlockConstants.WATER;
				blocks[x][height-1][z] = (byte) BlockConstants.WATER;
				queueActiveBlockUpdate(x, 0, z);
				queueActiveBlockUpdate(x, height-1, z);
			}
			for (int y = 0; y < height; y++) {
				blocks[0][y][z] = (byte) BlockConstants.WATER;
				blocks[width-1][y][z] = (byte) BlockConstants.WATER;
				queueActiveBlockUpdate(0, y, z);
				queueActiveBlockUpdate(width-1, y, z);
			}
		}*/

		for(int x = 0;x < width; x++) {
			logger.info("Building lava bed: "+(x*height)+"/"+(width*height));
			for (int y = 0; y < height; y++ ) {
				blocks[x][y][0] = (byte) BlockConstants.LAVA;
			}
		}

		recalculateAllLightDepths();
	}
	
	/**
	 * Recalculates all light depths. WARNING: this is a costly function and
	 * should only be used when it really is necessary.
	 */
	public void recalculateAllLightDepths() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				recalculateLightDepth(x, y);
			}
		}
	}
	
	/**
	 * Recalculates the light depth of the specified coordinates.
	 * @param x The x coordinates.
	 * @param y The y coordinates.
	 */
	public void recalculateLightDepth(int x, int y) {
		for (int z = depth - 1; z >= 0; z--) {
			if (BlockManager.getBlockManager().getBlock(blocks[x][y][z]).doesBlockLight()) {
				lightDepths[x][y] = (short) z;
				return;
			}
		}
		lightDepths[x][y] = (short) -1;
	}
	
	/**
	 * Manually assign a light depth to a given Cartesian coordinate.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param depth The lowest-lit block.
	 */
	public void assignLightDepth(int x, int y, int depth) {
		if (depth > this.height)
			return;
		lightDepths[x][y] = (short) depth;
	}
	
	/**
	 * Gets the light depth at the specific coordinate.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @return The light depth.
	 */
	public int getLightDepth(int x, int y) {
		return lightDepths[x][y];
	}
	
	/**
	 * Performs physics updates on queued blocks.
	 */
	public void applyBlockBehaviour() {
		Queue<Position> currentQueue = new ArrayDeque<Position>(updateQueue);
		updateQueue.clear();
		for (Position pos : currentQueue) {
			if (BlockManager.getBlockManager().getBlock(this.getBlock(pos.getX(), pos.getY(), pos.getZ())).hasGravity()) {
				if (!blockIsStable(pos.getX(), pos.getY(), pos.getZ())) {
					setBlock(pos.getX(), pos.getY(), pos.getZ(), BlockConstants.AIR);
					setBlock(pos.getX(), pos.getY(), pos.getZ()- 1, this.getBlock(pos.getX(), pos.getY(), pos.getZ()));
				}
			}
			BlockManager.getBlockManager().getBlock(this.getBlock(pos.getX(), pos.getY(), pos.getZ())).behavePassive(this, pos.getX(), pos.getY(), pos.getZ());
		}
		// we only process up to 20 of each type of thinking block every tick,
		// or we'd probably be here all day.
		for (int type = 0; type < 256; type++) {
			if (activeBlocks.containsKey(type)) {
				if (System.currentTimeMillis() - activeTimers.get(type) > BlockManager.getBlockManager().getBlock(type).getTimer()) {
					int cyclesThisTick = (activeBlocks.get(type).size() > 1000 ? 1000 : activeBlocks.get(type).size());
					for (int i = 0; i < cyclesThisTick; i++) {
						Position pos = activeBlocks.get(type).poll();
						if (pos == null)
							break;
						// the block that occupies this space might have
						// changed.
						if (this.getBlock(pos.getX(), pos.getY(), pos.getZ()) == type) {
							// World.getWorld().broadcast("Processing thinker at ("+pos.getX()+","+pos.getY()+","+pos.getZ()+")");
							BlockManager.getBlockManager().getBlock(type).behaveSchedule(this, pos.getX(), pos.getY(), pos.getZ());
						}
					}
					activeTimers.put(type, System.currentTimeMillis());
				}
			}
		}
	}
	
	/**
	 * Gets all of the blocks.
	 * @return All of the blocks.
	 */
	public byte[][][] getBlocks() {
		return blocks;
	}
	
	/**
	 * Gets the width of the level.
	 * @return The width of the level.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Gets the height of the level.
	 * @return The height of the level.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Gets the depth of the level.
	 * @return The depth of the level.
	 */
	public int getDepth() {
		return depth;
	}
	
	/**
	 * Sets a block and updates the neighbours.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @param type The type id.
	 */
	public void setBlock(int x, int y, int z, int type) {
		setBlock(x, y, z, type, true);
	}
	
	/**
	 * Sets a block.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @param type The type id.
	 * @param updateSelf Update self flag.
	 */
	public void setBlock(int x, int y, int z, int type, boolean updateSelf) {
		if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth) {
			return;
		}
		byte formerBlock = this.getBlock(x, y, z);
		blocks[x][y][z] = (byte) type;
		for (Player player : World.getWorld().getPlayerList().getPlayers()) {
			player.getSession().getActionSender().sendBlock(x, y, z, (byte) type);
		}
		if (updateSelf) {
			queueTileUpdate(x, y, z);
		}
		if (type == 0) {
			BlockManager.getBlockManager().getBlock(formerBlock).behaveDestruct(this, x, y, z);
			updateNeighboursAt(x, y, z);
			if (this.getLightDepth(x, y) == z) {
				this.recalculateLightDepth(x, y);
				this.scheduleZPlantThink(x, y, z);
			}
		}
		if (BlockManager.getBlockManager().getBlock(type).doesThink()) {
			activeBlocks.get(type).add(new Position(x, y, z));
		}
		if (BlockManager.getBlockManager().getBlock(type).doesBlockLight()) {
			this.assignLightDepth(x, y, z);
			this.scheduleZPlantThink(x, y, z);
		}
		
	}
	
	/**
	 * Schedules plants to think in a Z coordinate if a block above them
	 * changed.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	public void scheduleZPlantThink(int x, int y, int z) {
		for (int i = z - 1; i > 0; i--) {
			if (BlockManager.getBlockManager().getBlock(this.getBlock(x, y, i)).isPlant()) {
				queueActiveBlockUpdate(x, y, i);
			}
			if (BlockManager.getBlockManager().getBlock(this.getBlock(x, y, i)).doesBlockLight()) {
				return;
			}
		}
	}
	
	/**
	 * Updates neighbours at the specified coordinate.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	private void updateNeighboursAt(int x, int y, int z) {
		queueTileUpdate(x - 1, y, z);
		queueTileUpdate(x, y - 1, z);
		queueTileUpdate(x + 1, y, z);
		queueTileUpdate(x, y + 1, z);
		queueTileUpdate(x, y, z - 1);
		queueTileUpdate(x, y, z + 1);
		recalculateLightDepth(x, y);
	}
	
	/**
	 * Queues a tile update.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	private void queueTileUpdate(int x, int y, int z) {
		if (x >= 0 && y >= 0 && z >= 0 && x < width && y < height && z < depth) {
			Position pos = new Position(x, y, z);
			if (!updateQueue.contains(pos)) {
				updateQueue.add(pos);
			}
		}
	}
	
	/**
	 * Forces a tile update to be queued. Use with caution.
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 */
	public void queueActiveBlockUpdate(int x, int y, int z) {
		if (x >= 0 && y >= 0 && z >= 0 && x < width && y < height && z < depth) {
			int blockAt = this.getBlock(x, y, z);
			if (BlockManager.getBlockManager().getBlock(blockAt).doesThink()) {
				activeBlocks.get(blockAt).add(new Position(x, y, z));
			}
		}
	}
	
	/**
	 * Gets a block.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return The type id.
	 */
	public byte getBlock(int x, int y, int z) {
		if (x >= 0 && y >= 0 && z >= 0 && x < width && y < height && z < depth) {
			return blocks[x][y][z];
		} else {
			return (byte) BlockConstants.BEDROCK;
		}
	}


	/**
	 * Returns if a block is somehow touching bedrock
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return If a block is stable
	 */
	public boolean blockIsStable(int x, int y, int z) {
		return blockIsStable(x, y, z, new ArrayList<Position>(), 0);
	}

	private boolean blockIsStable(int x, int y, int z, ArrayList<Position> visited, int distance) {
		Position p = new Position(x, y, z);
		for (Position pos : visited) {
			if (pos.equals(p)) {
				visited.add(p);
				return false;
			}
		}
		visited.add(p);
		if (distance > 30)
			return true;
		if (getBlock(x, y, z) == BlockConstants.BEDROCK)
			return true;
		if (!BlockManager.getBlockManager().getBlock(getBlock(x, y, z)).isSolid())
			return false;
		if (BlockManager.getBlockManager().getBlock(getBlock(x, y, z)).isLiquid())
			return false;
		if (blockIsStable(x, y, z-1, visited, distance+1))
			return true;
		if (blockIsStable(x+1, y, z, visited, distance+1))
			return true;
		if (blockIsStable(x-1, y, z, visited, distance+1))
			return true;
		if (blockIsStable(x, y+1, z, visited, distance+1))
			return true;
		if (blockIsStable(x, y-1, z, visited, distance+1))
			return true;
		if (blockIsStable(x, y, z+1, visited, distance+1))
			return true;
		return false;
	}
	
	/**
	 * Set the rotation of the character when spawned.
	 * @param spawnRotation The rotation.
	 */
	public void setSpawnRotation(Rotation spawnRotation) {
		this.spawnRotation = spawnRotation;
	}
	
	/**
	 * Get the spawning rotation.
	 * @return The spawning rotation.
	 */
	public Rotation getSpawnRotation() {
		return spawnRotation;
	}
	
	/**
	 * Set the spawn position.
	 * @param spawnPosition The spawn position.
	 */
	public void setSpawnPosition(Position spawnPosition) {
		this.spawnPosition = spawnPosition;
	}
	
	/**
	 * Get the spawn position.
	 * @return The spawn position.
	 */
	public Position getSpawnPosition() {
		return spawnPosition;
	}
	
}
