package org.opencraft.server.extensions.brushes;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 S�ren Enevoldsen and Graham Edgecombe.
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
 * 
 * @author S�ren Enevoldsen
 * @author Graham Edgecombe
 *
 */

public final class BrushCommand implements Command {
	
	private static final BrushCommand INSTANCE = new BrushCommand(); 
	
	public static BrushCommand getBrushCommand() {
		return INSTANCE;
	}
	
	private void usage(Player player) {
		player.getActionSender().sendChatMessage("/brush radius [radius]");
		player.getActionSender().sendChatMessage("/brush [standard|default");
		player.getActionSender().sendChatMessage("/brush delete [1|0]");
		player.getActionSender().sendChatMessage("/brush type [square|diamond|line|flat]");
	}
	
	public void execute(Player player, CommandParameters parameters) {

		String action = parameters.getStringArgument(0);
		
		if (parameters.getArgumentCount() == 1) {
			if (action.equals("default") ||
					action.equals("standard")) {
				player.setAttribute("brush", SquareBrush.DEFAULT_BRUSH);
				player.getActionSender().sendChatMessage("Now using standard brush");
			}
			else
				usage(player);
		}
		else if (parameters.getArgumentCount() == 2) {
			
			if (action.equals("radius")) {
				try {
					int newRadius = parameters.getIntegerArgument(1);
					((Brush)player.getAttribute("brush")).setRadius(newRadius);
					player.getActionSender().sendChatMessage("Brush radius changed");
				} catch (Exception e) {
					player.getActionSender().sendChatMessage("/brush radius [radius]");
				}
				
				
			}
			else if (action.equals("delete")) {
				String onOff = parameters.getStringArgument(1);
				if (onOff.equals("1")) {
					((Brush)player.getAttribute("brush")).useForDelete(true);
					player.getActionSender().sendChatMessage("Using this brush to delete");
				}
				else if (onOff.equals("0")) {
					((Brush)player.getAttribute("brush")).useForDelete(false);
					player.getActionSender().sendChatMessage("Using standard brush to delete");
				}
				else
					player.getActionSender().sendChatMessage("/brush delete [1|0]");
			}
			else if (action.equals("type")) {
				String brush = parameters.getStringArgument(1);
				int bRadius = ((Brush)player.getAttribute("brush")).getRadius();
				Brush newBrush;
				if (brush.equals("square"))
					newBrush = new SquareBrush();
				else if (brush.equals("diamond"))
					newBrush = new DiamondBrush();
				else if (brush.equals("line"))
					newBrush = new LineBrush();
				else if (brush.equals("flat"))
					newBrush = new FlatBrush();
				else {
					player.getActionSender().sendChatMessage("/brush type [square|diamond|line|flat]");
					return;
				}
				newBrush.setRadius(bRadius);
				player.setAttribute("brush", newBrush);
				player.getActionSender().sendChatMessage("Brush type changed to " + brush);
			}
			else
				usage(player);
		}
		else
			usage(player);
	}
}
