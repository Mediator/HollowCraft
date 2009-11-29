package org.opencraft.server.model.impl;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe.
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
import org.opencraft.server.model.BlockDefinition;
import org.opencraft.server.model.BlockManager;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.World;

/**
 * A block behaviour that handles lava.
 * @author Brett Russell
 *
 */

public class LavaBehaviour implements BlockBehaviour {
	@Override
	public void handlePassive(Level level, int x, int y, int z, int type) {
		
	}

	@Override
	public void handleDestroy(Level level, int x, int y, int z, int type) {
		
	}

	@Override
	public void handleScheduledBehaviour(Level level, int x, int y, int z, int type) {
		// represents the different directions lava can spread
		//							  x,  y, z
		int[][] spreadRules = { 	{ 0,  0,-1},
									{ 1,  0, 0},
									{-1,  0, 0},
									{ 0,  1, 0},
									{ 0, -1, 0}, };
		// then, spread outward
		for(int i = 0; i <= spreadRules.length - 1; i++) {
			byte thisOutwardBlock = level.getBlock(x+spreadRules[i][0], y+spreadRules[i][1], z+spreadRules[i][2]);	
			
			// check for lava
			if (thisOutwardBlock == BlockConstants.WATER || thisOutwardBlock == BlockConstants.STILL_WATER) { 
				level.setBlock(x+spreadRules[i][0], y+spreadRules[i][1], z+spreadRules[i][2], BlockConstants.STONE); 
			}
			else if (!BlockManager.getBlockManager().getBlock(thisOutwardBlock).isSolid() && !BlockManager.getBlockManager().getBlock(thisOutwardBlock).isLiquid()) {
				level.setBlock(x+spreadRules[i][0], y+spreadRules[i][1], z+spreadRules[i][2], BlockConstants.LAVA); 
			}
		}
		// set the block as inactive until a neighbor update reactivates it
		level.setBlock(x, y, z, BlockConstants.STILL_LAVA, false);
		
	}

	
}
