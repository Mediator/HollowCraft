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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayList;

import org.hollowcraft.io.AlphaWorldHandler;
import org.hollowcraft.model.AlphaEnvironment;
import org.hollowcraft.server.model.Builder;
import org.hollowcraft.server.model.Chunk;
import org.hollowcraft.server.model.OnBlockChangeHandler;
import org.slf4j.*;


/**
 * Represents the actual alpha level.
 * @author Caleb Champlin
 */
public class AlphaLevel implements Level {
	
	protected String m_title;
	protected String m_author;
	protected long m_created;
	protected AlphaEnvironment m_env;
	protected OnBlockChangeHandler m_handler;

	public void setOnBlockChangeHandler(OnBlockChangeHandler handler) {
		m_handler = handler;
	}

	protected String worldPath;

	protected Map<Point,Chunk> loadedChunks = new HashMap<Point,Chunk>();


	protected static final Logger m_logger = LoggerFactory.getLogger(AlphaLevel.class);

	public AlphaLevel() {
	}

	/**
	 * Copy constructor
	 */
	public AlphaLevel(AlphaLevel other) {
		m_title = other.m_title;
		m_author = other.m_author;
		m_created = other.m_created;
		loadedChunks = (HashMap<Point,Chunk>)((HashMap<Point,Chunk>)other.loadedChunks).clone();

		try{
			m_env = (AlphaEnvironment) other.m_env.clone();
		} catch (CloneNotSupportedException e) {
			m_logger.info("Error: {}", e);
			m_env = null;
		}
		worldPath = other.worldPath;
	}

	
	   private Chunk generateFlatgrass(int x, int z) {
		   Chunk chunk = new Chunk(x,z);
		   for (int bX = 0; bX < 16; bX++) {
			   for (int bY = 0; bY < 128; bY++) {
				   for (int bZ = 0; bZ < 16; bZ++) {
					   if (bY == 0) {
						   chunk.setChunkBlock((bY + (bZ * 128 + (bX * 128 * 16))),(byte)1,(byte)0,(byte)15,(byte)15);
					   } else if (bY < 64) {
						   chunk.setChunkBlock((bY + (bZ * 128 + (bX * 128 * 16))),(byte)3,(byte)0,(byte)15,(byte)15);
					   } else if (bY == 64) {
						   if ((x == -1 && z == 0) && false) {
							   if(bX == 0 && bZ == 15)
								   chunk.setChunkBlock((bY + (bZ * 128 + (bX * 128 * 16))),(byte)18,(byte)0,(byte)15,(byte)15);
							   else
								   chunk.setChunkBlock((bY + (bZ * 128 + (bX * 128 * 16))),(byte)12,(byte)0,(byte)15,(byte)15);
						   } else {
							   chunk.setChunkBlock((bY + (bZ * 128 + (bX * 128 * 16))),(byte)20,(byte)0,(byte)15,(byte)15);
						   }
					   } else {
						   chunk.setChunkBlock((bY + (bZ * 128 + (bX * 128 * 16))),(byte)0,(byte)0,(byte)15,(byte)15);
					   }
				   }
			   }
		   }
		   return chunk;
	   }
	
	
	public Chunk getChunkAt(int x, int z)
	{
		m_logger.debug("Fetching chunk for: x=" + x + " z=" + z);
		if (loadedChunks.containsKey(new Point(x,z)))
		{
			//m_logger.info("Returning loaded chunk");
			return loadedChunks.get(new Point(x,z));
		}
		else
		{
			Chunk returnChunk = null;
			if (AlphaWorldHandler.chunkExists(worldPath, new Point(x,z)))
			{
				try
				{
					m_logger.debug("Loading chunk");
					returnChunk = AlphaWorldHandler.loadChunk(worldPath, new Point(x,z));
					loadedChunks.put(new Point(x,z), returnChunk);
					return returnChunk;
				}
				catch (IOException ex)
				{
					m_logger.info("Failed to load chunk");
					return null;
				}
			}
			else
			{
				m_logger.info("Genderating Chunk");
				return null;
			}
		}
		
	}
	
	public void unloadChunk(Point p)
	{
		if (loadedChunks.containsKey(p))
		{
			Chunk chunk = loadedChunks.get(p);
			if (chunk.hasPlayers())
				return;
			loadedChunks.remove(p);
			chunk = null;
		}
	}
	public void unloadChunk(Chunk chunkToRemove) {
		Point p = new Point(chunkToRemove.getX(), chunkToRemove.getZ());
		if (loadedChunks.containsKey(p))
		{
			Chunk chunk = loadedChunks.get(p);
			if (chunk.hasPlayers())
				return;
			loadedChunks.remove(p);
			chunk = null;
		}
	}	
	/**
	 * Sets a block and updates the neighbours.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @param type The type id.
	 */
	public void setBlock(Position pos, int type) {
		Point chunkLocation = Chunk.getChunkPointFromBlockPoint(pos);
        Chunk chunk = getChunkAt(chunkLocation.getX(), chunkLocation.getY());
        if (chunk == null) return;
        Position blockPos = Chunk.getBlockPositionInChunk(pos);
        chunk.setChunkBlock(Chunk.getChunkKey(blockPos),(byte)type, (byte)0, (byte)0x0F, (byte)0x0F);
       
	}
	
	/**
	 * Gets a block.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 * @return The type id.
	 */
	public byte getBlock(Position pos) {
		Point chunkLocation = Chunk.getChunkPointFromBlockPoint(pos);
        Chunk chunk = getChunkAt(chunkLocation.getX(), chunkLocation.getY());
        Position blockPos = Chunk.getBlockPositionInChunk(pos);
		 int blockData = chunk.getChunkBlock(Chunk.getChunkKey(blockPos));
		 return (byte)((blockData >> 24));
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

	public long getCreationDate() {
		return m_created;
	}

	public void setCreationDate(long date) {
		m_created = date;
	}

	public AlphaEnvironment getEnvironment() {
		return m_env;
	}

	public void setEnvironment(AlphaEnvironment env) {
		m_env = env;
	}
	
	public String getWorldPath() {
		return worldPath;
	}

	public void setWorldPath(String value) {
		worldPath = value;
	}

	@Override
	public byte getBlockMeta(Position pos) {
		Point chunkLocation = Chunk.getChunkPointFromBlockPoint(pos);
        Chunk chunk = getChunkAt(chunkLocation.getX(), chunkLocation.getY());
        Position blockPos = Chunk.getBlockPositionInChunk(pos);
		 int blockData = chunk.getChunkBlock(Chunk.getChunkKey(blockPos));
		 return (byte)((blockData & 0x00ff0000)>>>16);
	}

}
