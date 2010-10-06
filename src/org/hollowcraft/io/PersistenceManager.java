package org.hollowcraft.io;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.hollowcraft.model.BlockDefinition;
import org.hollowcraft.model.BlockManager;
import org.hollowcraft.model.ItemDefinition;
import org.hollowcraft.model.ItemManager;
import org.hollowcraft.server.net.packet.PacketDefinition;
import org.hollowcraft.server.net.packet.PacketField;
import org.hollowcraft.server.net.packet.PacketManager;
import org.slf4j.*;

/**
 * A class which manages XStream persistence.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public final class PersistenceManager {
	
	/**
	 * The singleton instance.
	 */
	private static final PersistenceManager INSTANCE = new PersistenceManager();

	private static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);
	
	/**
	 * Gets the persistence manager instance.
	 * @return The persistence manager instance.
	 */
	public static PersistenceManager getPersistenceManager() {
		return INSTANCE;
	}
	
	/**
	 * The XStream object.
	 */
	private final XStream xstream = new XStream(new DomDriver());
	
	/**
	 * Initializes the persistence manager.
	 */
	private PersistenceManager() {
		xstream.alias("packets", PacketManager.class);
		xstream.alias("packet", PacketDefinition.class);
		xstream.alias("field", PacketField.class);
		xstream.alias("blocks", BlockManager.class);
		xstream.alias("block", BlockDefinition.class);
		xstream.alias("items", ItemManager.class);
		xstream.alias("item", ItemDefinition.class);
	}
	
	/**
	 * Loads an object from an XML file.
	 * @param file The file.
	 * @return The object.
	 */
	public Object load(String file) {
		logger.trace("Loading from {}", file);
		Object ret;
		try {
			ret = xstream.fromXML(new FileInputStream(file));
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		logger.trace("Loaded!");
		return ret;
	}
	
	/**
	 * Saves an object to an XML file.
	 * @param file The file.
	 * @param o The object.
	 */
	public void save(String file, Object o) {
		try {
			xstream.toXML(o, new FileOutputStream(file));
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Gets the xstream object.
	 * @return the xstream object.
	 */
	public XStream getXStream() {
		return xstream;
	}
	
}
