package org.hollowcraft.server.net;
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

import org.hollowcraft.io.PersistenceManager;
import org.hollowcraft.server.net.packet.PacketManager;
import org.hollowcraft.server.net.packet.handler.PacketHandlerManager;

import java.util.Map;
/**
 * Handle different protocol implemetnations
 * @author tdfischer
 * @author Caleb Champlin
 */
public class Protocol {
	public enum Version {
		Classic,
		Alpha
	}

	PacketHandlerManager m_handler;
	PacketManager m_packets;

	private Version m_version;

	public Protocol(Version v) {
		m_version = v;
	}

	public PacketHandlerManager handler() {
		if (m_handler == null)
			m_handler = handler(m_version);
		return m_handler;
	}

	public static PacketHandlerManager handler(Version v) {
		switch(v) {
			case Classic:
				return new PacketHandlerManager((Map<Integer,String>) PersistenceManager.getPersistenceManager().load("data/protocol/classic/packetHandlers.xml"));
			case Alpha:
				return new PacketHandlerManager((Map<Integer,String>) PersistenceManager.getPersistenceManager().load("data/protocol/alpha/packetHandlers.xml"));
		}
		return null;
	}

	public PacketManager packets() {
		if (m_packets == null)
			m_packets = packets(m_version);
		return m_packets; 
	}

	public static PacketManager packets(Version v) {
		switch(v) {
			case Classic:
				return (PacketManager) PersistenceManager.getPersistenceManager().load("data/protocol/classic/packets.xml");
			case Alpha:
				return (PacketManager) PersistenceManager.getPersistenceManager().load("data/protocol/alpha/packets.xml");
		}
		return null;
	}
}
