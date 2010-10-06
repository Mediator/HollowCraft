package org.hollowcraft.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
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

import org.hollowcraft.model.ClassicEnvironment;
import org.hollowcraft.server.model.Builder;
import org.hollowcraft.server.model.OnBlockChangeHandler;
import org.slf4j.*;


/**
 * Represents the actual level.
 * @author Graham Edgecombe
 * @author Brett Russell
 * @author Caleb Champlin
 */
public class ClassicLevel implements Level {
	
	protected String m_title;
	protected String m_author;
	protected long m_created;
	protected ClassicEnvironment m_env;
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


	protected static final Logger m_logger = LoggerFactory.getLogger(ClassicLevel.class);

	public ClassicLevel() {
	}

	/**
	 * Copy constructor
	 */
	public ClassicLevel(ClassicLevel other) {
		m_author = other.m_author;
		m_created = other.m_created;
		try{
			m_env = (ClassicEnvironment) other.m_env.clone();
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
	public void setBlock(Position pos, int type) {
		m_blocks[pos.getX()][pos.getY()][pos.getZ()] = (byte) type;
	}
	
	/**
	 * Gets a block.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return The type id.
	 */
	public byte getBlock(Position pos) {
		if (pos.getX() >= 0 && pos.getY() >= 0 && pos.getZ() >= 0 && pos.getX() < m_width && pos.getY() < m_height && pos.getZ() < m_depth) {
			return m_blocks[pos.getX()][pos.getY()][pos.getZ()];
		} else {
			return (byte) BlockManager.getBlockManager().getBlock("BEDROCK").getId();
		}
	}


	/**
	 * Returns if a block is somehow touching bedrock
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return If a block is stable
	 */
	public boolean blockIsStable(Position pos) {
		return blockIsStable(pos, new ArrayList<Position>(), 0); 
	}
	private boolean blockIsStable(Position pos, ArrayList<Position> visited, int distance) {
		for (Position p : visited) {
			if (pos.equals(p)) {
				visited.add(p);
				return false;
			}
		}
		visited.add(pos);
		int strength = BlockManager.getBlockManager().getBlock(getBlock(pos)).getStrength();
		if (strength == -1)
			return true;
		if (!BlockManager.getBlockManager().getBlock(getBlock(pos)).isSolid() && !BlockManager.getBlockManager().getBlock(getBlock(pos)).isPlant())
			return false;
		if (BlockManager.getBlockManager().getBlock(getBlock(pos)).isLiquid())
			return false;
		if (distance > 40)
			return true;
		if (blockIsStable(new Position(pos.getX(), pos.getY(), pos.getZ()-1), visited, distance+1))
			return true;
		if (strength == 0)
			return false;
		if (distance > strength)
			return false;
		if (blockIsStable(new Position(pos.getX()+1, pos.getY(), pos.getZ()), visited, distance+1))
			return true;
		if (blockIsStable(new Position(pos.getX()-1, pos.getY(), pos.getZ()), visited, distance+1))
			return true;
		if (blockIsStable(new Position(pos.getX(), pos.getY()+1, pos.getZ()), visited, distance+1))
			return true;
		if (blockIsStable(new Position(pos.getX(), pos.getY()-1, pos.getZ()), visited, distance+1))
			return true;
		if (blockIsStable(new Position(pos.getX(), pos.getY(), pos.getZ()+1), visited, distance+1))
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

	public ClassicEnvironment getEnvironment() {
		return m_env;
	}

	public void setEnvironment(ClassicEnvironment env) {
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

	@Override
	public byte getBlockMeta(Position pos) {
		// TODO Auto-generated method stub
		return 0;
	}
}
