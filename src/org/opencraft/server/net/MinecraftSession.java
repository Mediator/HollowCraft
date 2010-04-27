package org.opencraft.server.net;

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

import org.apache.mina.core.session.IoSession;
import org.opencraft.server.model.Player;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.Server;
import org.slf4j.*;

/**
 * Manages a connected Minecraft session.
 * @author Graham Edgecombe
 */
public final class MinecraftSession extends OCSession{
	
	
	/**
	 * The action sender associated with this session.
	 */
	private final ActionSender actionSender = new ActionSender(this);
	
	private final static Logger logger = LoggerFactory.getLogger(MinecraftSession.class);
	
	/**
	 * The player associated with this session.
	 */
	private Player player;
	
	
	public MinecraftSession(IoSession sess) {
		super(sess);
	}
	/**
	 * Gets the action sender associated with this session.
	 * @return The action sender.
	 */
	public ActionSender getActionSender() {
		return actionSender;
	}
	
	/**
	 * Sets the player associated with this session.
	 * @param player The player.
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	/**
	 * Gets the player associated with this session.
	 * @return The player.
	 */
	public Player getPlayer() {
		return player;
	}

	
	/**
	 * Handles a packet.
	 * @param packet The packet to handle.
	 */
	@Override
	public void handle(Packet packet) {
		PersistingHandlerManager.getPacketHandlerManager().handlePacket(this, packet);
	}

	
	/**
	 * Called when this session is to be destroyed, should release any
	 * resources.
	 */
	@Override
	public void destroy() {
		logger.debug("Destroying session.");
		Server.getServer().unregister(this);
	}
	
	
	public boolean isAuthenticated() {
		if(player == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
