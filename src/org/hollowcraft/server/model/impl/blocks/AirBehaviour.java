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

/**
 * A block behaviour that handles water. Takes into account water's preference
 * for downward flow.
 * @author Brett Russell
 * @author Caleb champlin
 */
public class AirBehaviour implements BlockBehaviour {
	
	public void handlePassive(ClassicLevel level, Position pos, int type) {
		((ClassicWorld)level).queueActiveBlockUpdate(pos);
	}
	
	public void handleDestroy(ClassicLevel level, Position pos, int type) {
		
	}
	
	public void handleScheduledBehaviour(ClassicLevel lvl, Position pos, int type) {
		ClassicWorld level = (ClassicWorld)(lvl);
		short waterID = BlockManager.getBlockManager().getBlock("WATER").getId();
		if (level.getBlock(new Position(pos.getX()+1, pos.getY(), pos.getZ())) == waterID &&  level.getBlock(new Position(pos.getX()-1, pos.getY(), pos.getZ())) == waterID && level.getBlock(new Position(pos.getX(), pos.getY()+1, pos.getZ())) == waterID && level.getBlock(new Position(pos.getX(), pos.getY()-1, pos.getZ())) == waterID) {
			level.setBlock(pos, waterID);
		}
		if (pos.getZ() < level.getDepth()/2 && pos.getZ() > level.getDepth()/2-3) {
			if (pos.getX() == 0)
				level.setBlock(pos, waterID);
			if (pos.getX() == level.getWidth()-1)
				level.setBlock(pos, waterID);
			if (pos.getY() == 0)
				level.setBlock(pos, waterID);
			if (pos.getY() == level.getHeight()-1)
				level.setBlock(pos, waterID);
		}
		if (level.getBlock(new Position(pos.getX(), pos.getY(), pos.getZ()+1)) == waterID) {
			level.setBlock(pos, waterID);
			level.setBlock(new Position(pos.getX(), pos.getY(), pos.getZ()+1), BlockManager.getBlockManager().getBlock("AIR").getId());
		}
	}
}
