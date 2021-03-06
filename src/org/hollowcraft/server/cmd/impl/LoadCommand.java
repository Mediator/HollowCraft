package org.hollowcraft.server.cmd.impl;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2010 Trever Fischer <tdfischer@fedoraproject.org>
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

import org.hollowcraft.server.Server;
import org.hollowcraft.server.cmd.Command;
import org.hollowcraft.server.cmd.CommandParameters;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.security.Permission;

/**
 * A command to send a player to a new world
 * @author Trever Fischer
 */

public class LoadCommand extends Command {
	
	/**
	 * The instance of this command.
	 */
	private static final LoadCommand INSTANCE = new LoadCommand ();
	
	/**
	 * Gets the singleton instance of this command.
	 * @return The singleton instance of this command.
	 */
	public static LoadCommand getCommand() {
		return INSTANCE;
	}

	public String name() {
		return "load";
	}
	
	/**
	 * Default private constructor.
	 */
	private LoadCommand () {
		/* empty */
	}
	
	public void execute(Player player, CommandParameters params) {
		try
		{
			if (params.getArgumentCount() != 1) {
				player.getActionSender().sendChatMessage("Usage:");
				player.getActionSender().sendChatMessage("/load <name>");
				return;
			}
			if (!Server.getServer().hasWorld(params.getStringArgument(0))) {
				player.getActionSender().sendChatMessage("Loading "+params.getStringArgument(0));
				if (Server.getServer().loadWorld(params.getStringArgument(0))) {
					player.getActionSender().sendChatMessage(params.getStringArgument(0) + " loaded");
				} else {
					player.getActionSender().sendChatMessage("No such level: " + params.getStringArgument(0));
				}
			} else {
				player.getActionSender().sendChatMessage("World "+params.getStringArgument(0) + " is already loaded.");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
