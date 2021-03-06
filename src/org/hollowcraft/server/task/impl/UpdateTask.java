package org.hollowcraft.server.task.impl;
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

import java.util.Iterator;
import java.util.Set;

import org.hollowcraft.model.Animation;
import org.hollowcraft.model.Entity;
import org.hollowcraft.server.Server;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.task.ScheduledTask;
import org.slf4j.*;

/**
 * Updates the players and game world.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public class UpdateTask extends ScheduledTask {
	
	/**
	 * The delay.
	 */
	private static final long DELAY = 100;

	private static final Logger logger = LoggerFactory.getLogger(UpdateTask.class);
	
	/**
	 * Creates the update task with a delay of 100ms.
	 */
	public UpdateTask() {
		super(DELAY);
	}

	public void execute() {
		World[] worlds = Server.getServer().getWorlds();
		for(World world : worlds) {
			world.getGameMode().tick();
			logger.trace("Player list length: {}", world.getPlayerList().getPlayers().size());
			for (Player player : world.getPlayerList().getPlayers()) {
				//player.getActionSender().sendKeepAlive();
				Set<Entity> localEntities = player.getLocalEntities();
				Iterator<Entity> localEntitiesIterator = localEntities.iterator();
				while (localEntitiesIterator.hasNext()) {
					Entity localEntity = localEntitiesIterator.next();
					if (localEntity.getId() == -1) {
						localEntitiesIterator.remove();
						logger.trace("Sending removeEntity to other player");
						try
						{
						player.getSession().getActionSender().sendRemoveEntity(localEntity);
						}
						catch (Exception ex)
						{
							logger.warn("Failed to send remove entity");
						}
					} else {
						logger.trace("Sending updateEntity to other player");
						try
						{
						player.getSession().getActionSender().sendUpdateEntity(localEntity);
						}
						catch (Exception ex)
						{
							logger.warn("Failed to send update entity");
						}
					}
				}
				for (Player otherEntity : world.getPlayerList().getPlayers()) {
					if (!localEntities.contains(otherEntity) && otherEntity != player) {
						localEntities.add(otherEntity);
						logger.trace("Sending addEntity to other player");
						try
						{
						player.getSession().getActionSender().sendAddEntity(otherEntity);
						}
						catch (Exception ex)
						{
							logger.warn("Failed to send add entity");
						}
						
					}
				}
			}
			for (Player player : world.getPlayerList().getPlayers()) {
				player.resetOldPositionAndRotation();
				for (Animation animation : player.getAnimations())
					animation.resetPosition();
			}
			// We need to rethink how block behavior is going to work in alpha
			// maybe something similar to the current system would work idk
			//TODO FIX THIS
			//world.applyBlockBehaviour();
		}
	}
	
}
