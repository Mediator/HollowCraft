package org.opencraft.server.cmd.impl;

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

import org.opencraft.server.cmd.Command;
import org.opencraft.server.cmd.CommandParameters;
import org.opencraft.server.model.Player;
import java.util.Random;

/**
 * A command to simulate dice rolls
 * @author Adam Liszka
 */

public class RollCommand extends Command {
	
	/**
	 * The instance of this command.
	 */
	private static final RollCommand INSTANCE = new RollCommand();

	public String name() {
		return "roll";
	}
	
	/**
	 * Gets the singleton instance of this command.
	 * @return The singleton instance of this command.
	 */
	public static RollCommand getCommand() {
		return INSTANCE;
	}
	
	/**
	 * Default private constructor.
	 */
	private RollCommand() {
		/* empty */
	}
	
	public void execute(Player player, CommandParameters params) {
		if (params.getArgumentCount() != 1) {
			player.getActionSender().sendChatMessage("<dice>d<sides>");
			player.getActionSender().sendChatMessage("Roll a dice with <sides> number of sides");
			player.getActionSender().sendChatMessage("and <dice> number of times");
			player.getActionSender().sendChatMessage("<dice> will be no greateer than 9.");
			player.getActionSender().sendChatMessage("<sides> will be no greater than 99.");
			player.getActionSender().sendChatMessage("Ex. 2d6 will roll a six-sided die two times");
			player.getActionSender().sendChatMessage("/roll <XdYY>");
			return;
		}

		String message = "";
		String arg[] = params.getStringArgument(0).split("d");
		if (arg.length != 2) {
			player.getActionSender().sendChatMessage("/roll <XdYY>");
			return;
		}
		try {
			int dice  = Math.abs(Integer.parseInt(arg[0]));
			dice = dice > 9 ? 9 : dice;
			int sides = Math.abs(Integer.parseInt(arg[1]));
			sides = sides > 99 ? 99 : sides;
			Random r = new Random();
			while (dice > 0) {
				dice -= 1;
				message += r.nextInt(sides) + ", ";
			}
			// clean up the extra ", " if it exists
			if (message.length() > 2) {
				message = message.substring(0, message.length() - 2);
			}
		} catch (Exception e) {
			player.getActionSender().sendChatMessage("Please input valid numbers");
		}
		player.getActionSender().sendChatMessage(message);
	}
	
}
