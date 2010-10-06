package org.hollowcraft.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */


import org.hollowcraft.server.model.BlockBehaviour;

/**
 * Represents an individual item type.
 * @author Caleb Champlin
 */
public class ItemDefinition {
	
	/**
	 * The item name.
	 */
	private String name;
	
	/**
	 * The item ID.
	 */
	private int iid;
	
	
	/**
	 * Constructor.
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private ItemDefinition(String name, int bid) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.name = name;
		this.iid = bid;

	}
	
	/**
	 * Resolves this object.
	 * @return A resolved object.
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private Object readResolve() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return new ItemDefinition(name, iid);
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
	public int getId() {
		return iid;
	}
}
