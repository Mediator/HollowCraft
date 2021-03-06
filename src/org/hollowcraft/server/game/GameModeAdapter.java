package org.hollowcraft.server.game;
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

import java.util.HashMap;
import java.util.Map;

import org.hollowcraft.model.Position;
import org.hollowcraft.server.cmd.Command;
import org.hollowcraft.server.cmd.impl.*;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.security.Permission;
import org.slf4j.*;

/**
 * An implementation of a game mode that does the majority of the work for the
 * game mode developer.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public abstract class GameModeAdapter<P extends Player> implements GameMode<P> {
	
	/**
	 * The command map.
	 */
	private final Map<String, Command> commands = new HashMap<String, Command>();
	
	/**
	 * Creates the game mode adapter with default settings.
	 */
	public GameModeAdapter() {
		// these commands are standard to every game mode
		registerCommand(SayCommand.getCommand());
		registerCommand(KickCommand.getCommand());
		registerCommand(TeleportCommand.getCommand());
		registerCommand(SetspawnCommand.getCommand());
		registerCommand(SummonCommand.getCommand());
		registerCommand(SpawnCommand.getCommand());
		registerCommand(HelpCommand.getCommand());
		registerCommand(GotoCommand.getCommand());
		registerCommand(MeCommand.getCommand());
		registerCommand(RollCommand.getCommand());
		registerCommand(GenerateCommand.getCommand());
		registerCommand(WorldsCommand.getCommand());
		registerCommand(LoadCommand.getCommand());
		registerCommand(UnloadCommand.getCommand());
		registerCommand(PingCommand.getCommand());
	}

	private static final Logger logger = LoggerFactory.getLogger(GameModeAdapter.class);
	
	/**
	 * Adds a command
	 * @param name The command name.
	 * @param command The command.
	 */
	public void registerCommand(Command command, String name) {
		commands.put(name, command);
	}

	public void registerCommand(Command command) {
		commands.put(command.name(), command);
	}

	/**
	 * Lists all the commands for use by a command like /help
	 */
	//TODO: Alphabatize?
	public String listCommands() {
		String cmds = "";
		for(String key : commands.keySet()) {
			cmds += "/" + key + ", ";
		}
		// the last chars are ", " and are unneeded
		return cmds.substring(0, cmds.length()-2);
	}
	
	public Map<String, Command> getCommands() {
		return commands;
	}
	
	// Default implementation
	public void tick() {
		
	}
	
	// Default implementation
	public void playerConnected(Player player) {
		player.getWorld().broadcast(player.getName()+" joined");
	}
	
	// Default implementation
	public void setBlock(Player player, World level, Position pos, int mode, int type) {
		logger.debug("Setting block mode {} type {}", mode, type);
		if (mode == 1 && false && !player.isAuthorized(Permission.BUILD)) {
			logger.info("Not permitted to build.");
			try {
			//	player.getSession().getActionSender().sendBlock(pos, level.getBlock(pos),level.getBlockMeta(pos));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (mode == 0 && false && !player.isAuthorized(Permission.DESTROY)) {
			logger.info("Not permitted to destroy.");
			try {
			//	player.getSession().getActionSender().sendBlock(pos, level.getBlock(pos), level.getBlockMeta(pos));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			logger.debug("Building is OK!");
			level.setBlock(pos, (byte) (mode == 1 ? type : 0));
		}
	}
	
	// Default implementation
	public void playerDisconnected(Player player) {
		player.getWorld().broadcast(player.getName() + " disconnected.");
	}
	
	// Default implementation
	public void broadcastChatMessage(Player player, String message) {
		player.getWorld().broadcast(player, player.getName() + ": " + message);
	}
	
}
