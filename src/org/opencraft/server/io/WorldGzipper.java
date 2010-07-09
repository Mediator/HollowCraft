package org.opencraft.server.io;

/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, S�ren Enevoldsen and Brett Russell.
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

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

import org.apache.mina.core.buffer.IoBuffer;
import org.opencraft.server.model.World;
import org.opencraft.server.net.MinecraftSession;
import org.slf4j.*;

/**
 * A utility class for gzipping levels.
 * @author Graham Edgecombe
 */
public final class WorldGzipper {
	
	/**
	 * The singleton instance.
	 */
	private static final WorldGzipper INSTANCE = new WorldGzipper();
	
	/**
	 * Gets the level gzipper.
	 * @return The level gzipper.
	 */
	public static WorldGzipper getWorldGzipper() {
		return INSTANCE;
	}
	
	/**
	 * The executor service.
	 */
	private ExecutorService service = Executors.newCachedThreadPool();
	
	/**
	 * Default private constructor.
	 */
	private WorldGzipper() {
		/* empty */
	}

	private static final Logger logger = LoggerFactory.getLogger(WorldGzipper.class);

	private class ChunkOutputStream extends OutputStream {
		private MinecraftSession m_session;
		private byte[] m_chunk;
		private boolean m_closed = false;
		private int m_blocksSent = 0;
		private int m_blockCount;
		public ChunkOutputStream(MinecraftSession session, int blockCount) {
			m_session = session;
			m_blockCount = blockCount;
			m_chunk = new byte[0];
		}

		public void close() {
			logger.trace("Closing gzip output");
			m_closed = true;
			flush();
		}

		public void flush() {
			if (m_chunk.length == 1024 || m_closed) {
				logger.trace("{} bytes in buffer. Flushing for real.", m_chunk.length);
				int percent = (int) ((double)m_blocksSent/m_blockCount * 255D);
				m_blocksSent++;
				if (percent > 255)
					percent = 254;
				if (m_closed)
					percent = 255;
				m_session.getActionSender().sendWorldBlock(m_chunk.length, m_chunk, percent);
				m_chunk = new byte[0];
				m_session.getActionSender().sendWorldFinish();
				logger.trace("Chunk {}/{} sent.", m_blocksSent, m_blockCount);
			}
		}

		public void write(int b) {
			byte[] newChunk = new byte[m_chunk.length+1];
			newChunk[m_chunk.length] = (byte)b;
			m_chunk = newChunk;
			flush();
		}
	}

	/**
	 * Gzips and sends the level for the specified session.
	 * @param session The session.
	 */
	public void gzipWorld(final MinecraftSession session) {
		logger.debug("Gzipping world to {}", session);
		assert(session!=null);
		assert(session.getPlayer()!=null);
		assert(session.getPlayer().getWorld() != null);
		World level = session.getPlayer().getWorld();
		final int width = level.getWidth();
		final int height = level.getHeight();
		final int depth = level.getDepth();
		final byte[][][] blockData = (byte[][][])(session.getPlayer().getWorld().getBlocks().clone());
		session.getActionSender().sendWorldInit();
		/*service.submit(new Runnable() {
			public void run() {
				try {
					ChunkOutputStream clientMap = new ChunkOutputStream(session, width*height*depth);
					logger.trace("Gzipping world");
					DataOutputStream dataOut = new DataOutputStream(new GZIPOutputStream(clientMap));
					logger.trace("Writing size");
					dataOut.writeInt(width*height*depth);
					logger.trace("Writing blocks");
					for(int z = 0;z<depth;z++) {
						for (int y = 0;y<height;y++) {
							for(int x = 0;x<width;x++) {
								dataOut.write(blockData[x][y][z]);
							}
						}
					}
					logger.trace("Closing map output");
					clientMap.close();
				} catch (IOException ex) {
					session.getActionSender().sendLoginFailure("Failed to gzip level. Please try again.");
					logger.warn("GZip failed.", ex);
				}
			}
		});*/
		service.submit(new Runnable() {
			public void run() {
				try {
					//TODO: Parallelize the compression and transmission
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					int size = width * height * depth;
					logger.trace("Gzipping world");
					DataOutputStream os = new DataOutputStream(new GZIPOutputStream(out));
					os.writeInt(size);
					for (int z = 0; z < depth; z++) {
						for (int y = 0; y < height; y++) {
							for (int x = 0; x < width; x++) {
								os.write(blockData[x][y][z]);
							}
						}
					}
					os.close();
					logger.trace("Gzip complete. Transmitting to client");
					byte[] data = out.toByteArray();
					IoBuffer buf = IoBuffer.allocate(data.length);
					buf.put(data);
					buf.flip();
					while (buf.hasRemaining()) {
						int len = buf.remaining();
						if (len > 1024) {
							len = 1024;
						}
						byte[] chunk = new byte[len];
						buf.get(chunk);
						int percent = (int) ((double) buf.position() / (double) buf.limit() * 255D);
						session.getActionSender().sendWorldBlock(len, chunk, percent);
					}
					session.getActionSender().sendWorldFinish();
					logger.trace("World sent!");
				} catch (IOException ex) {
					session.getActionSender().sendLoginFailure("Failed to gzip level. Please try again.");
				}
			}
		});
	}
}
