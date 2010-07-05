package org.opencraft.server.model.impl.builders;

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

import org.opencraft.server.model.Builder;
import org.opencraft.model.BlockConstants;
import org.opencraft.model.Level;

/**
 * Builds a level based off equations and a seed.
 * @author Adam Liszka
 */

public class InfBuilder extends Builder {

	private int m_scale = 32;

	public InfBuilder(Level level) {
		super(level);
	}

	public void  generate() {
		for (int x = 0; x < m_width; x++) {
			for (int y = 0; y < m_height; y++) {
				int i = m_width / 2 - m_width + x;
				int j = m_height / 2 - m_height + y;
				int k = calcPoint(i, j, m_seed);
				if (k < 0) k = 0;
				if (k > m_depth - 1) k = m_depth - 1;
				m_blocks[x][y][k] = BlockConstants.GRASS;
				for (int z = k - 1; z >= 0; z--) {
					m_blocks[x][y][z] = BlockConstants.DIRT;
				}
			}
		}
		simulateOceanFlood();
	}

	private int calcPoint(double x, double y, long seed) {
		//int k = m_scale;
		x = x * Math.PI / 12;
		y = y * Math.PI / 12;

		double base = (arctan(-8 * x) + arctan(-8 * y)) + (arctan(x / 2) + arctan(y / 2)) + (arctan(-1 * x / 10) + arctan(-1 * y / 10)) + (arctan(x / 20) + arctan(y / 80));
		double a = (cos(x / 18) + Math.pow(arctan(x), 2) / 2 + cos(y / 18) * Math.pow(arctan(y), 2) + Math.pow(cos(x / 2), 2)  + Math.pow(cos(y / 2), 3)) * m_scale;

		return (int)(a + base - m_depth * 1.05);
	}

	public void simulateOceanFlood() {
		int oceanLevel = m_depth / 2 - 1;
		for (int x = 0; x < m_width; x++) {
			for (int y = 0; y < m_height; y++) {
				if (m_blocks[x][y][oceanLevel] == BlockConstants.AIR) {
					floodBlock(x, y, oceanLevel);
				}
			}
		}
	}

	private void floodBlock(int x, int y, int oceanLevel) {
		for (int z = oceanLevel; true; z--) {
			if (z < 0) { break; }
				if (m_blocks[x][y][z] == BlockConstants.AIR) {
					m_blocks[x][y][z] = BlockConstants.WATER;
				} else if (m_blocks[x][y][z + 1] == BlockConstants.WATER && (m_blocks[x][y][z] == BlockConstants.DIRT || m_blocks[x][y][z] == BlockConstants.GRASS)) {
					m_blocks[x][y][z] = BlockConstants.SAND;
					break;
				}
			}
	}

	protected double sin(double value) {
		return Math.sin(value / (m_scale / 4));
	}

	protected double cos(double value) {
		return Math.cos(value / (m_scale / 4));
	
	}

	protected double tanh(double value) {
		return Math.tanh(value / (m_scale / 4));
	}

	protected double arctan(double value) {
		return Math.atan(value / (m_scale / 4));
	}

	protected double csc(double value) {
		return 1 / Math.sin(value / (m_scale / 4));
	}

	protected double sec(double value) {
		return 1 / Math.cos(value / (m_scale / 4));
	}
}
