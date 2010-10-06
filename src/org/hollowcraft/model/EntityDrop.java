package org.hollowcraft.model;

public class EntityDrop {
	private short id = -1;
	private short quantityMin = 1;
	private short quantityMax = 1;
	private boolean greedy = true;
	private float probability = 1;
	private short requirement = -1;
	public EntityDrop()
	{
		/* Empty */
	}
}
