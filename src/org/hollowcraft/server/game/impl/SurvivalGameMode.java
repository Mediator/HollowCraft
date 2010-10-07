package org.hollowcraft.server.game.impl;
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

import org.hollowcraft.model.Position;
import org.hollowcraft.server.game.GameModeAdapter;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.security.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the survival game mode.
 * @author Caleb Champlin
 */
public class SurvivalGameMode extends GameModeAdapter<Player> {
	private static final Logger logger = LoggerFactory.getLogger(SurvivalGameMode.class);
	
	public SurvivalGameMode()
	{
		super();	
	}

	public void tick() {
		
	}
	

	public void playerConnected(Player player) {
		super.playerConnected(player);
	}
	
	
	public void setBlock(Player player, World level, Position pos, int mode, int type) {
		if (mode == 0)
		{
			
		}
		level.setBlock(pos, (byte) (mode == 1 ? type : 0));	
	}
	
	// Default implementation
	public void playerDisconnected(Player player) {
		player.getWorld().broadcast(player.getName() + " disconnected.");
	}
}
