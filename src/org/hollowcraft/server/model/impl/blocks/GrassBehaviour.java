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

import org.hollowcraft.model.BlockManager;
import org.hollowcraft.model.ClassicLevel;
import org.hollowcraft.model.Position;
import org.hollowcraft.server.model.BlockBehaviour;
import org.hollowcraft.server.model.impl.worlds.ClassicWorld;

/**
 * Handles the spreading of grass blocks to adjacent, sun-exposed dirt blocks.
 * @author Brett Russell
 * @author Caleb Champlin
 */
public class GrassBehaviour implements BlockBehaviour {

	public void handleDestroy(ClassicLevel level, Position pos, int type) {
		
	}
	
	public void handlePassive(ClassicLevel level, Position pos, int type) {
		((ClassicWorld)level).queueActiveBlockUpdate(pos);
	}
	
	public void handleScheduledBehaviour(ClassicLevel lvl, Position pos, int type) {
		// do we need to die?
		ClassicWorld level = (ClassicWorld)lvl;
		short dirt = BlockManager.getBlockManager().getBlock("DIRT").getId();
		short grass = BlockManager.getBlockManager().getBlock("GRASS").getId();
		byte aboveBlock = level.getBlock(new Position(pos.getX(), pos.getY(), pos.getZ() + 1));
		// Should the block above us kill us?
		if (BlockManager.getBlockManager().getBlock(aboveBlock).doesBlockLight() || BlockManager.getBlockManager().getBlock(aboveBlock).isLiquid()) {
			level.setBlock(pos, dirt);
			return;
		}

		//spread
		for (int h = (pos.getX() == 0 ? 0 : -1); h <= (pos.getX() == level.getWidth() - 1 ? 0 : 1); h++) {
			for (int i = (pos.getY() == 0 ? 0 : -1); i <= (pos.getY() == level.getHeight() - 1 ? 0 : 1); i++) {
				for (int j = (pos.getZ() == 0 ? 0 : -1); j <= (pos.getZ() == level.getDepth() - 1 ? 0 : 1); j++) {
					if (level.getBlock(new Position(pos.getX() + h, pos.getY() + i, pos.getZ() + j)) == dirt) {
						if (pos.getZ() + j < level.getDepth()) {
							// Is the block above it liquid?
							if (BlockManager.getBlockManager().getBlock(level.getBlock(new Position(pos.getX() + h, pos.getY() + i, pos.getZ() + j + 1))).isLiquid()) {
								break;
							}

							// Is the block smothered by something?
							if (BlockManager.getBlockManager().getBlock(level.getBlock(new Position(pos.getX() + h, pos.getY() + i, pos.getZ() + j + 1))).doesBlockLight()) {
								break;
							}
						}
			
						// Grow the grass
						level.setBlock(new Position(pos.getX() + h, pos.getY() + i, pos.getZ() + j), grass);
					}
				}
			}
		}
	}
}
