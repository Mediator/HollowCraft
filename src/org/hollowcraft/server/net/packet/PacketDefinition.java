package org.hollowcraft.server.net.packet;
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

/**
 * Represents a type of packet.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public final class PacketDefinition {
	
	/**
	 * The opcode of this packet.
	 */
	private final int opcode;
	
	/**
	 * Whether or not this packet should be queued
	 */
	private final boolean shouldqueue;
	
	/**
	 * The name of this packet.
	 */
	private final String name;
	
	/**
	 * The fields in this packet.
	 */
	private final PacketField[] fields;
	
	/**
	 * The length of this packet.
	 */
	private final transient int length;
	
	/**
	 * Creates the packet definition.
	 * @param opcode The opcode.
	 * @param name The name.
	 * @param fields The fields.
	 */
	public PacketDefinition(int opcode, String name, boolean shouldqueue, PacketField[] fields) {
		this.opcode = opcode;
		this.name = name;
		this.fields = fields;
		this.shouldqueue = shouldqueue;
		// compute packet length
		int length = 0;
		for (PacketField field : fields) {
			length += field.getType().getLength();
		}
		this.length = length;
	}
	
	/**
	 * Resolves this object.
	 * @return The object with the correct packet length.
	 */
	private Object readResolve() {
		// ensures length is computed
		return new PacketDefinition(opcode, name, shouldqueue, fields);
	}
	
	/**
	 * Gets the opcode of this packet.
	 * @return The opcode of this packet.
	 */
	public int getOpcode() {
		return opcode;
	}
	
	/**
	 * Gets the name of this packet.
	 * @return The name of this packet.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets whether or not the packet should queue
	 * @return Whether or not to queue the packet.
	 */
	public boolean getShouldQueue()
	{
		return shouldqueue;
	}
	/**
	 * Gets the fields in this packet.
	 * @return The fields in this packet.
	 */
	public PacketField[] getFields() {
		return fields;
	}
	
	/**
	 * Gets the length of this packet.
	 * @return The length of this packet, in bytes.
	 */
	public int getLength() {
		return length;
	}
	
}
