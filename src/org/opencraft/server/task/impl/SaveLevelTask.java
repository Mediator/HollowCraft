package org.opencraft.server.task.impl;

import org.opencraft.server.task.ScheduledTask;
import org.opencraft.server.io.LevelManager;
import org.opencraft.server.model.Level;

/**
 * A Task that will automatically save a Level.
 * @author Adam Liszka
 */
public final class SaveLevelTask extends ScheduledTask {
	
	private static final long DELAY = 60000;
	private Level m_lvl;
	
	public SaveLevelTask(Level lvl) {
		super(DELAY);
		m_lvl = lvl;
		System.out.println(m_lvl.getName());
	}
	
	public void execute() {
		System.out.println("Saving: data/maps/" + m_lvl.getName() + ".mclevel");
		if (this.getDelay() == 0) {
			this.setDelay(DELAY);
		}
		LevelManager.save(m_lvl);
	}
	
}
