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

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.hollowcraft.server.net.codec.MinecraftCodecFactory;
import org.hollowcraft.server.net.packet.Packet;
import org.hollowcraft.server.task.TaskQueue;
import org.hollowcraft.server.task.impl.SessionClosedTask;
import org.hollowcraft.server.task.impl.SessionMessageTask;
import org.hollowcraft.server.task.impl.SessionOpenedTask;
import org.slf4j.*;

/**
 * An implementation of an <code>IoHandler</code> which manages incoming events
 * from MINA and passes them onto the necessary subsystem in the OpenCraft
 * server.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public final class SessionHandler extends IoHandlerAdapter {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(SessionHandler.class);
	
	@Override
	public void exceptionCaught(IoSession session, Throwable throwable) throws Exception {
		logger.error("Exception occurred, closing session.", throwable);
		session.close(false);
	}
	
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		TaskQueue.getTaskQueue().push(new SessionMessageTask(session, (Packet) message));
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		logger.trace("Session closed. Queuing for cleanup.");
		TaskQueue.getTaskQueue().push(new SessionClosedTask(session));
	}
	
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		logger.trace("Session opened. Queuing for initialization.");
		session.getFilterChain().addFirst("protocol", new ProtocolCodecFilter(new MinecraftCodecFactory()));
		TaskQueue.getTaskQueue().push(new SessionOpenedTask(session));
	}
	
}
