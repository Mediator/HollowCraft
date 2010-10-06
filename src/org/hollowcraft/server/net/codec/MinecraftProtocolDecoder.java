package org.hollowcraft.server.net.codec;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.hollowcraft.server.net.Protocol;
import org.hollowcraft.server.net.packet.DataType;
import org.hollowcraft.server.net.packet.Packet;
import org.hollowcraft.server.net.packet.PacketDefinition;
import org.hollowcraft.server.net.packet.PacketField;
import org.hollowcraft.server.net.packet.PacketManager;
import org.hollowcraft.server.net.packet.handler.PacketHandlerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implement of a <code>ProtocolDecoder</code> which decodes buffers into
 * Minecraft packet objects then dispatches them.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 * @changes 10/03/10 Caleb Champlin - Full support for alpha protocol and packet decoding
 */
public final class MinecraftProtocolDecoder extends CumulativeProtocolDecoder {
	
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(MinecraftProtocolDecoder.class);
	
	/**
	 * The current packet being decoded.
	 */
	private PacketDefinition currentPacket = null;
	private String curHex = "";
	public MinecraftProtocolDecoder()
	{
	}
	
	@Override
	protected boolean doDecode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput out) throws Exception {
		if (!session.containsAttribute("protocol")) {
			int opcode = buffer.getUnsigned();
			Protocol protocol;
			if (opcode == 0) {
				protocol = new Protocol(Protocol.Version.Classic);
				System.out.println("Protocol Set To Classic");
			} else {
				protocol = new Protocol(Protocol.Version.Alpha);
				System.out.println("Protocol Set To Alpha");
			}
			session.setAttribute("protocol", protocol);
			buffer.position(0);
		}
		Protocol p = (Protocol) session.getAttribute("protocol");
		int opcode = -1;
		curHex += buffer.getHexDump() + " | ";
		if (currentPacket == null) {
			if (buffer.remaining() >= 1) {
				opcode = buffer.getUnsigned();
				currentPacket = p.packets().getIncomingPacket(opcode);
				if (currentPacket == null) {
					throw new IOException("Unknown incoming packet type (opcode = " + opcode + ").");
				}
			} else {
				return false;
			}
		}
		//logger.info("Hexdump of packet: (" + ((currentPacket == null) ? "unknown" : currentPacket.getOpcode()) + " ) [ " + curHex + " ]");
		int bufferPos = buffer.position();
		Map<String, Object> values = new HashMap<String, Object>();
		for (PacketField field : currentPacket.getFields()) {
			if (field.getType().getLength() > 0 && buffer.remaining() < field.getType().getLength())
			{
				curHex = "";
				buffer.position(bufferPos);
				return false;
			}
			Object value = null;
			int fieldlength;
			switch (field.getType()) {
			case BYTE:
				value = buffer.get();
				break;
			case SHORT:
				value = buffer.getShort();
				break;
			case INT:
				value = buffer.getInt();
				break;
			case LONG:
				value = buffer.getLong();
				break;
			case FLOAT:
				value = buffer.getFloat();
				break;
			case DOUBLE:
				value = buffer.getDouble();
				break;
			case INVENTORY:
				//This data type really blows
				fieldlength = ((Number)values.get("count")).shortValue();
				IoBuffer inventoryData = IoBuffer.allocate(fieldlength  * DataType.SHORT.getLength());
				inventoryData.setAutoExpand(true);
				short id,health;
				byte count;
				
				for (int idx = 0; idx < fieldlength; idx++)
				{
					if(buffer.remaining() < (DataType.SHORT.getLength()))
					{
						curHex = "";
						buffer.position(bufferPos);
						return false;
					}
					id = buffer.getShort();
					inventoryData.putShort(id);
					if (id != -1)
					{
						if(buffer.remaining() < (DataType.BYTE.getLength() + DataType.SHORT.getLength()))
						{
							curHex = "";
							buffer.position(bufferPos);
							return false;
						}
						count = buffer.get();
						health = buffer.getShort();
						fieldlength += 1;
						inventoryData.put(count);
						inventoryData.putShort(health);
					}
						
				}
				value = inventoryData;
				break;
			case BYTE_ARRAY:
				if(buffer.remaining() < DataType.INT.getLength())
				{
					curHex = "";
					buffer.position(bufferPos);
					return false;
				}
				fieldlength = buffer.getInt();
				if (buffer.remaining() < fieldlength ||  fieldlength <= 0)
				{
					curHex = "";
					buffer.position(bufferPos);
					return false;
				}
				value = IoBuffer.allocate(fieldlength).put(buffer);
				break;
			case STRING:
				if(buffer.remaining() < DataType.SHORT.getLength())
				{
					curHex = "";
					buffer.position(bufferPos);
					return false;
				}
				fieldlength = (int)buffer.getShort();
				if (buffer.remaining() < fieldlength ||  fieldlength <= 0)
				{
					curHex = "";
					buffer.position(bufferPos);
					return false;
				}
				byte[] bytes = new byte[fieldlength];
				buffer.get(bytes);
				value = new String(bytes).trim();
				break;
			}
			values.put(field.getName(), value);
		}
		Packet packet = new Packet(currentPacket, values);
		currentPacket = null;
		curHex = "";
		out.write(packet);
		return true;
	}
}
