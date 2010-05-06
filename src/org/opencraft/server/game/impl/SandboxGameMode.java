package org.opencraft.server.game.impl;

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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opencraft.server.game.GameModeAdapter;
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Player;

/**
 * A game mode in which building is free and anything goes.
 * @author Adam Liszka
 */

public class SandboxGameMode extends GameModeAdapter<Player> {
	
	/**
	 * A map of players who have connected.
	 */
	private Map<String, Date> visitors = new HashMap<String, Date>();
	
	/**
	 * Register extra commands here.
	 */
	public SandboxGameMode() {
	}
	
	public void playerConnected(Player player) {
		super(player);
		String name = player.getName();
		// New player?
		if (!visitors.containsKey(name)) {
			player.getSession().getActionSender().sendChatMessage("Welcome " + name + ".");
		} else {
			// Welcome back.
			String lastConnectDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(visitors.get(name));
			player.getSession().getActionSender().sendChatMessage("Welcome back " + name + ".");
			player.getSession().getActionSender().sendChatMessage("You last connect was: " + lastConnectDate + ".");
			
		}
		// Remember connection time
		visitors.put(name, new Date());
	}
	
	public void broadcastChatMessage(Player player, String message) { // TODO:
		// rank
		// colors?
		player.getWorld().broadcast(player, player.getName() + ": " + message);
	}
	
}
