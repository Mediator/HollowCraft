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

import java.util.LinkedList;
import java.util.List;

import org.hollowcraft.server.model.BlockBehaviour;

/**
 * Represents an individual block type.
 * @author Brett Russell
 * @author Caleb Champlin
 */
public class BlockDefinition {
	
	/**
	 * The block name.
	 */
	private String name;
	
	/**
	 * The block ID.
	 */
	private short bid;
	
	/**
	 * The block's solidity.
	 */
	private boolean solid;
	
	/**
	 * The block's fluidity.
	 */
	private boolean liquid;
	
	/**
	 * The block's transparency.
	 */
	private boolean blocksLight;
	
	/**
	 * The block's size.
	 */
	private boolean halfBlock;
	
	/**
	 * The block's "full" version if it is a halfblock.
	 */
	private short fullCounterpart;
	
	/**
	 * The block's behaviour, as a string.
	 */
	private String behaviourName;
	
	/**
	 * The block's behaviour.
	 */
	private transient BlockBehaviour behaviour;
	
	/**
	 * The block's periodic physics check state.
	 */
	private boolean doesThink;
	
	/**
	 * Whether this block is a plant (whether it cares about the blocks above
	 * it)
	 */
	private boolean isPlant;
	
	/**
	 * The timer, in milliseconds, on which this block thinks.
	 */
	private long thinkTimer;

	private boolean gravity;

	private int strength;
	
	
	private List<EntityDrop> drops;
	/**
	 * Constructor.
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private BlockDefinition(String name, short bid, boolean solid, boolean liquid, boolean blocksLight, boolean halfBlock, boolean doesThink, boolean isPlant, long thinkTimer, short fullCounterpart, String behaviourName, boolean gravity, int strength, List<EntityDrop> drops) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.name = name;
		this.bid = bid;
		this.solid = solid;
		this.liquid = liquid;
		this.blocksLight = blocksLight;
		this.halfBlock = halfBlock;
		this.doesThink = doesThink;
		this.isPlant = isPlant;
		this.thinkTimer = thinkTimer;
		this.fullCounterpart = fullCounterpart;
		this.drops = drops;
		this.behaviourName = behaviourName.trim();
		this.gravity = gravity;
		this.strength = strength;
		if (behaviourName.length() > 0) {
			this.behaviour = (BlockBehaviour) Class.forName(this.behaviourName).newInstance();
		}
	}
	
	/**
	 * Resolves this object.
	 * @return A resolved object.
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private Object readResolve() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return new BlockDefinition(name, bid, solid, liquid, blocksLight, halfBlock, doesThink, isPlant, thinkTimer, fullCounterpart, behaviourName, gravity, strength, drops);
	}
	
	/**
	 * Gets the name.
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the ID.
	 * @return The ID.
	 */
	public short getId() {
		return bid;
	}
	
	/**
	 * Gets the solidity.
	 * @return The solidity.
	 */
	public boolean isSolid() {
		return solid;
	}

	public boolean hasGravity() {
		return gravity;
	}

	public int getStrength() {
		return strength;
	}
	
	/**
	 * Gets the fluidity.
	 * @return The fluidity.
	 */
	public boolean isLiquid() {
		return liquid;
	}
	
	/**
	 * Gets the transparency.
	 * @return The transparency.
	 */
	public boolean doesBlockLight() {
		return blocksLight;
	}
	
	/**
	 * Gets the size.
	 * @return The size.
	 */
	public boolean isHalfBlock() {
		return halfBlock;
	}
	
	/**
	 * Gets the periodic physics check state.
	 * @return The... yeah.
	 */
	public boolean doesThink() {
		return doesThink;
	}
	
	/**
	 * Apply passive physics.
	 * @param level The level.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 */
	public void behavePassive(ClassicLevel level, Position pos) {
		if (behaviour == null) {
			return;
		}
		this.behaviour.handlePassive(level, pos, this.bid);
	}
	
	/**
	 * Apply physics on block destruction.
	 * @param level The level.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 */
	public void behaveDestruct(ClassicLevel level, Position pos) {
		if (behaviour == null) {
			return;
		}
		this.behaviour.handleDestroy(level, pos, this.bid);
	}
	
	/**
	 * Apply periodic physics.
	 * @param level The level.
	 * @param x The x coordinate.
	 * @param y The y coordinate.
	 * @param z The z coordinate.
	 */
	public void behaveSchedule(ClassicLevel level, Position pos) {
		if (behaviour == null) {
			return;
		}
		this.behaviour.handleScheduledBehaviour(level, pos, this.bid);
	}
	
	/**
	 * Gets the speed at which this block "thinks."
	 * @return The think speed.
	 */
	public long getTimer() {
		return thinkTimer;
	}
	
	/**
	 * Gets this blocks' "plant" state.
	 * @return Whether or not this block is a plant.
	 */
	public boolean isPlant() {
		return isPlant;
	}
	
	/**
	 * Gets the fullsize counterpart for this block.
	 * @return The fullsize counterpart ID.
	 */
	public short getFullCounterpart() {
		if (halfBlock) {
			return fullCounterpart;
		} else {
			return 0;
		}
	}
}
