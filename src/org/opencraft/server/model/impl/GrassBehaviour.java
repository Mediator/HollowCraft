package org.opencraft.server.model.impl;

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

import org.opencraft.server.model.BlockBehaviour;
import org.opencraft.server.model.BlockConstants;
import org.opencraft.server.model.BlockManager;
import org.opencraft.server.model.Level;

/**
 * Handles the spreading of grass blocks to adjacent, sun-exposed dirt blocks.
 * @author Brett Russell
 */
public class GrassBehaviour implements BlockBehaviour {

	// Represents the different directions grass can spread.
	// Grass will one block in all directions around the grass
	// block except directly above and below the block.
	// x, y, z
	public static final int[][] spreadRules = { { 1,  0, 0 }, { -1,  0, 0 }, {  0,   0, 1 }, { 0,  0, -1 },
						    { 1,  0, 1 }, { -1,  0, 1 }, { -1,   0, 1 }, { 1,  0, -1 },
						    { 1,  1, 0 }, { -1,  1, 0 }, {  0,   1, 1 }, { 0,  1, -1 },
						    { 1,  1, 1 }, { -1,  1, 1 }, { -1,   1, 1 }, { 1,  1, -1 },
						    { 1, -1, 0 }, { -1, -1, 1 }, {  0,  -1, 1 }, { 0, -1, -1 },
						    { 1, -1, 1 }, { -1, -1, 1 }, { -1,  -1, 1 }, { 1, -1, -1 },
						  };

	public static final int[][] lifeLights = { { 1, 1,  0 }, { -1, 1,  0 }, {  0,  1,  1 }, { 0, 1,  -1 },
						   { 1, 1,  1 }, { -1, 1,  1 }, { -1,  1,  1 }, { 1, 1,  -1 },
						 };
	
	@Override
	public void handleDestroy(Level level, int x, int y, int z, int type) {
		
	}
	
	@Override
	public void handlePassive(Level level, int x, int y, int z, int type) {
		level.queueActiveBlockUpdate(x, y, z);
	}
	
	@Override
	public void handleScheduledBehaviour(Level level, int x, int y, int z, int type) {
		// do we need to die?

		byte aboveBlock = level.getBlock(x, y, z + 1);
		// Should the block above us kill us?
		if (BlockManager.getBlockManager().getBlock(aboveBlock).doesBlockLight() || BlockManager.getBlockManager().getBlock(aboveBlock).isLiquid()) {
			level.setBlock(x, y, z, BlockConstants.DIRT);
			return;
		}

		boolean lightFound = false;

		SEARCH_LIGHT: for (int i = 0; i <= spreadRules.length - 1; i++) {
			if (level.getLightDepth(x + spreadRules[i][0], y + spreadRules[i][1]) < z + spreadRules[i][2]) {
				//we found light
				lightFound = true;
				break;
			}
		}
		
		if (!lightFound) {
			 level.setBlock(x, y, z, BlockConstants.DIRT);
			 return;
		}

		// spread
		SEARCH_GRASS: for (int i = 0; i <= spreadRules.length - 1; i++) {
			if (level.getLightDepth(x, y) < z) {
				if (level.getBlock(x + spreadRules[i][0], y + spreadRules[i][1], z + spreadRules[i][2]) == BlockConstants.DIRT) {
					// Is the block above it liquid?
					if (BlockManager.getBlockManager().getBlock(level.getBlock(x + spreadRules[i][0], y + spreadRules[i][1], z + spreadRules[i][2] + 1)).isLiquid()) {
						break SEARCH_GRASS;
					}

					// Is the block smothered by something?
					if (BlockManager.getBlockManager().getBlock(level.getBlock(x + spreadRules[i][0], y + spreadRules[i][1], z + spreadRules[i][2] + 1)).doesBlockLight()) {
						break SEARCH_GRASS;
					}
					
					// Grow the grass
					level.setBlock(x + spreadRules[i][0], y + spreadRules[i][1], z + spreadRules[i][2], BlockConstants.GRASS);
				}
			}
		}
	}
}
