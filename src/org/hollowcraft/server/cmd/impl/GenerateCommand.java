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

import org.hollowcraft.model.ClassicEnvironment;
import org.hollowcraft.server.Server;
import org.hollowcraft.server.cmd.Command;
import org.hollowcraft.server.cmd.CommandParameters;
import org.hollowcraft.server.io.WorldManager;
import org.hollowcraft.server.io.impl.ClassicWorldManager;
import org.hollowcraft.server.model.Builder;
import org.hollowcraft.server.model.Player;
import org.hollowcraft.server.model.impl.builders.*;
import org.hollowcraft.server.model.impl.worlds.ClassicWorld;
import org.hollowcraft.server.security.Permission;

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
			String theme = "Summer";
			if (params.getArgumentCount() > 6 || params.getArgumentCount() < 5) {
				try
				{
				player.getActionSender().sendChatMessage("<name> - The name of the map");
				player.getActionSender().sendChatMessage("<x> <y> <z> - The width, height, and depth of the level");
				player.getActionSender().sendChatMessage("<type> - The type of level to generate");
				player.getActionSender().sendChatMessage("Valid types are 'Hills' 'Flat' 'Pixel'");
				player.getActionSender().sendChatMessage("<theme> - The tile set to use");
				player.getActionSender().sendChatMessage("Valid themes are 'Summer' 'Winter' 'Oasis'");
				player.getActionSender().sendChatMessage("/generate <name> <x> <y> <z> <type> [<theme>]");
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				return;
			}


			try {
				String name = params.getStringArgument(0);
				if (Server.getServer().hasWorld(name)) {
					player.getActionSender().sendChatMessage("World is loaded. Stopping");
					return;
				}
				int x = Integer.parseInt(params.getStringArgument(1));
				int y = Integer.parseInt(params.getStringArgument(2));
				int z = Integer.parseInt(params.getStringArgument(3));

				if (x < 16 || y < 16 || z < 16) {
					player.getActionSender().sendChatMessage("16x16x16 is the smallest level supported");
					return;
				}

				if (!(2*x == (x ^ x-1) + 1) && !(2*y == (y ^ y-1) + 1) && !(2*z == (z ^ z-1) + 1)) {
					player.getActionSender().sendChatMessage("sizes must be powers of 2");
					return;
				}
				String type = params.getStringArgument(4);

				ClassicWorld newlvl = new ClassicWorld();
				Builder b;
				if (type.equalsIgnoreCase("Hills")) {
					b = new ClassicLandscapeBuilder(newlvl);
				} else if (type.equalsIgnoreCase("Flat")) {
					b = new ClassicFlatGrassBuilder(newlvl);
				} else if (type.equalsIgnoreCase("Pixel")) {
					b = new ClassicPixelBuilder(newlvl);
				} else {
					player.getActionSender().sendChatMessage("Valid types are 'Hills' 'Flat' 'Pixel'");
					return;
				}

				if (params.getArgumentCount() == 6) {
					theme = params.getStringArgument(5);
				}

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

				newlvl.generateWorld(b, x, y, z, new ClassicEnvironment(), name, player.getName());
				WorldManager.getInstance().save(newlvl);
				player.getActionSender().sendChatMessage("World " + name + " created");
			} catch (Exception e) {
				try
				{
				player.getActionSender().sendChatMessage("/generate <name> <x> <y> <z> <type> [<theme>]");
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
	}

}
