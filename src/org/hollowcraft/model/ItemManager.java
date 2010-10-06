package org.hollowcraft.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */


import java.util.LinkedList;
import java.util.List;

import org.hollowcraft.io.PersistenceManager;
import org.hollowcraft.server.Configuration;


/**
 * A class which manages <code>ItemDefinition</code>s and
 * <code>ItemBehavior</code>s.
 * @author Caleb Champlin
 */
public final class ItemManager {
	
	/**
	 * The packet manager instance.
	 */
	private static final ItemManager INSTANCE = (ItemManager) PersistenceManager.getPersistenceManager().load(Configuration.getConfiguration().getItemDefinitions());
	
	/**
	 * Gets the packet manager instance.
	 * @return The packet manager instance.
	 */
	public static ItemManager getItemManager() {
		return INSTANCE;
	}
	
	/**
	 * A list of the blocks.
	 */
	private List<ItemDefinition> itemsList = new LinkedList<ItemDefinition>();
	
	/**
	 * The item array (faster access by opcode than list iteration).
	 */
	private transient ItemDefinition[] itemsArray;
	
	/**
	 * Default private constructor.
	 */
	private ItemManager() {
		/* empty */
	}
	
	/**
	 * Resolves the item manager after deserialization.
	 * @return The resolved object.
	 */
	private Object readResolve() {
		itemsArray = new ItemDefinition[256];
		for (ItemDefinition def : itemsList) {
			itemsArray[def.getId()] = def;
		}
		return this;
	}
	
	/**
	 * Gets an incoming item definition.
	 * @param id The id.
	 * @return The item definition.
	 */
	public ItemDefinition getBlock(int id) {
		return itemsArray[id];
	}
	
	
	/**
	 * Gets an incoming item definition.
	 * @param name The name of the block.
	 * @return The item definition.
	 */
	public ItemDefinition getBlock(String name) {
		for (ItemDefinition def : itemsArray)
		{
			if (def != null)
				if (def.getName().equalsIgnoreCase(name))
					return def;
		}
		return null;
	}
	
}
