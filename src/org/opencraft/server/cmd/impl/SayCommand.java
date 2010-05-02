package org.opencraft.server.cmd.impl;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, S�ren Enevoldsen and Brett Russell.
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

/**
 * Official /say command
 * @author S�ren Enevoldsen
 */

public class SayCommand implements Command {
	
	/**
	 * The instance of this command.
	 */
	private static final SayCommand INSTANCE = new SayCommand();

	public String name() {
		return "say";
	}
	
	/**
	 * Gets the singleton instance of this command.
	 * @return The singleton instance of this command.
	 */
	public static SayCommand getCommand() {
		return INSTANCE;
	}
	
	/**
	 * Default private constructor.
	 */
	private SayCommand() {
		/* empty */
	}
	
	public void execute(Player player, CommandParameters params) {
		// Player using command is OP?
		if (player.getAttribute("IsOperator") != null && player.getAttribute("IsOperator").equals("true")) {
			if (params.getArgumentCount() == 0) {
				player.getActionSender().sendChatMessage("No message to send");
				player.getActionSender().sendChatMessage("/say <message>");
				return;
			}
			String message = "";
			for (int i = 0; i < params.getArgumentCount() - 1; i++)
				message += params.getStringArgument(i - 1) + " ";
			message += params.getStringArgument(params.getArgumentCount() - 1);
			player.getWorld().broadcast(message);
		} else
			player.getActionSender().sendChatMessage("You must be OP to do that");
	}
}
