package org.opencraft.model;

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
import java.util.ArrayList;
import org.slf4j.*;
import org.opencraft.server.model.OnBlockChangeHandler;
import org.opencraft.server.model.Builder;

import org.opencraft.model.Environment;
import org.opencraft.server.model.impl.builders.LandscapeBuilder;

/**
 * Represents the actual level.
 * @author Graham Edgecombe
 * @author Brett Russell
 */
public class Level {
	
	protected String m_title;
	protected String m_author;
	protected long m_created;
	protected Environment m_env;
	protected OnBlockChangeHandler m_handler;

	public void setOnBlockChangeHandler(OnBlockChangeHandler handler) {
		m_handler = handler;
	}

	protected String m_fileType;

	protected int m_width;
	protected int m_height;
	protected int m_depth;
	protected byte[][][] m_blocks;
	protected byte[][][] m_data;
	protected Rotation m_spawnRotation;
	protected Position m_spawnPosition;


	protected static final Logger m_logger = LoggerFactory.getLogger(Level.class);

	public Level() {
	}

	/**
	 * Copy constructor
	 */
	public Level(Level other) {
		m_title = other.m_title;
		m_author = other.m_author;
		m_created = other.m_created;
		try{
			m_env = (Environment) other.m_env.clone();
		} catch (CloneNotSupportedException e) {
			m_logger.info("Error: {}", e);
			m_env = null;
		}
		m_fileType = other.m_fileType;
		m_width = other.m_width;
		m_height = other.m_height;
		m_depth = other.m_depth;
		m_blocks = other.m_blocks.clone();
		m_data = other.m_blocks.clone();
		m_spawnRotation = other.m_spawnRotation;
		m_spawnPosition = other.m_spawnPosition;
	}

	/**
	 * Sets a block and updates the neighbours.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @param type The type id.
	 */
	public void setBlock(int x, int y, int z, int type) {
		m_blocks[x][y][z] = (byte) type;
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
		int strength = BlockManager.getBlockManager().getBlock(getBlock(x,y,z)).getStrength();
		if (strength == -1)
			return true;
		if (!BlockManager.getBlockManager().getBlock(getBlock(x, y, z)).isSolid() && !BlockManager.getBlockManager().getBlock(getBlock(x, y, z)).isPlant())
			return false;
		if (BlockManager.getBlockManager().getBlock(getBlock(x, y, z)).isLiquid())
			return false;
		if (distance > 40)
			return true;
		if (blockIsStable(x, y, z-1, visited, distance+1))
			return true;
		if (strength == 0)
			return false;
		if (distance > strength)
			return false;
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

	// TODO: Delete these and use title
	public String getName() {
		return m_title;
	}

	// TODO: Delete these and use title
	public void setName(String name) {
		m_logger.debug("Setting name to {}", name);
		m_title = name;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public String getTitle() {
		return m_title;
	}

	public String getAuthor() {
		return m_author;
	}

	public void setAuthor(String author) {
		m_author = author;
	}

	public byte[][][] getBlocks() {
		return m_blocks;
	}

	public byte[][][] getData() {
		return m_data;
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

	public void setBlocks(byte[][][] blocks, byte[][][] data, int width, int height, int depth) {
		m_logger.trace("Setting size of {}x{}x"+depth, width, height);
		m_blocks = blocks;
		m_data = data;
		m_width = width;
		m_height = height;
		m_depth = depth;
	}

	public long getCreationDate() {
		return m_created;
	}

	public void setCreationDate(long date) {
		m_created = date;
	}

	public Environment getEnvironment() {
		return m_env;
	}

	public void setEnvironment(Environment env) {
		m_env = env;
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
