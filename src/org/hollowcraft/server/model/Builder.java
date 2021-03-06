package org.hollowcraft.server.model;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
/*
 * OpenCraft License
 * 
 * Copyright (c) 2010 Trever Fischer <tdfischer@fedoraproject.org>
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

import java.util.Random;

import org.hollowcraft.model.BlockManager;
import org.hollowcraft.model.ClassicLevel;
import org.hollowcraft.server.model.impl.worlds.ClassicWorld;
import org.slf4j.*;

/**
 * Level Builder Interface.
 * @author Adam Liszka
 * @author Caleb Champlin
 */

public abstract class Builder {
	protected byte[][][] m_blocks;

	protected static final Logger m_logger = LoggerFactory.getLogger(Builder.class);

	protected int m_height;
	
	protected int m_depth;
	
	protected int m_width;

	protected Random m_random;

	protected long m_seed;

	// for use in theming
	protected String m_theme;
	protected short m_grassBlock;
	protected short m_leavesBlock;

	public void setSeed(long seed) {
		m_seed = seed;
		m_random = new Random(seed);
	}

	public Builder(ClassicLevel level) {
		setSummer();
		setLevel(level);
		m_seed = 0;
		m_random = new Random();
	}

	public void setLevel(ClassicLevel level) {
		m_height = level.getHeight();
		m_width = level.getWidth();
		m_depth = level.getDepth();
		m_blocks = new byte[m_width][m_height][m_depth];
	}

	public abstract void generate();

	public byte[][][] getBlocks() {
		return (byte[][][])(m_blocks.clone());
	}

	public void setSummer() {
		m_grassBlock = BlockManager.getBlockManager().getBlock("GRASS").getId();
		m_leavesBlock = BlockManager.getBlockManager().getBlock("LEAVES").getId();
		m_theme = "Summer";
	}

	public void setWinter() {
		m_grassBlock = BlockManager.getBlockManager().getBlock("CLOTH").getId();
		m_leavesBlock = BlockManager.getBlockManager().getBlock("CLOTH_WHITE").getId();
		m_theme = "Winter";
	}

	public void setOasis() {
		m_grassBlock = BlockManager.getBlockManager().getBlock("SAND").getId();
		m_leavesBlock = BlockManager.getBlockManager().getBlock("LEAVES").getId();
		m_theme = "Oasis";
	}
}
