package org.hollowcraft.server.persistence;
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

import java.io.IOException;

import org.hollowcraft.server.model.Player;
import org.slf4j.*;


/**
 * Represents a single persistence request.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 *
 */
public abstract class PersistenceRequest implements Runnable {
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(PersistenceRequest.class);
	
	/**
	 * The player.
	 */
	private final Player player;
	
	/**
	 * Creates a persistence request for the specified player.
	 * @param player The player.
	 */
	public PersistenceRequest(Player player) {
		this.player = player;
	}
	
	/**
	 * Gets the player.
	 * @return The player.
	 */
	public final Player getPlayer() {
		return player;
	}
	
	/**
	 * Performs the persistence request.
	 */
	public abstract void perform() throws IOException;
	
	/**
	 * Calls the <code>perform</code> method to actually run the persistence
	 * request.
	 */

	public final void run() {
		try {
			perform();
			logger.info(getClass().getName() + " for : " + player.getName() + " succeeded.");
		} catch (IOException ex) {
			logger.error(getClass().getName() + " for : " + player.getName() + " failed.", ex);
			try
			{
			player.getSession().getActionSender().sendLoginFailure("Persistence request failed. Please try again.");
			}
			catch (Exception e)
			{
				logger.warn("Failed to send login failure");
			}
		}
	}
	
}
