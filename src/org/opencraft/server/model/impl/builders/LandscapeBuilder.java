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

import java.util.Random;
import java.util.Queue;
import java.awt.Point;
import java.util.LinkedList;
import java.util.ArrayList;
import java.lang.Math;

import org.opencraft.server.model.Builder;
import org.opencraft.server.model.BlockConstants;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.Level;
import org.slf4j.*;

/**
 * Builds a level.
 * @author Trever Fischer <tdfischer@fedoraproject.org>
 */

public class LandscapeBuilder extends Builder {

	public LandscapeBuilder(Level level) {
		super(level);
	}

	public void generate() {
		raiseTerrain();
		buildLavaBed(2);
		simulateOceanFlood();
		//plantTrees();
	}

	public void plantTrees() {
		ArrayList<Position> treeList = new ArrayList<Position>();
		for(int x = 0;x<m_width;x++) {
			for(int y = 0;y<m_height;y++) {
				boolean tooClose = false;
				for(Position p : treeList) {
					double distance = Math.sqrt(Math.pow(p.getX()-x,2)+Math.pow(p.getY()-y,2));
					if (distance < 30)
						tooClose = true;
				}
				if (!tooClose) {
					if (m_random.nextInt(100) <= 5) {
						for(int z = m_depth-1;z>0;z--) {
							if ((m_blocks[x][y][z] == BlockConstants.DIRT || m_blocks[x][y][z] == BlockConstants.GRASS) && m_blocks[x][y][z+1] == BlockConstants.AIR) {
								plantTree(x, y, z);
								treeList.add(new Position(x, y, z));
								break;
							} else if (z < m_depth-1 && m_blocks[x][y][z+1] != BlockConstants.AIR) {
								break;
							}
						}
					}
				}
			}
		}
	}

	public void plantTree(int rootX, int rootY, int rootZ) {
		for(int z = rootZ;z < rootZ + 5;z++) {
			m_blocks[rootX][rootY][z] = BlockConstants.TREE_TRUNK;
		}
		for(int width = 4;width>0;width--) {
			leafLayer(rootX, rootY, rootZ+5+(4-width), width);
		}
	}

	public void leafLayer(int cx, int cy, int cz, int width) {
		for(int x = Math.max(0, cx-width);x<Math.min(m_width, cx+width);x++) {
			for(int y = Math.max(0, cy-width);y<Math.min(m_height, cy+width);y++) {
				m_blocks[x][y][cz] = BlockConstants.LEAVES;
			}
		}
	}

	public void raiseTerrain() {
		boolean[][][] prevContour = new boolean[m_width][m_height][m_depth];
		boolean[][][] curContour = new boolean[m_width][m_height][m_depth];
		for(int x = 0;x<m_width;x++) {
			for(int y = 0;y<m_height;y++) {
				for(int z = 0;z<m_depth;z++) {
					curContour[x][y][z] = (m_random.nextInt(100) <= 45);
				}
			}
		}
		for(int count = 0;count<3;count++) {
			System.arraycopy(curContour, 0, prevContour, 0, curContour.length);
			for(int x = 0;x<m_width;x++) {
				for(int y = 0;y<m_height;y++) {
					for(int z = 0;z<m_depth;z++) {
						curContour[x][y][z] = simulateCell(prevContour, x, y, z);
					}
				}
			}
		}
		for(int x = 0;x<m_width;x++) {
			for(int y = 0;y<m_height;y++) {
				for(int z = 0;z<m_depth;z++) {
					byte type = (byte)BlockConstants.AIR;
					if (curContour[x][y][m_depth-1-z]) {
						if (z > 1) {
							if (curContour[x][y][m_depth-z-2])
								type = (byte)BlockConstants.DIRT;
							else
								type = (byte)BlockConstants.GRASS;
						} else {
							type =(byte)BlockConstants.DIRT;
						}
					}
					m_blocks[x][y][z] = type;
				}
			}
		}
	}

	private boolean simulateCell(boolean[][][] grid, int x, int y, int z) {
		double count = 0;
		boolean isAlive = grid[x][y][z];
		if (isAlive)
			count++;
		if (x > 0 && x < m_width-1) {
			if (grid[x-1][y][z])
				count++;
			if (grid[x+1][y][z])
				count++;
		}

		if (y > 0 && y < m_height-1) {
			if (grid[x][y+1][z])
				count++;
			if (grid[x][y-1][z])
				count++;
		}

		if (z > 0 && z < m_depth-1) {
			if (grid[x][y][z-1])
				count+=2;
			if (grid[x][y][z+1])
				count+=0.9;
		}

		if (y > 0) {
			if (x > 0) {
				if (z > 0)
					if (grid[x-1][y-1][z-1])
						count+=0.7;
				if (z < m_depth-1)
					if (grid[x-1][y-1][z+1])
						count+=0.5;
			}
			if (x < m_width-1) {
				if (z > 0)
					if (grid[x+1][y-1][z-1])
						count+=0.7;
				if (z < m_depth-1)
					if (grid[x+1][y-1][z+1])
						count+=0.5;
			}
		}
		if (y < m_height-1) {
			if (x > 0) {
				if (z > 0)
					if (grid[x-1][y+1][z-1])
						count+=0.7;
				if (z < m_depth-1)
					if (grid[x-1][y+1][z+1])
						count+=0.5;
			}
			if (x < m_width-1) {
				if (z > 0)
					if (grid[x+1][y+1][z-1])
						count+=0.7;
				if (z < m_depth-1)
					if (grid[x+1][y+1][z+1])
						count+=0.5;
			}
		}
		return count >= 5.7;
	}

	public byte[][][] getBlocks() {
		return m_blocks;
	}

	/*public void applyContour() {
		int maxHeight = 1;
		for (int x = 0;x<m_width;x++) {
			for (int y = 0;y < m_height;y++) {
				if (m_contour[x][y] > maxHeight)
					maxHeight = m_contour[x][y];
			}
		}
		m_logger.debug("Applying contour");
		for(int x = 0; x < m_width; x++) {
			for(int y = 0; y < m_height; y++) {
				int h = Math.max(0, Math.min(m_depth-1, (m_depth/2) + m_contour[x][y]));
				int d = m_random.nextInt(8) - 4;
				for(int z = 0; z < m_depth; z++) {
					int type = BlockConstants.AIR;
					if (z >= h && z < m_depth/2-1) {
						type = BlockConstants.WATER;
					} else if (z >= h) {
						type = BlockConstants.AIR;
					} else if(z == (h - 1)) {
						type = BlockConstants.GRASS;
					} else if(z < (h - 1) && z > (h -5 )) {
						type = BlockConstants.DIRT;
					} else if(z <= (h - 5 )) {
						type = BlockConstants.STONE;
					}
					m_blocks[x][y][z] = (byte) type;
				}
			}
		}
	}*/

	public void buildLavaBed(int depth) {
		m_logger.debug("Building lava bed.");
		for (int z = 0;z < depth; z++) {
			for(int x = 0;x < m_width; x++) {
				for (int y = 0; y < m_height; y++ ) {
					m_blocks[x][y][z] = (byte) BlockConstants.LAVA;
				}
			}
		}
	}


	public void simulateOceanFlood() {
		if (m_width < 2 || m_height < 2) { return; }

		LinkedList<Point> toFlood = new LinkedList<Point>();
		int oceanLevel = m_depth / 2 - 1;

		for (int x = 0; x < m_width; x++) {
			if (m_blocks[x][0][oceanLevel] == BlockConstants.AIR) {
				floodBlock(x, 0, oceanLevel);
				floodBlock(x, m_height - 1, oceanLevel);
				if (m_blocks[x][1][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point(x, 1));
				} else if (m_blocks[x][m_height - 1][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point(x, m_height - 2));
				}
			}
		}

		if (m_blocks[1][0][oceanLevel] == BlockConstants.AIR) {
			floodBlock(1, 0, oceanLevel);
		}

		if (m_blocks[m_width - 2][0][oceanLevel] == BlockConstants.AIR) {
			floodBlock(m_width - 2, 0, oceanLevel);
		}

		if (m_blocks[1][m_height - 2][oceanLevel] == BlockConstants.AIR) {
			floodBlock(1, m_height - 2, oceanLevel);
		}

		if (m_blocks[m_width - 2][m_height - 2][oceanLevel] == BlockConstants.AIR) {
			floodBlock(m_width - 2, m_height - 2, oceanLevel);
		}

		for (int y = 2; y < m_height - 2; y++) {
			if (m_blocks[0][y][oceanLevel] == BlockConstants.AIR) {
				floodBlock(0, y, oceanLevel);
				floodBlock(m_width - 1, y, oceanLevel);
				if (m_blocks[1][y][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point(1, y));
				} else if (m_blocks[m_width - 2][y][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point(m_width - 2, y));
				}
			}
		}

		while (toFlood.size() > 0) {
			Point p = toFlood.removeFirst();
			if (m_blocks[(int)(p.getX())][(int)(p.getY())][oceanLevel] != BlockConstants.WATER) {

				floodBlock((int)(p.getX()), (int)(p.getY()), oceanLevel);

				if (p.getX() > 0 && m_blocks[(int)(p.getX() - 1)][(int)(p.getY())][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point((int)(p.getX() - 1), (int)(p.getY())));
				}
				
				if (p.getX() < m_width -1 && m_blocks[(int)(p.getX() + 1)][(int)(p.getY())][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point((int)(p.getX() + 1), (int)(p.getY())));
				}
				
				if (p.getY() > 0 && m_blocks[(int)(p.getX())][(int)(p.getY() - 1)][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point((int)(p.getX()), (int)(p.getY() - 1)));
				}
				
				if (p.getY() < m_height -1 && m_blocks[(int)(p.getX())][(int)(p.getY() + 1)][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point((int)(p.getX()), (int)(p.getY() + 1)));
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
}

