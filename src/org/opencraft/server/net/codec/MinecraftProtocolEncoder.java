package org.opencraft.server.net.codec;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe and Brett Russell.
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

import java.util.Arrays;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.opencraft.server.net.packet.Packet;
import org.opencraft.server.net.packet.PacketDefinition;
import org.opencraft.server.net.packet.PacketField;

/**
 * An implementation of a <code>ProtocolEncoder</code> which encodes Minecraft
 * packet objects into buffers and then dispatches them.
 * @author Graham Edgecombe
 *
 */
public final class MinecraftProtocolEncoder extends ProtocolEncoderAdapter {

	@Override
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		Packet packet = (Packet) message;
		PacketDefinition def = packet.getDefinition();
		IoBuffer buf = IoBuffer.allocate(def.getLength() + 1);
		buf.put((byte) def.getOpcode());
		for(PacketField field : def.getFields()) {
			switch(field.getType()) {
			case BYTE:
				buf.put(packet.getNumericField(field.getName()).byteValue());
				break;
			case SHORT:
				buf.putShort(packet.getNumericField(field.getName()).shortValue());
				break;
			case INT:
				buf.putInt(packet.getNumericField(field.getName()).intValue());
				break;
			case LONG:
				buf.putLong(packet.getNumericField(field.getName()).longValue());
				break;
			case BYTE_ARRAY:
				byte[] data = packet.getByteArrayField(field.getName());
				byte[] resized = Arrays.copyOf(data, 1024);
				buf.put(resized);
				break;
			case STRING:
				String str = packet.getStringField(field.getName());
				data = str.getBytes();
				resized = Arrays.copyOf(data, 64);
				for(int i = 0; i < resized.length; i++) {
					if(resized[i] == 0) {
						resized[i] = ' ';
					}
				}
				buf.put(resized);
				break;
			}
		}
		buf.flip();
		out.write(buf);
	}

}
