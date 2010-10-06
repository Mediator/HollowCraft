package org.hollowcraft.server.model;

/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */


import java.util.*;
import java.io.IOException;

import org.hollowcraft.model.BlockDefinition;
import org.hollowcraft.model.BlockManager;
import org.hollowcraft.model.Entity;
import org.hollowcraft.model.Environment;
import org.hollowcraft.model.Level;
import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.hollowcraft.server.Configuration;
import org.hollowcraft.server.game.GameMode;
import org.hollowcraft.server.game.impl.SandboxGameMode;
import org.hollowcraft.server.io.WorldManager;
import org.hollowcraft.server.model.Builder;
import org.hollowcraft.server.model.OnBlockChangeHandler;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.model.impl.builders.*;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.security.Policy;
import org.hollowcraft.server.task.TaskQueue;
import org.hollowcraft.server.util.NPEList;
import org.hollowcraft.server.util.PlayerList;
import org.slf4j.*;

/**
 * Manages the in-game world.
 * @author Caleb Champlin
 */
public interface World extends Level {
	

	public Policy getPolicy();

	public void setPolicy(Policy p);
	
	public void finalize();
	
	/**
	 * Gets the current game mode.
	 * @return The current game mode.
	 */
	public GameMode<Player> getGameMode();

	/**
	 * Gets the player list.
	 * @return The player list.
	 */
	public PlayerList getPlayerList();
	public NPEList getEntityList();

	public void removePlayer(Player p);

	public void addPlayer(Player p);
	
	
	public void removeEntity(NPEntity ent);

	public void addEntity(NPEntity ent);
	
	/**
	 * Completes registration of a session.
	 * @param session The session.
	 */
	public void completeRegistration(MinecraftSession session);
	
	/**
	 * Broadcasts a chat message.
	 * @param player The source player.
	 * @param message The message.
	 */
	public void broadcast(Player player, String message);
	
	/**
	 * Broadcasts a server message.
	 * @param message The message.
	 */
	public void broadcast(String message);
	
}
