package org.opencraft.server.task.impl;

import org.opencraft.server.task.ScheduledTask;
import org.opencraft.server.io.LevelManager;
import org.opencraft.server.model.Level;
import org.slf4j.*;

/**
 * A Task that will automatically save a Level.
 * @author Adam Liszka
 */
public final class SaveLevelTask extends ScheduledTask {
	
	private static final long DELAY = 120 * 1000; // Every 2 minutes
	private Level m_lvl;
	private Logger logger = LoggerFactory.getLogger(SaveLevelTask.class);
	
	public SaveLevelTask(Level lvl) {
		super(DELAY);
		m_lvl = lvl;
		execute();
	}
	
	public void execute() {
		logger.info("Saving " + m_lvl.getName());
		if (this.getDelay() == 0) {
			this.setDelay(DELAY);
		}
		LevelManager.save(m_lvl);
	}
	
}
