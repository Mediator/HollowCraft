package org.hollowcraft.server.heartbeat;
/*
 * HollowCraft License
 *  Copyright (c) 2010 Caleb Champlin.
 *  All rights reserved
 *  This license must be include in all copied, cloned and derived works 
 */
/*
 * OpenCraft License
 * 
 * Copyright (c) 2009 Graham Edgecombe, Søren Enevoldsen and Brett Russell.
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.*;

/**
 * A class which manages heartbeats.
 * @author Graham Edgecombe
 * @author Caleb Champlin
 */
public class FListHeartbeatManager {
	
	/**
	 * The singleton instance.
	 */
	private static final FListHeartbeatManager INSTANCE = new FListHeartbeatManager();
	
	/**
	 * Heartbeat server URL.
	 */
	public static final URL URL;
	
	/**
	 * Initializes the heartbeat server URL.
	 */
	static {
		try {
			URL = new URL("http://list.fragmer.net/announce.php");
		} catch (MalformedURLException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(FListHeartbeatManager.class);
	
	/**
	 * Gets the heartbeat manager instance.
	 * @return The heartbeat manager instance.
	 */
	public static FListHeartbeatManager getHeartbeatManager() {
		return INSTANCE;
	}
	
	/**
	 * An executor service which executes HTTP requests.
	 */
	private ExecutorService service = Executors.newSingleThreadExecutor();
	
	/**
	 * Default private constructor.
	 */
	private FListHeartbeatManager() {
		/* empty */
	}
	
	/**
	 * Sends a heartbeat with the specified parameters. This method does not
	 * block.
	 * @param parameters The parameters.
	 */

	public void sendHeartbeat(final Map<String, String> parameters) {
		logger.debug("Enqueuing heartbeat");
		
		service.submit(new Runnable() {
			public void run() {
				logger.debug("Sending flist heartbeat");
				// assemble POST data
				StringBuilder bldr = new StringBuilder();
				for (Map.Entry<String, String> entry : parameters.entrySet()) {
					logger.trace("Handling key {}", entry.getKey());
					bldr.append(entry.getKey());
					bldr.append('=');
					try {
						bldr.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
						logger.trace("Added {} to parameters", entry.getKey());
					} catch (UnsupportedEncodingException e) {
						logger.error("Bad parameters for flist heartbeat", e);
						return;
					}
					bldr.append('&');
				}
				if (bldr.length() > 0) {
					bldr.deleteCharAt(bldr.length() - 1);
				}
				logger.trace("Sending {} to flist", bldr);
				// send it off
				try {
					HttpURLConnection conn = (HttpURLConnection) URL.openConnection();
					byte[] bytes = bldr.toString().getBytes();
					conn.setDoOutput(true);
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
					conn.setUseCaches(false);
					conn.setDoInput(true);
					conn.setDoOutput(true);
					logger.trace("Connecting");
					conn.connect();
					try {
						DataOutputStream os = new DataOutputStream(conn.getOutputStream());
						try {
							os.write(bytes);
						} finally {
							os.close();
						}
					} finally {
						conn.disconnect();
					}
					logger.info("Pinged FList");
					logger.trace("Error code: {}", conn.getResponseCode());
				} catch (IOException ex) {
					logger.warn("Error sending hearbeat.", ex);
				}
			}
		});
	}
}
