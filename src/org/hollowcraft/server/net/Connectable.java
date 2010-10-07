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
 * Copyright (c) 2009 Graham Edgecombe, Søren Enevoldsen, Mark Farrell and Brett Russell.
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

import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.mina.core.session.IoSession;
import org.hollowcraft.server.net.packet.Packet;

import org.slf4j.*;

/**
 * @author Mark Farrell
 * @author Caleb Champlin
 * Represents an object that is bound to either end of a socket.
 */
public abstract class Connectable {
	
	/**
	 * Packet queue.
	 */
	protected final Queue<Packet> queuedPackets = new ArrayDeque<Packet>();

	private static final Logger logger = LoggerFactory.getLogger(Connectable.class);
	
	/**
	 * State.
	 */
	protected State state = State.CONNECTED;
	
	/**
	 * Sends a packet. This method may be called from multiple threads.
	 * @param packet The packet to send.
	 */
	protected void send(Packet packet, IoSession session) {
		synchronized (this) {
			final String name = packet.getDefinition().getName();
			logger.debug("Should queue packet: name=" + name + " state=" + state + " queue=" + packet.getDefinition().getShouldQueue());
			if (state == State.READY) {
				try
				{
				if (queuedPackets.size() > 0) {
					for (Packet queuedPacket : queuedPackets) {
						logger.debug("Sending queued packet " + queuedPacket.getDefinition().getName());
						session.write(queuedPacket);
					}
					queuedPackets.clear();
				}
				logger.debug("Sending normal packet " + packet.getDefinition().getName());
				session.write(packet);
				}
				catch (Exception ex)
				{
					logger.info("Exception sending packet");
				}
			} else if (!packet.getDefinition().getShouldQueue()) {
				session.write(packet);
			} else {
				logger.debug("Adding packet to queue " + packet.getDefinition().getName());
				queuedPackets.add(packet);
			}
		}
	}
	
	
}
