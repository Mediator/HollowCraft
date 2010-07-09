package org.opencraft.server.task.impl;

import org.opencraft.server.task.ScheduledTask;
import org.opencraft.server.io.WorldManager;
import org.opencraft.server.model.World;
import org.slf4j.*;

/**
 * A Task that will automatically save a World.
 * @author Adam Liszka
 */
public final class SaveWorldTask extends ScheduledTask {
	
	private static final long DELAY = 120 * 1000; // Every 2 minutes
	private World m_lvl;
	private Logger logger = LoggerFactory.getLogger(SaveWorldTask.class);
	
	public SaveWorldTask(World lvl) {
		super(DELAY);
		m_lvl = lvl;
		execute();
	}
	
	public void execute() {
		logger.info("Saving " + m_lvl.getName());
		if (this.getDelay() == 0) {
			this.setDelay(DELAY);
		}
		WorldManager.save(m_lvl);
	}
	
}
