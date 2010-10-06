package org.hollowcraft.io;

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
 *ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.hollowcraft.model.Position;
import org.hollowcraft.model.Rotation;
import org.jnbt.*;
import java.io.*;

import org.slf4j.*;

/**
 * A frontend to the JNBT file reader
 * @author Adam Liszka
 * @author Caleb Champlin
 */
public final class AlphaNBTFileHandler {

	/**
	 * Default private constructor.
	 */
	private AlphaNBTFileHandler() { /* empty */ }

	private static final Logger logger = LoggerFactory.getLogger(AlphaNBTFileHandler.class);
	
	public static Map<String, Tag> load(String filename) throws IOException {
		NBTInputStream nbtin;

		logger.trace("Loading in {}", filename);
		nbtin = new NBTInputStream(new FileInputStream(filename));
		CompoundTag root = (CompoundTag)(nbtin.readTag());
		Map<String, Tag> items = root.getValue();
		nbtin.close();

		return items;
	}
	//
	//
	//
	// Saving a Level
	//
	//
	//

	public static void save(String filename) {

	}
}
