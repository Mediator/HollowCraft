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
import org.hollowcraft.server.model.BlockBehaviour;
import org.hollowcraft.server.model.impl.worlds.ClassicWorld;

import java.util.Random;

/**
 * A block behaviour that handles plants which require dirt and sunlight to
 * survive.
 * @author Brett Russell
 * @author Caleb Champlin
 */
public class SurfacePlantBehaviour implements BlockBehaviour {
	
	public void handleDestroy(ClassicLevel level, Position pos, int type) {
		
	}
	
	public void handlePassive(ClassicLevel level, Position pos, int type) {
		((ClassicWorld)level).queueActiveBlockUpdate(pos);
	}
	
	public void handleScheduledBehaviour(ClassicLevel lvl, Position pos, int type) {
		short air = BlockManager.getBlockManager().getBlock("AIR").getId();
		short water = BlockManager.getBlockManager().getBlock("WATER").getId();
		short still_water = BlockManager.getBlockManager().getBlock("STILL_WATER").getId();
		ClassicWorld level = (ClassicWorld) lvl;
		level.queueActiveBlockUpdate(pos);
		Random r = new Random();
		if (BlockManager.getBlockManager().getBlock(level.getBlock(new Position(pos.getX(), pos.getY(), pos.getZ() - 1))).getId() != BlockManager.getBlockManager().getBlock("DIRT").getId() && 
		    BlockManager.getBlockManager().getBlock(level.getBlock(new Position(pos.getX(), pos.getY(), pos.getZ() - 1))).getId() != BlockManager.getBlockManager().getBlock("GRASS").getId() || 
		    level.getLightDepth(pos.getX(), pos.getY()) > pos.getZ() || 
		    BlockManager.getBlockManager().getBlock(level.getBlock(new Position(pos.getX(), pos.getY(), pos.getZ() + 1))).isLiquid()) {
			level.setBlock(pos, air);
		}
		int spongeRadius = 2;
		for (int spongeX = -1 * spongeRadius; spongeX <= spongeRadius; spongeX++) {
			for (int spongeY = -1 * spongeRadius; spongeY <= spongeRadius; spongeY++) {
				for (int spongeZ = -1 * spongeRadius; spongeZ <= spongeRadius; spongeZ++) {
					if (level.getBlock(new Position(pos.getX() + spongeX, pos.getY() + spongeY, pos.getZ() + spongeZ)) == water || 
						level.getBlock(new Position(pos.getX() + spongeX, pos.getY() + spongeY, pos.getZ() + spongeZ)) == still_water && r.nextBoolean() ) {
						level.setBlock(new Position(pos.getX() + spongeX, pos.getY() + spongeY, pos.getZ() + spongeZ), air);
						return;
					}
				}
			}
		}
	}
	
}
