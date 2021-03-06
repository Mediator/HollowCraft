package org.hollowcraft.server.net.packet.handler;
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

import java.util.Map;

import org.hollowcraft.server.net.OCSession;
import org.hollowcraft.server.net.packet.Packet;
import org.slf4j.*;


/**
 * A class which manages <code>PacketHandler</code>s.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public class PacketHandlerManager<SessionType extends OCSession> {
	
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(PacketHandlerManager.class);
	
	
	/**
	 * An array of packet handlers.
	 */
	private PacketHandler[] handlers = new PacketHandler[256];
	
	/**
	 * Default private constructor.
	 */
	@SuppressWarnings("unchecked")
	public PacketHandlerManager(Map<Integer, String> map) {
		try {
			Map<Integer, String> handlers = map;
			for (Map.Entry<Integer, String> handler : handlers.entrySet()) {
				this.handlers[handler.getKey()] = (PacketHandler) Class.forName(handler.getValue()).newInstance();
			}
		} catch (Exception ex) {
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	/**
	 * Handles a packet.
	 * @param session The session.
	 * @param packet The packet.
	 */
	public void handlePacket(SessionType session, Packet packet) {
		PacketHandler handler = handlers[packet.getDefinition().getOpcode()];
		if (handler != null) {
			handler.handlePacket(session, packet);
		} else {
			logger.warn("Unhandled packet : " + packet + ".");
		}
	}
	
}
