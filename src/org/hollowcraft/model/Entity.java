package org.hollowcraft.model;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The superclass for players and mobs.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public abstract class Entity {
	
	/**
	 * A collection of local entities.
	 */
	private final Set<Entity> localEntities = new HashSet<Entity>();
	
	private AbsolutePosition oldPosition;
	private AbsolutePosition position;
	private AbsoluteRotation oldRotation;
	private AbsoluteRotation rotation;
	private boolean onGround;
	private boolean oldOnGround;
	private int id = -1;
	private int oldId = -1;

	/**
	 * Default public constructor.
	 */
	public Entity() {
		position = new AbsolutePosition(0, 0, 0);
		rotation = new AbsoluteRotation(0, 0);
		onGround = true;
		resetOldPositionAndRotation();
	}
	
	/**
	 * Gets the local entity set.
	 * @return The local entity set.
	 */
	public Set<Entity> getLocalEntities() {
		return localEntities;
	}
		
	/**
	 * Gets the id.
	 * @return The id.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * @param id The id.
	 */
	public void setId(int id) {
		if (id == -1) {
			this.oldId = this.id;
		}
		this.id = id;
	}
	
	/**
	 * Gets the old id.
	 * @return The old id.
	 */
	public int getOldId() {
		return oldId;
	}
	
	/**
	 * Sets the rotation.
	 * @param rotation The rotation.
	 */
	public void setRotation(AbsoluteRotation rotation) {
		this.rotation = rotation;
	}
	
	/**
	 * Gets the rotation.
	 * @return The rotation.
	 */
	public AbsoluteRotation getRotation() {
		return rotation;
	}
	
	/**
	 * Sets the position.
	 * @param position The position.
	 */
	public void setPosition(AbsolutePosition position) {
		this.position = position;
	}
	
	/**
	 * Gets the position.
	 * @return The position.
	 */
	public AbsolutePosition getPosition() {
		return position;
	}
	
	/**
	 * Gets the old position.
	 * @return The old position.
	 */
	public AbsolutePosition getOldPosition() {
		return oldPosition;
	}
	
	/**
	 * Gets the old rotation.
	 * @return The old rotation.
	 */
	public AbsoluteRotation getOldRotation() {
		return oldRotation;
	}
	
	/**
	 * Resets the old position and rotation data.
	 */
	public void resetOldPositionAndRotation() {
		oldPosition = position;
		oldRotation = rotation;
		oldOnGround = onGround;
	}
	
	/**
	 * Sets whether or not an entity is on the ground
	 * @return On Ground
	 */
	public boolean getOnGround() {
		return onGround;
	}
	
	/**
	 * Sets whether or not an entity is on the ground
	 */
	public void setOnGround(boolean value) {
		onGround = value;
	}
	
	/**
	 * Sets whether or not an entity is on the ground
	 * @return On Ground
	 */
	public boolean getOldOnGround() {
		return oldOnGround;
	}
	
	/**
	 * Gets the name of this entity.
	 * @return The name of this entity.
	 */
	public abstract String getName();
	
}
