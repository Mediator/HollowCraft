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
import java.util.Queue;
import java.util.LinkedList;
import java.util.ArrayList;
import java.awt.Point;
import java.lang.Math;
import java.util.logging.Logger;

/**
 * Builds a level.
 * @author Trever Fischer <tdfischer@fedoraproject.org>
 */

public class Builder {
	private byte[][][] blocks;

	private int[][] m_contour;

	private static final Logger logger = Logger.getLogger(Level.class.getName());

	int m_height;
	
	int m_depth;
	
	int m_width;

	int m_scale = 1;

	Random m_random;

	public Builder(int width, int height, int depth) {
		m_height  = height;
		m_width   = width;
		m_depth   = depth;
		blocks    = new byte[width][height][depth];
		m_contour = new int[width][height];

		for(int x = 0;x<m_width;x++)
			for(int y = 0;y<m_height;y++)
				m_contour[x][y] = 0;
		m_random = new Random();
	}

	public Builder(int width, int height, int depth, long seed) {
		m_height = height;
		m_width = width;
		m_depth = depth;
		blocks = new byte[width][height][depth];
		m_random = new Random(seed);
		for(int x = 0;x<m_width;x++)
			for(int y = 0;y<m_height;y++)
				m_contour[x][y] = 0;
	}

	public void setScale(int scale) {
		m_scale = scale;
	}

	public byte[][][] getBlocks() {
		return blocks;
	}

	public void applyContour() {
		int maxHeight = 1;
		for (int x = 0;x<m_width;x++) {
			for (int y = 0;y < m_height;y++) {
				if (m_contour[x][y] > maxHeight)
					maxHeight = m_contour[x][y];
			}
		}
		for(int x = 0; x < m_width; x++) {
			//logger.info("Applying contour: "+(x*m_height)+"/"+(m_height*m_width));
			for(int y = 0; y < m_height; y++) {
				int h = Math.max(0, Math.min(m_depth-1, (m_depth/2) + m_contour[x][y]));
				int d = m_random.nextInt(8) - 4;
				for(int z = 0; z < m_depth; z++) {
					int type = BlockConstants.AIR;
					if (z >= h) {
						type = BlockConstants.AIR;
					} else if(z == (h - 1)) {
						type = BlockConstants.GRASS;
					} else if(z < (h - 1) && z > (h -5 )) {
						type = BlockConstants.DIRT;
					} else if(z <= (h - 5 )) {
						type = BlockConstants.STONE;
					}
					blocks[x][y][z] = (byte) type;
				}
			}
		}
	}

	public void sculptHill(int centerX, int centerY, int height, int radius) {
		sculptHill(centerX, centerY, height, radius, false);
	}

	public void sculptHill(int centerX, int centerY, int height, int radius, boolean additive) {
		int maxHeight = 1;
		if (additive)
			m_contour[centerX][centerY] += height;
		else
			m_contour[centerX][centerY] = height;

		for(int x = Math.max(0, centerX-radius);x < Math.min(m_width-1, centerX+radius);x++) {
			for(int y = Math.max(0, centerY-radius);y < Math.min(m_height-1, centerY+radius);y++) {
				double distance = Math.sqrt(Math.pow(x-centerX,2)+Math.pow(y-centerY,2));
				if (Math.abs(radius-distance) <= 1)
					interpolateLine(x, y, centerX, centerY);
			}
		}
	}

	private void interpolateLine(int startX, int startY, int destX, int destY) {
		int startHeight = m_contour[startX][startY];
		int endHeight = m_contour[destX][destY];
		double distance = Math.sqrt(Math.pow(startX-destX,2)+Math.pow(startY-destY,2));
		int nextX = startX;
		int nextY = startY;
		double value = Math.sqrt(Math.pow(nextX-destX,2)+Math.pow(nextY-destY,2))/distance;
		while (value > 0) {

			value = Math.sqrt(Math.pow(nextX-destX,2)+Math.pow(nextY-destY,2))/distance;

			if (value < 0.5)
				m_contour[nextX][nextY] = (int)((startHeight-endHeight)/2*Math.pow(value*2,3) + endHeight);
			else
				m_contour[nextX][nextY] = (int)((startHeight-endHeight)/2*(Math.pow(value*2-2,3) + 2) + endHeight);

			double direction = Math.atan2(destY-nextY, destX-nextX);
			double dx = Math.cos(direction);
			double dy = Math.sin(direction);
			if (dx == 1) {
				dx = 1;
				dy = 0;
			} else if (dx == -1) {
				dx = -1;
				dy = 0;
			} else if (dy == 1) {
				dy = 1;
				dx = 0;
			} else if (dy == -1) {
				dy = -1;
				dx = 0;
			}
			nextX += (int) (Math.ceil(Math.abs(dx)) * ((dx < 0) ? -1 : 1));
			nextY += (int) (Math.ceil(Math.abs(dy)) * ((dy < 0) ? -1 : 1));
		}
	}

	public void sculptHills(int iterations) {
		for(int i = 0; i < iterations; i++) {
			if (i % 1000 == 0)
				logger.info("Sculpting hills: "+i+"/"+iterations);
			int x = m_random.nextInt(m_width);
			int y = m_random.nextInt(m_height);
			int height = (m_random.nextInt(10)-5)*m_scale;
			int radius = m_random.nextInt(20) + 15;
			sculptHill(x, y, height, radius);
		}
	}

	public void generateCaverns(int count) {
		for (int i = 0; i < count;i++) {
			//logger.info("Generating underground erosion bubbles: "+i+"/"+count);
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
				//logger.info("Building lava bed: "+(x*m_height)+"/"+(m_width*m_height));
				for (int y = 0; y < m_height; y++ ) {
					blocks[x][y][z] = (byte) BlockConstants.LAVA;
				}
			}
		}
	}

	public void carveLake() {
		int x = m_random.nextInt(m_width);
		int y = m_random.nextInt(m_height);
		int avgDepth = (m_random.nextInt(6)+6)*m_scale;
		int radius = (m_random.nextInt(10)+10)*m_scale;
		carveLake(x, y, new ArrayList<Position>(), radius, avgDepth);
		int edgeHeight = avgDepth/m_scale;
	}

	private void carveLake(int x, int y, ArrayList<Position> visited, int distance, int depth) {
		if (distance == 0)
			return;
		if (x < 0 || y < 0 || x >= m_width || y >= m_height)
			return;
		if (depth <= 0)
			return;
		Position cur = new Position(x, y, 0);
		for(Position p : visited)
			if (p.equals(cur))
				return;
		visited.add(cur);

		int delta = depth;

		carveLake(x+1, y, visited, distance-1, delta);
		carveLake(x, y+1, visited, distance-1, delta);
		carveLake(x-1, y, visited, distance-1, delta);
		carveLake(x, y-1, visited, distance/2, delta);
		sculptHill(x, y, -depth, distance);
	}

	public void carveCanyon() {
		int startX = m_random.nextInt(m_width);
		int startY = m_random.nextInt(m_height);
		double direction = m_random.nextDouble()*Math.PI*2;
		int depth = m_random.nextInt(4)+4*m_scale;
		carveCanyon(startX, startY, direction, depth);
		carveCanyon(startX, startY, m_random.nextDouble()*Math.PI*2, depth);
	}

	private void carveCanyon(int x, int y, double direction, int depth) {
		int nextX = x;
		int nextY = y;
		while (nextX > 0 && nextY > 0 && nextY < m_height-1 && nextX < m_width-1) {
			sculptHill(nextX, nextY, -depth, 10, true);
			/*for(int i = Math.max(0,nextX-6);i<Math.min(m_width-1, nextX+6);i++) {
				for(int j = Math.max(0,nextY-6);j<Math.min(m_height-1,nextY+6);j++) {
					m_contour[i][j] = -depth;
				}
			}*/
			double choice = m_random.nextGaussian();
			if (choice > 0.7)
				direction+=m_random.nextDouble()*Math.PI*2;
			if (choice < -0.7)
				direction-=m_random.nextDouble()*Math.PI*2;

			double dx = Math.cos(direction);
			double dy = Math.sin(direction);
			nextX += Math.ceil(Math.abs(dx)) * ((dx < 0) ? -1 : 1) * 4;
			nextY += Math.ceil(Math.abs(dy)) * ((dy < 0) ? -1 : 1) * 4;
		}
	}

	public void simulateOceanFlood() {
		if (m_width < 2 || m_height < 2) { return; }

		LinkedList<Point> toFlood = new LinkedList<Point>();
		int oceanLevel = m_depth / 2 - 1;

		for (int x = 0; x < m_width; x++) {
			if (blocks[x][0][oceanLevel] == BlockConstants.AIR) {
				floodBlock(x, 0, oceanLevel);
				floodBlock(x, m_height - 1, oceanLevel);
				if (blocks[x][1][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point(x, 1));
				} else if (blocks[x][m_height - 1][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point(x, m_height - 2));
				}
			}
		}

		if (blocks[1][0][oceanLevel] == BlockConstants.AIR) {
			floodBlock(1, 0, oceanLevel);
		}

		if (blocks[m_width - 2][0][oceanLevel] == BlockConstants.AIR) {
			floodBlock(m_width - 2, 0, oceanLevel);
		}

		if (blocks[1][m_height - 2][oceanLevel] == BlockConstants.AIR) {
			floodBlock(1, m_height - 2, oceanLevel);
		}

		if (blocks[m_width - 2][m_height - 2][oceanLevel] == BlockConstants.AIR) {
			floodBlock(m_width - 2, m_height - 2, oceanLevel);
		}

		for (int y = 2; y < m_height - 2; y++) {
			if (blocks[0][y][oceanLevel] == BlockConstants.AIR) {
				floodBlock(0, y, oceanLevel);
				floodBlock(m_width - 1, y, oceanLevel);
				if (blocks[1][y][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point(1, y));
				} else if (blocks[m_width - 2][y][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point(m_width - 2, y));
				}
			}
		}

		while (toFlood.size() > 0) {
			Point p = toFlood.removeFirst();
			if (blocks[(int)(p.getX())][(int)(p.getY())][oceanLevel] != BlockConstants.WATER) {

				floodBlock((int)(p.getX()), (int)(p.getY()), oceanLevel);

				if (blocks[(int)(p.getX() - 1)][(int)(p.getY())][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point((int)(p.getX() - 1), (int)(p.getY())));
				}
				
				if (blocks[(int)(p.getX() + 1)][(int)(p.getY())][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point((int)(p.getX() + 1), (int)(p.getY())));
				}
				
				if (blocks[(int)(p.getX())][(int)(p.getY() - 1)][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point((int)(p.getX()), (int)(p.getY() - 1)));
				}
				
				if (blocks[(int)(p.getX())][(int)(p.getY() + 1)][oceanLevel] == BlockConstants.AIR) {
					toFlood.add(new Point((int)(p.getX()), (int)(p.getY() + 1)));
				}
			}
		}
	}

	private void floodBlock(int x, int y, int oceanLevel) {
		for (int z = oceanLevel; true; z--) {
			if (z < 0) { break; }
				if (blocks[x][y][z] == BlockConstants.AIR) {
					blocks[x][y][z] = BlockConstants.WATER;
				} else if (blocks[x][y][z + 1] == BlockConstants.WATER && (blocks[x][y][z] == BlockConstants.DIRT || blocks[x][y][z] == BlockConstants.GRASS)) {
					blocks[x][y][z] = BlockConstants.SAND;
					break;
				}
			}
	}
}
