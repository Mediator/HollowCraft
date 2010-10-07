package org.hollowcraft.server.task.impl;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */

import java.util.Iterator;
import java.util.Set;

import org.hollowcraft.model.Entity;
import org.hollowcraft.server.Server;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.task.ScheduledTask;
import org.slf4j.*;

/**
 * Sends a keep alive to clients every 1 second
 * @author Caleb Champlin
 */
public class KeepAliveTask extends ScheduledTask {
	
	/**
	 * The delay.
	 */
	private static final long DELAY = 1000;

	private static final Logger logger = LoggerFactory.getLogger(KeepAliveTask.class);
	
	/**
	 * Creates the keep alive with a delay of 1000ms.
	 */
	public KeepAliveTask() {
		super(DELAY);
	}

	public void execute() {
		World[] worlds = Server.getServer().getWorlds();
		for(World world : worlds) {
			world.getGameMode().tick();
			logger.trace("Player list length: {}", world.getPlayerList().getPlayers().size());
			for (Player player : world.getPlayerList().getPlayers()) {
				try
				{
				player.getActionSender().sendKeepAlive();
				}
				catch (Exception ex)
				{
					
				}
		}
	}
	}
	
}
