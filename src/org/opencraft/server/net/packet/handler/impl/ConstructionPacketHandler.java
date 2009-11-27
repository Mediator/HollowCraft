package org.opencraft.server.net.packet.handler.impl;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe.
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

import org.opencraft.server.model.World;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.net.packet.handler.PacketHandler;

/**
 * A packet handler which handles the construction packet.
 * @author Graham Edgecombe
 *
 */
public class ConstructionPacketHandler implements PacketHandler {

	@Override
	public void handlePacket(MinecraftSession session, Packet packet) {
		if(!session.isAuthenticated()) {
			return;
		}
		int x = packet.getNumericField("x").intValue();
		int y = packet.getNumericField("y").intValue();
		int z = packet.getNumericField("z").intValue();
		int mode = packet.getNumericField("mode").intValue();
		int type = packet.getNumericField("type").intValue();
		System.out.println("[BLOCK][Type:" + type + "][Mode:" + mode + "]");
		switch(type)
		{
			case 46:
				World.getWorld().getLevel().preserveBlock(x,y,z);
				if(World.getWorld().getEntityControl().isEntityAt(session.getPlayer().getPosition().getX(),
						
						session.getPlayer().getPosition().getY(),session.getPlayer().getPosition().getZ(),25))
				{
					session.getActionSender().sendChatMessage("Response to entity contact.");
				}
			break;
			default:
				World.getWorld().getLevel().setBlock(x, y, z, (mode == 0) ? 0 : type);
				session.getPlayer().getSkill().addExperience(1, mode, type);
			break;
		}
	}

}
