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
package org.opencraft.server.net;

import java.util.ArrayDeque;
import java.util.Queue;

import org.apache.mina.core.session.IoSession;
import org.opencraft.server.net.packet.Packet;

import org.slf4j.*;

/**
 * @author Mark Farrell
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
			final boolean unqueuedPacket = name.equals("authentication_response") || name.endsWith("level_init") || name.equals("level_block") || name.equals("level_finish") || name.equals("disconnect");
			if (state == State.READY) {
				if (queuedPackets.size() > 0) {
					for (Packet queuedPacket : queuedPackets) {
						session.write(queuedPacket);
					}
					queuedPackets.clear();
				}
				session.write(packet);
			} else if (unqueuedPacket) {
				session.write(packet);
			} else {
				queuedPackets.add(packet);
			}
		}
	}
	
	
}
