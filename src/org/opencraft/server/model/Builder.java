package org.opencraft.server.model;

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
import java.util.ArrayList;
import java.lang.Math;
import java.util.logging.Logger;

/**
 * Builds a level.
 * @author Trever Fischer <tdfischer@fedoraproject.org>
 */

public class Builder {
	private byte[][][] blocks;

	private static final Logger logger = Logger.getLogger(Level.class.getName());

	int m_height;
	
	int m_depth;
	
	int m_width;

	int m_scale = 2;

	Random m_random;

	public Builder(int width, int height, int depth) {
		m_height = height;
		m_width = width;
		m_depth = depth;
		blocks = new byte[width][height][depth];
		m_random = new Random();
	}

	public Builder(int width, int height, int depth, long seed) {
		m_height = height;
		m_width = width;
		m_depth = depth;
		blocks = new byte[width][height][depth];
		m_random = new Random(seed);
	}

	public void setScale(int scale) {
		m_scale = scale;
	}

	public byte[][][] getBlocks() {
		return blocks;
	}

	public void sculptHills(int iterations, int depthAdjust) {
		int[][] heights = new int[m_width][m_height];
		int maxHeight = 1;
		for(int i = 0; i < iterations; i++) {
			if (i % 10000 == 0)
				logger.info("Raising terrain: "+i+"/"+iterations);
			int x = m_random.nextInt(m_width);
			int y = m_random.nextInt(m_height);
			int ry = m_random.nextInt(10) + 4;
			int rx = m_random.nextInt(10) + 4;
			for(int j = 0; j < m_width; j++) {
				for(int k = 0; k < m_height; k++) {
					int mod = (rx * ry) - (k - x) * (k - x) - (j - y) * (j - y);
					if(mod > 0) {
						heights[j][k] += mod;
						if(heights[j][k] > maxHeight) {
							maxHeight = heights[j][k];
						}
					}
				}
			}
		}

		for(int x = 0; x < m_width; x++) {
			for(int y = 0; y < m_height; y++) {
				//int h = (depth / 2) + (heights[x][y] * (depth / 2) / maxHeight);
				int h = (m_depth/2) + (heights[x][y] * (m_depth /2) / maxHeight)/m_scale - depthAdjust;
				int d = m_random.nextInt(8) - 4;
				for(int z = 0; z < h; z++) {
					int type = BlockConstants.DIRT;
					if(z == (h - 1)) {
						type = BlockConstants.GRASS;
					} else if(z <= (m_depth / 2 + d)) {
						type = BlockConstants.STONE;
					}
					blocks[x][y][z] = (byte) type;
				}
			}
		}
	}

	public void generateCaverns(int count) {
		for (int i = 0; i < count;i++) {
			logger.info("Generating underground erosion bubbles: "+i+"/"+count);
			int x = m_random.nextInt(m_width);
			int y = m_random.nextInt(m_height);
			int z = m_random.nextInt(m_depth/4);
			int radius = m_random.nextInt(60)+40*m_scale;
			radius = 6;
			int type = m_random.nextInt(100);
			if (type > 90)
				type = BlockConstants.LAVA;
			else if (type > 45)
				type = BlockConstants.AIR;
			else
				type = BlockConstants.WATER;
			for (int m = 0;m < 2; m++) {
				BUBBLE_GEN: for(int j = x-radius;j<x+radius*2;j++) {
					if (j < 0)
						j = 0;
					if (j >= m_width)
						break BUBBLE_GEN;
					for(int k = y-radius;k<y+radius*2;k++) {
						if (k < 0)
							k = 0;
						if (k >= m_height)
							break BUBBLE_GEN;
						for (int l = z-radius;l<z+radius;l++) {
							if (l < 0)
								l = 0;
							if (l >= m_depth)
								break BUBBLE_GEN;
							double distance = Math.sqrt(Math.pow(j-x, 2)+Math.pow(k-y, 2)+Math.pow(l-z, 2));
							if (Math.abs(distance/radius) <= Math.abs(m_random.nextGaussian())) {
								blocks[j][k][l] = (byte) type;
							}
						}
					}
				}
				x++;
			}
		}
	}

	public void buildLavaBed(int depth) {
		for (int z = 0;z < depth; z++) {
			for(int x = 0;x < m_width; x++) {
				logger.info("Building lava bed: "+(x*m_height)+"/"+(m_width*m_height));
				for (int y = 0; y < m_height; y++ ) {
					blocks[x][y][z] = (byte) BlockConstants.LAVA;
				}
			}
		}
	}

	public void carveLake() {
		int x = m_random.nextInt(m_width);
		int y = m_random.nextInt(m_height);
		int avgDepth = (m_random.nextInt(3)+1)*m_scale;
		int radius = (m_random.nextInt(30)+40)*m_scale;
		carveLake(x, y, new ArrayList<Position>(), radius, avgDepth);
	}

	private void carveLake(int x, int y, ArrayList<Position> visited, int distance, int depth) {
		if (distance == 0)
			return;
		Position cur = new Position(x, y, 0);
		for(Position p : visited)
			if (p.equals(cur)) {
				visited.add(cur);
				return;
			}
		visited.add(cur);
		if (x < 0 || y < 0 || x >= m_width || y >= m_height)
			return;
		for(int z = m_depth/2-depth;z < m_depth;z++)
			blocks[x][y][z] = (byte) BlockConstants.AIR;
		for(int z = m_depth/2-depth;z<m_depth/2-depth && z < m_depth;z++)
			blocks[x][y][z] = (byte) BlockConstants.WATER;
		carveLake(x+1, y, visited, distance-1, depth);
		carveLake(x, y+1, visited, distance-1, depth);
		carveLake(x-1, y, visited, distance-1, depth);
		carveLake(x, y-1, visited, distance-1, depth);
	}
}
