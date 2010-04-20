package org.opencraft.server.task.impl;

import org.apache.mina.core.session.IoSession;
import org.opencraft.server.net.MinecraftSession;
import org.opencraft.server.task.ScheduledTask;
import org.opencraft.server.io.NBTFileHandler;
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
	}
	
	@Override
	public void execute() {
		System.out.println("Saving: data/maps/" + m_lvl.getName() + ".mclevel");
		if (this.getDelay() == 0) {
			this.setDelay(DELAY);
		}
		NBTFileHandler.save(m_lvl, "data/maps/" + m_lvl.getName() + ".mclevel", true);
	}
	
}
