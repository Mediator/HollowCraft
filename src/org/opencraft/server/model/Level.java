package org.opencraft.server.model;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, S�ren Enevoldsen and Brett Russell.
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
import org.opencraft.server.model.Environment;
import java.util.logging.Logger;
import java.lang.Math;

/**
 * Represents the actual level.
 * @author Graham Edgecombe
 * @author Brett Russell
 */
public final class Level {
	
	protected String m_name = "Untitled";
	protected String m_author = "ACM OpenCraft Crew";
	protected long m_created = 0;
	protected Environment m_env = new Environment();
	protected int m_width = 256;
	protected int m_height = 256;
	protected int m_depth = 64;
	protected byte[][][] m_blocks = new byte[m_width][m_height][m_depth];
	protected short[][] m_lightDepths = new short[m_width][m_height];
	protected Rotation m_spawnRotation = new Rotation(0, 0);;
	protected Position m_spawnPosition = new Position(m_width*16, m_height*16, m_depth*32);;
	/** The active "thinking" blocks on the map. */
	protected Map<Integer, ArrayDeque<Position>> m_activeBlocks = new HashMap<Integer, ArrayDeque<Position>>();
	/** The timers for the active "thinking" blocks on the map. */
	protected Map<Integer, Long> m_activeTimers = new HashMap<Integer, Long>();
	/** A queue of positions to update at the next tick. */
	protected Queue<Position> m_updateQueue = new ArrayDeque<Position>();
	protected static final Logger m_logger = Logger.getLogger(Level.class.getName());
	
	public Level() {
		GenLevel();
	}

	public Level(String name, String author, long createdOn, int width, int height, int depth, byte[][][] blocks, Environment env, Position spawnPos) {
		m_name = name;
		m_author = author;
		m_created = createdOn;
		m_env = env;
		m_width = width;
		m_height = height;
		m_depth = depth;
		m_blocks = blocks;
		m_lightDepths = new short[m_width][m_height];
		m_spawnPosition = spawnPos;
		m_spawnRotation = new Rotation(0, 0);
		recalculateAllLightDepths();
		//GenLevel();
	}

	public void GenLevel() {
		m_width = 256;
		m_height = 256;
		m_depth = 64;
		m_blocks = new byte[m_width][m_height][m_depth];
		m_lightDepths = new short[m_width][m_height];
		m_spawnPosition = new Position(m_width*16, m_height*16, m_depth*32);
		m_spawnRotation = new Rotation(0, 0);
		for (int i = 0; i < 256; i++) {
			BlockDefinition b = BlockManager.getBlockManager().getBlock(i);
			if (b != null && b.doesThink()) {
				m_activeBlocks.put(i, new ArrayDeque<Position>());
				m_activeTimers.put(i, System.currentTimeMillis());
			}
		}


		Builder b = new Builder(m_width, m_height, m_depth);
		b.sculptHills(10000, 7);
		b.generateCaverns(100);
		b.buildLavaBed(2);
		b.carveLake();
		m_blocks = b.getBlocks();
	
		for (int x = 0;x < m_width; x++) {
			m_logger.info("Activating ocean: " + (x * 2) + "/" + (m_width * 2 + m_height * 2));
			queueTileUpdate(x, 0, m_depth/2 - 1);
			queueTileUpdate(x, m_height - 1, m_depth/2 - 1);
		}

		for (int y = 0;y < m_height; y++) {
			m_logger.info("Activating ocean: " + (y * 2 + m_width * 2) + "/" + (m_width * 2 + m_height * 2));
			queueTileUpdate(0, y, m_depth/2 - 1);
			queueTileUpdate(m_width - 1, y, m_depth/2 - 1);
		}

		recalculateAllLightDepths();
	}
	
	/**
	 * Recalculates all light depths. WARNING: this is a costly function and
	 * should only be used when it really is necessary.
	 */
	public void recalculateAllLightDepths() {
		for (int x = 0; x < m_width; x++) {
			for (int y = 0; y < m_height; y++) {
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
		for (int z = m_depth - 1; z >= 0; z--) {
			if (BlockManager.getBlockManager().getBlock(m_blocks[x][y][z]).doesBlockLight()) {
				m_lightDepths[x][y] = (short) z;
				return;
			}
		}
		m_lightDepths[x][y] = (short) -1;
	}
	
	/**
	 * Manually assign a light depth to a given Cartesian coordinate.
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param depth The lowest-lit block.
	 */
	public void assignLightDepth(int x, int y, int depth) {
		if (depth > m_height)
			return;
		m_lightDepths[x][y] = (short) depth;
	}
	
	/**
	 * Gets the light depth at the specific coordinate.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @return The light depth.
	 */
	public int getLightDepth(int x, int y) {
		return m_lightDepths[x][y];
	}
	
	/**
	 * Performs physics updates on queued blocks.
	 */
	public void applyBlockBehaviour() {
		Queue<Position> currentQueue = new ArrayDeque<Position>(m_updateQueue);
		m_updateQueue.clear();
		for (Position pos : currentQueue) {
			if (BlockManager.getBlockManager().getBlock(this.getBlock(pos.getX(), pos.getY(), pos.getZ())).hasGravity()) {
				if (!blockIsStable(pos.getX(), pos.getY(), pos.getZ())) {
					setBlock(pos.getX(), pos.getY(), pos.getZ(), BlockConstants.AIR);
					setBlock(pos.getX(), pos.getY(), pos.getZ()- 1, getBlock(pos.getX(), pos.getY(), pos.getZ()));
				}
			}
			BlockManager.getBlockManager().getBlock(this.getBlock(pos.getX(), pos.getY(), pos.getZ())).behavePassive(this, pos.getX(), pos.getY(), pos.getZ());
		}
		// we only process up to 20 of each type of thinking block every tick,
		// or we'd probably be here all day.
		for (int type = 0; type < 256; type++) {
			if (m_activeBlocks.containsKey(type)) {
				if (System.currentTimeMillis() - m_activeTimers.get(type) > BlockManager.getBlockManager().getBlock(type).getTimer()) {
					int cyclesThisTick = (m_activeBlocks.get(type).size() > 600 ? 600 : m_activeBlocks.get(type).size());
					for (int i = 0; i < cyclesThisTick; i++) {
						Position pos = m_activeBlocks.get(type).poll();
						if (pos == null)
							break;
						// the block that occupies this space might have
						// changed.
						if (getBlock(pos.getX(), pos.getY(), pos.getZ()) == type) {
							// World.getWorld().broadcast("Processing thinker at ("+pos.getX()+","+pos.getY()+","+pos.getZ()+")");
							BlockManager.getBlockManager().getBlock(type).behaveSchedule(this, pos.getX(), pos.getY(), pos.getZ());
						}
					}
					m_activeTimers.put(type, System.currentTimeMillis());
				}
			}
		}
	}
	
	public String getName() {
		return m_name;
	}

	public byte[][][] getBlocks() {
		return m_blocks;
	}
	
	public int getWidth() {
		return m_width;
	}
	
	public int getHeight() {
		return m_height;
	}
	
	public int getDepth() {
		return m_depth;
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
		if (x < 0 || y < 0 || z < 0 || x >= m_width || y >= m_height || z >= m_depth) {
			return;
		}
		byte formerBlock = getBlock(x, y, z);
		m_blocks[x][y][z] = (byte) type;
		for (Player player : World.getWorld().getPlayerList().getPlayers()) {
			player.getSession().getActionSender().sendBlock(x, y, z, (byte) type);
		}
		if (updateSelf) {
			queueTileUpdate(x, y, z);
		}
		if (type == 0) {
			BlockManager.getBlockManager().getBlock(formerBlock).behaveDestruct(this, x, y, z);
			updateNeighboursAt(x, y, z);
			if (getLightDepth(x, y) == z) {
				recalculateLightDepth(x, y);
				scheduleZPlantThink(x, y, z);
			}
		}
		if (BlockManager.getBlockManager().getBlock(type).doesThink()) {
			m_activeBlocks.get(type).add(new Position(x, y, z));
		}
		if (BlockManager.getBlockManager().getBlock(type).doesBlockLight()) {
			assignLightDepth(x, y, z);
			scheduleZPlantThink(x, y, z);
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
		if (x >= 0 && y >= 0 && z >= 0 && x < m_width && y < m_height && z < m_depth) {
			Position pos = new Position(x, y, z);
			if (!m_updateQueue.contains(pos)) {
				m_updateQueue.add(pos);
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
		if (x >= 0 && y >= 0 && z >= 0 && x < m_width && y < m_height && z < m_depth) {
			int blockAt = getBlock(x, y, z);
			if (BlockManager.getBlockManager().getBlock(blockAt).doesThink()) {
				m_activeBlocks.get(blockAt).add(new Position(x, y, z));
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
		if (x >= 0 && y >= 0 && z >= 0 && x < m_width && y < m_height && z < m_depth) {
			return m_blocks[x][y][z];
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
	
	public void setSpawnRotation(Rotation spawnRotation) {
		m_spawnRotation = spawnRotation;
	}

	public Rotation getSpawnRotation() {
		return m_spawnRotation;
	}
	
	public void setSpawnPosition(Position spawnPosition) {
		m_spawnPosition = spawnPosition;
	}
	
	public Position getSpawnPosition() {
		return m_spawnPosition;
	}
	
}
