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
import org.opencraft.server.model.Level;
import org.opencraft.server.model.Environment;
import org.opencraft.server.model.Builder;
import org.opencraft.server.Server;
import org.opencraft.server.security.Permission;
import org.opencraft.server.model.impl.builders.LandscapeBuilder;

/**
 * A command that generates a new world
 * @author Adam Liszka
 */

public class GenerateCommand extends Command {
	
	/**
	 * The instance of this command.
	 */
	private static final GenerateCommand INSTANCE = new GenerateCommand();
	
	/**
	 * Gets the singleton instance of this command.
	 * @return The singleton instance of this command.
	 */
	public static GenerateCommand getCommand() {
		return INSTANCE;
	}

	public String name() {
		return "generate";
	}
	
	/**
	 * Default private constructor.
	 */
	private GenerateCommand () {
		/* empty */
	}
	
	public void execute(Player player, CommandParameters params) {
		//if (player.isAuthorized(new Permission("org.opencraft.server.Worlds."+params.getStringArgument(0)+".goto"))) {
			if (params.getArgumentCount() != 6) {
				player.getActionSender().sendChatMessage("<name> - The name of the map");
				player.getActionSender().sendChatMessage("<x> <y> <z> - The width, height, and depth of the level");
				player.getActionSender().sendChatMessage("<type> - The type of level to generate");
				player.getActionSender().sendChatMessage("Valid types are 'Hilly'");
				player.getActionSender().sendChatMessage("<theme> - The tile set to use");
				player.getActionSender().sendChatMessage("Valid themes are 'Summer' 'Winter' 'Oasis'");
				player.getActionSender().sendChatMessage("/generate <name> <x> <y> <z> <type> <theme>");
				return;
			}


			try {
				String name = params.getStringArgument(0);
				int x = Integer.parseInt(params.getStringArgument(1));
				int y = Integer.parseInt(params.getStringArgument(2));
				int z = Integer.parseInt(params.getStringArgument(3));
				String type = params.getStringArgument(4);
				String theme = params.getStringArgument(5);

				Level newlvl = new Level();
				if (type.equalsIgnoreCase("Hilly")) {
					LandscapeBuilder b = new LandscapeBuilder(newlvl);
					if (theme.equalsIgnoreCase("Summer")) {
						b.setSummer();
					} else if (theme.equalsIgnoreCase("Winter")) {
						b.setWinter();
					} else if (theme.equalsIgnoreCase("Oasis")) {
						b.setOasis();
					} else {
						player.getActionSender().sendChatMessage("Valid themes are 'Summer' 'Winter' 'Oasis'");
						return;
					}
					newlvl.generateLevel(b, x, y, z, new Environment(), name, player.getName());
					Server.getServer().addLevel(newlvl);
				} else {
					player.getActionSender().sendChatMessage("Valid types are 'Hilly'");
					return;
				}
			} catch (Exception e) {
				player.getActionSender().sendChatMessage("/generate <name> <x> <y> <z> <type> <theme>");
			}
		//}
	}

}
