package org.opencraft.server.cmd.impl;

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

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import org.opencraft.server.model.World;
import org.opencraft.model.Environment;
import org.opencraft.server.model.Builder;
import org.opencraft.server.Server;
import org.opencraft.server.security.Permission;
import org.opencraft.server.model.impl.builders.*;

/**
 * A command that generates a new world
 * @author Adam Liszka
 */

public class WorldsCommand extends Command {
	
	/**
	 * The instance of this command.
	 */
	private static final WorldsCommand INSTANCE = new WorldsCommand();
	
	/**
	 * Gets the singleton instance of this command.
	 * @return The singleton instance of this command.
	 */
	public static WorldsCommand getCommand() {
		return INSTANCE;
	}

	public String name() {
		return "levels";
	}
	
	/**
	 * Default private constructor.
	 */
	private WorldsCommand () {
		/* empty */
	}
	
	public void execute(Player player, CommandParameters params) {
		String[] names = Server.getServer().getLoadedWorldNames();
		player.getActionSender().sendChatMessage("Loaded Worlds:");
		String message = "" + (char)(0xf);
		for (int i = 0; i < names.length; i++) {
			if (message.length() == 1) {
				message += "&a" + names[i];
			} else {
				// 6 is the magic number for "&e, &a"
				if (message.length() + 6 + names[i].length() < 64) {
					message += "&e, &a" + names[i];
				} else {
					player.getActionSender().sendChatMessage(message);
					message = " ";
					i -= 1;
				}
			}
		}

		if (message.length() > 0) {
			player.getActionSender().sendChatMessage(message);
		}

		player.getActionSender().sendChatMessage("Unloaded Worlds:");
		player.getActionSender().sendChatMessage("-Not implemented yet-");
	}

}
