package org.hollowcraft.server.model.impl.blocks;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
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

import org.hollowcraft.model.BlockManager;
import org.hollowcraft.model.ClassicLevel;
import org.hollowcraft.model.Position;
import org.hollowcraft.server.Configuration;
import org.hollowcraft.server.model.BlockBehaviour;
import org.hollowcraft.server.model.impl.worlds.ClassicWorld;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A block behaviour that handles water. Takes into account water's preference
 * for downward flow.
 * @author Brett Russell
 * @author Caleb Champlin
 */
public class WaterBehaviour implements BlockBehaviour {
	
	public void handlePassive(ClassicLevel level, Position pos, int type) {
		((ClassicWorld)level).queueActiveBlockUpdate(pos);
	}
	
	public void handleDestroy(ClassicLevel level, Position pos, int type) {
		
	}
	
	public void handleScheduledBehaviour(ClassicLevel level, Position pos, int type) {
		short sponge = BlockManager.getBlockManager().getBlock("sponge").getId();
		// represents different directions in the Cartesian plane, z axis is
		// ignored and handled specially
		Integer[][] spreadRules = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 } };
		ArrayList<Integer[]> shuffledRules = new ArrayList<Integer[]>();
		for(Integer[] rule : spreadRules)
			shuffledRules.add(rule);
		Collections.shuffle(shuffledRules);
		spreadRules = shuffledRules.toArray(spreadRules);
		
		int spongeRadius = Configuration.getConfiguration().getSpongeRadius();
		

		for (int spongeX = (-1 * spongeRadius); spongeX <= spongeRadius; spongeX++) {
			for (int spongeY = (-1 * spongeRadius); spongeY <= spongeRadius; spongeY++) {
				for (int spongeZ = (-1 * spongeRadius); spongeZ <= spongeRadius; spongeZ++) {
					if (((ClassicWorld)level).getBlock(new Position(pos.getX() + spongeX, pos.getY() + spongeY, pos.getZ() + spongeZ)) == sponge)
						return;
				}
			}
		}

		
		byte underBlock = level.getBlock(new Position(pos.getX(), pos.getY(), pos.getZ() - 1));
		short lava = BlockManager.getBlockManager().getBlock("LAVA").getId();
		short water = BlockManager.getBlockManager().getBlock("WATER").getId();
		short still_lava = BlockManager.getBlockManager().getBlock("STILL_LAVA").getId();
		short air = BlockManager.getBlockManager().getBlock("AIR").getId();
		short rock = BlockManager.getBlockManager().getBlock("ROCK").getId();
		// there is lava under me
		if (underBlock == lava|| underBlock == still_lava) {
			level.setBlock(pos, air);
			level.setBlock(new Position(pos.getX(), pos.getY(), pos.getZ() - 1), rock);
		// move me down
		} else if (!BlockManager.getBlockManager().getBlock(underBlock).isSolid() && !BlockManager.getBlockManager().getBlock(underBlock).isLiquid()) {
			level.setBlock(new Position(pos.getX(), pos.getY(), pos.getZ() - 1), water);
			level.setBlock(pos, air);
		// spread outward
		} else {
			OUTERMOST_OUTWARD: for (int i = 0; i <= spreadRules.length - 1; i++) {
				byte thisOutwardBlock = level.getBlock(new Position(pos.getX() + spreadRules[i][0], pos.getY() + spreadRules[i][1], pos.getZ() + spreadRules[i][2]));
				
				for (int spongeX = (-1 * spongeRadius); spongeX <= spongeRadius; spongeX++) {
					for (int spongeY = (-1 * spongeRadius); spongeY <= spongeRadius; spongeY++) {
						for (int spongeZ = (-1 * spongeRadius); spongeZ <= spongeRadius; spongeZ++) {
							if (level.getBlock(new Position(pos.getX() + spreadRules[i][0] + spongeX, pos.getY() + spreadRules[i][1] + spongeY, pos.getZ() + spreadRules[i][2] + spongeZ)) == sponge)
								break OUTERMOST_OUTWARD;
						}
					}
				}
				
				// check for lava
				if (thisOutwardBlock == lava || thisOutwardBlock == still_lava) {
					level.setBlock(pos, air);
					level.setBlock(new Position(pos.getX() + spreadRules[i][0], pos.getY() + spreadRules[i][1], pos.getZ() + spreadRules[i][2]), rock);
				} else if (level.getBlock(new Position(pos.getX() + spreadRules[i][0], pos.getY() + spreadRules[i][1], pos.getZ() + spreadRules[i][2] - 1)) == air &&
					   level.getBlock(new Position(pos.getX(), pos.getY(), pos.getZ() - 1)) == water) {
					break OUTERMOST_OUTWARD;
				} else if (!BlockManager.getBlockManager().getBlock(thisOutwardBlock).isSolid() && !BlockManager.getBlockManager().getBlock(thisOutwardBlock).isLiquid()) {
					//level.setBlock(x, y, z, BlockConstants.AIR);
					level.setBlock(new Position(pos.getX() + spreadRules[i][0], pos.getY() + spreadRules[i][1], pos.getZ() + spreadRules[i][2]), water);
				}
			}
		}
	}
}
