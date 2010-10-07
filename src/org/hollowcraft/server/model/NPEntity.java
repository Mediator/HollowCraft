package org.hollowcraft.server.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
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

import java.util.HashMap;
import java.util.Map;


import java.util.ArrayList;

import org.hollowcraft.model.AbsolutePosition;
import org.hollowcraft.model.AbsoluteRotation;
import org.hollowcraft.model.Animation;
import org.hollowcraft.model.Entity;
import org.hollowcraft.server.io.WorldManager;
import org.hollowcraft.server.model.World;
import org.hollowcraft.server.net.MinecraftSession;
import org.hollowcraft.server.net.actions.ActionSender;
import org.hollowcraft.server.security.Permission;
import org.hollowcraft.server.security.Policy;
import org.hollowcraft.server.security.Principal;
import org.slf4j.*;

/**
 * Represents a connected player.
 * @author Graham Edgecombe
 */
public class NPEntity extends Entity {
	
	
	private static final Logger logger = LoggerFactory.getLogger(NPEntity.class);
	
	public NPEntity() {

	}

	public String toString() {
		return getName();
	}
	
	
	public String getName() {
		return name();
	}

	public String name() {
		return "Entity" + super.getId();
	}

	
	private World m_world;

	public World getWorld() {
		return m_world;
	}
	
	
	public void setWorld(World world) {
		m_world = world;
	}

	public void moveToWorld(World world) {
		if (m_world != null)
			m_world.removeEntity(this);
		assert(world != null);
		setWorld(world);
		m_world.addEntity(this);
		
	}
}
