package org.hollowcraft.server.cmd.impl;

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

import org.hollowcraft.server.cmd.Command;
import org.hollowcraft.server.cmd.CommandParameters;
import org.hollowcraft.server.model.Player;

/**
 * Official /summon command
 * @author Adam Liszka
 */

public class SummonCommand extends Command {
	
	/**
	 * The instance of this command.
	 */
	private static final SummonCommand INSTANCE = new SummonCommand();
	
	/**
	 * Gets the singleton instance of this command.
	 * @return The singleton instance of this command.
	 */
	public static SummonCommand getCommand() {
		return INSTANCE;
	}

	public String name() {
		return "summon";
	}
	
	/**
	 * Default private constructor.
	 */
	private SummonCommand() {
		/* empty */
	}
	
	public void execute(Player player, CommandParameters params) {
		// Player using command is OP?
		if (params.getArgumentCount() == 1) {
			for (Player other : player.getWorld().getPlayerList().getPlayers()) {
				if (other.getName().toLowerCase().equals(params.getStringArgument(0).toLowerCase())) {
					//TODO: Make the player face each other?
					other.teleport(player.getPosition(), player.getRotation());
					return;
				}
			}
			// Player not found
			player.getActionSender().sendChatMessage(params.getStringArgument(0) + " was not found");
			return;
		} else {
			player.getActionSender().sendChatMessage("Wrong number of arguments");
		}
		player.getActionSender().sendChatMessage("/summon <name>");
	}
}
