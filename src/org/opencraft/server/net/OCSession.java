package org.opencraft.server.net;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, Søren Enevoldsen and Brett Russell. Mark Farrell
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
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.model.Player;
import java.net.SocketAddress;

/**
 * @author Mark Farrell
 * The base class for all sessions .
 */
public abstract class OCSession extends Connectable{
	

	
	/**
	 * The <code>IoSession</code> associated with this
	 * <code>MinecraftSession</code>.
	 */
	protected final IoSession session;

	/**
	 * Creates the Minecraft session.
	 * @param session The <code>IoSession</code>.
	 */
	public OCSession(IoSession session) {
		this.session = session;
	}
	
	public String toString() {
		if (m_player == null)
			return "<none>@"+session.getRemoteAddress();
		return m_player.getName()+"@"+session.getRemoteAddress();
	}

	public SocketAddress getAddress() {
		return session.getRemoteAddress();
	}

	
	/**
	 * Sets the state to authenticated.
	 */
	public void setAuthenticated() {
		this.state = State.AUTHENTICATED;
	}
	
	/**
	 * Sets the state to ready.
	 */
	public void setReady() {
		this.state = State.READY;
	}
	
	/**
	 * Sends a packet. This method may be called from multiple threads.
	 * @param packet The packet to send.
	 */
	public void send(Packet packet) {
			this.send(packet, session);
	}
	
	/**
	 * Closes this session.
	 */
	public void close() {
		session.close(false);
	}
	
	/**
	 * Called when this session is to be destroyed, should release any
	 * resources.
	 */
	public abstract void destroy();
	
	private Player m_player;

	public void setPlayer(Player p) {
		m_player = p;
	}
	
	public Player getPlayer() {
		return m_player;
	}

	
	/**
	 * Handles a packet.
	 * @param packet The packet to handle.
	 */
	public abstract void handle(Packet packet);
}
