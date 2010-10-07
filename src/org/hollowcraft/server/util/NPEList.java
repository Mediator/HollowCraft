package org.hollowcraft.server.util;
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hollowcraft.server.model.NPEntity;
import org.hollowcraft.server.model.Player;
import org.slf4j.*;


/**
 * A class which manages the list non-player entities.
 * @author Caleb Champlin
 */
public class NPEList {
	
	private static final Logger logger = LoggerFactory.getLogger(NPEList.class);

	/**
	 * The player array.
	 */
	private final Set<NPEntity> entities = new HashSet<NPEntity>();
	
	
	private int nextID;
	/**
	 * Default public constructor.
	 */
	public NPEList() {
		/* empty */
		nextID = 500;
	}
	
	/**
	 * Gets a list of entities.
	 * @return A list of online players.
	 */
	public Set<NPEntity> getEntities() {
		
		return Collections.unmodifiableSet(entities);
	}
	
	/**
	 * Adds an entity.
	 * @param entity The new entity.
	 * @return <code>true</code> if they could be added, <code>false</code> if
	 * not.
	 */
	public boolean add(NPEntity entity) {
		entity.setId(nextID++);
		return entities.add(entity);
	}
	
	/**
	 * Removes an entity.
	 * @param entity The entity to remove.
	 */
	public void remove(NPEntity entity) {
		entities.remove(entity);
	}
	
	/**
	 * Gets the number of loaded entities.
	 * @return The list size.
	 */
	public int size() {
		return entities.size();
	}
	
}
