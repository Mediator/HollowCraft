package org.opencraft.server.model;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe and Brett Russell.
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

import java.util.LinkedList;
import java.util.List;

import org.opencraft.server.io.PersistenceManager;

/**
 * A class which manages <code>BlockDefinition</code>s and <code>BlockBehaviour</code>s.
 * @author Graham Edgecombe
 * @author Brett Russell
 *
 */
public final class BlockManager {
	
	/**
	 * The packet manager instance.
	 */
	private static final BlockManager INSTANCE = (BlockManager) PersistenceManager.getPersistenceManager().load("data/blocks.xml");
	
	/**
	 * Gets the packet manager instance.
	 * @return The packet manager instance.
	 */
	public static BlockManager getBlockManager() {
		return INSTANCE;
	}
	
	/**
	 * A list of the blocks.
	 */
	private List<BlockDefinition> blockList = new LinkedList<BlockDefinition>();
	
	/**
	 * The block array (faster access by opcode than list iteration).
	 */
	private transient BlockDefinition[] blocksArray;
	
	/**
	 * Default private constructor.
	 */
	private BlockManager() {
		/* empty */
	}
	
	/**
	 * Resolves the block manager after deserialization.
	 * @return The resolved object.
	 */
	private Object readResolve() {
		blocksArray = new BlockDefinition[256];
		for(BlockDefinition def : blockList) {
			blocksArray[def.getId()] = def;
		}
		return this;
	}

	/**
	 * Gets an incoming block definition.
	 * @param id The id.
	 * @return The block definition.
	 */
	public BlockDefinition getBlock(int id) {
		return blocksArray[id];
	}


}