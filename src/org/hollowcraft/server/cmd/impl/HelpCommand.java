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
 * Official /help command
 * @author Adam Liszka
 */

public class HelpCommand extends Command {
	
	/**
	 * The instance of this command.
	 */
	private static final HelpCommand INSTANCE = new HelpCommand();
	
	/**
	 * Gets the singleton instance of this command.
	 * @return The singleton instance of this command.
	 */
	public static HelpCommand getCommand() {
		return INSTANCE;
	}

	public String name() {
		return "help";
	}
	
	/**
	 * Default private constructor.
	 */
	private HelpCommand() {
		/* empty */
	}
	
	public void execute(Player player, CommandParameters params) {
		String message = player.getWorld().getGameMode().listCommands();
		while (message.length() > 0) {
			// this is a short list so send it and leave
			if (message.length() < 64) {
				player.getActionSender().sendChatMessage(message);
				return;
			}

			int end = 64;
			while (end > 0) {
				// Look for a space in which to nicely break up the list
				// does NOT cover commands that are greater than 64 characters in length
				if (message.charAt(end) == ' ') {
					// chop the string up and send the first chunk
					player.getActionSender().sendChatMessage(message.substring(0, end));
					message = message.substring(end+1, message.length());
					break;
				}
				end -= 1;
			}
		}
	}

}
