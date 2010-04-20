package org.opencraft.server.io;

import org.opencraft.server.Configuration;
import org.opencraft.server.model.Position;
import org.opencraft.server.model.Rotation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Level loading and saving code.
 * Maintains compatibility with map editors and other server softwares.
 * @author Brett Russell <br />
 * <i>With code from www.minecraftwiki.net and Minecraft (www.minecraft.net), authored by Markus Persson (AKA Notch).</i>
 */
public class SerializableLevel implements Serializable {

	/**
	 * The serial version UID for this object.
	 */
	private static final long serialVersionUID = 0L;
	
	/**
	 * The width of the map. This was public.
	 */
	private int width;
	
	/**
	 * The height of the map. This was public.
	 */
	private int height;
	
	/**
	 * The depth of the map. This was public.
	 */
	private int depth;
	
	/**
	 * The blocks in the map (stored as an array of bytes) This was public.
	 */
	private byte[] blocks;
	
	/**
	 * The name of the map. This was public.
	 */
	private String name;
	
	/**
	 * The name of the map's creator. This was public.
	 */
	private String creator;
	
	/**
	 * The date of the map's creation. This was public.
	 */
	private long createTime;
	
	/**
	 * The X spawn coordinate. This was public.
	 */
	private int xSpawn;
	
	/**
	 * The Y spawn coordinate. This was public.
	 */
	private int ySpawn;
	
	/**
	 * The Z spawn coordinate. This was public.
	 */
	private int zSpawn;
	
	/**
	 * The player's spawning rotation. This was public.
	 */
	private float rotSpawn;
	
	/**
	 * Kept for consistency. Unused. This was private.
	 */
	private boolean networkMode = false;
	
	/**
	 * Kept for consistency. Unused. This was public.
	 */
	private boolean creativeMode;
	
	/**
	 * Kept for consistency. Unused. This was public.
	 */
	private int waterLevel, skyColor, fogColor, cloudColor;
	
	/**
	 * Kept for consistency. Unused. This was public.
	 */
	int unprocessed = 0;
	
	/**
	 * Kept for consistency. Unused. This was public.
	 */
	private int tickCount = 0;
	
	/**
	 * The map's filename.
	 */
	private transient String filename;
	
	/**
	 * The logger instance.
	 */
	private transient Logger logger = Logger.getLogger(SerializableLevel.class.getName());
	
	/**
	 * Did load() successfully load a map?
	 */
	private transient boolean loadSuccess = false;

	/**
	 * Constructor.
	 */
	public SerializableLevel(String filename) {
		this.filename = filename;
	}

	/**
	 * Did we load the level successfully?
	 * @return Whether we loaded the map successfully.
	 */
	public boolean isLoadSuccess() {
		return loadSuccess;
	}
	
	/**
	 * Return the spawn coordinates as a Position.
	 * @return
	 */
	public Position getSpawnPoint() {
		return new Position(xSpawn, ySpawn, zSpawn);
	}
	
	/**
	 * Update the spawn coordinates.
	 * @param point The new spawnpoint.
	 */
	public void updateSpawnPoint(Position point) {
		this.xSpawn = point.getX();
		this.ySpawn = point.getY();
		this.zSpawn = point.getZ();
	}
	
	/**
	 * Get the spawn rotation as a Rotation.
	 * @return The spawn rotation.
	 */
	public Rotation getSpawnRotation() {
		return new Rotation((int) rotSpawn, 0);
	}
	
	/**
	 * Update the spawn rotation.
	 * @param rotation The new spawn rotation.
	 */
	public void updateSpawnRotation(Rotation rotation) {
		this.rotSpawn = (float) rotation.getRotation();
	}
	
	/**
	 * Gets all of the blocks.
	 * @return All of the blocks.
	 */
	public byte[] getBlocks() {
		return blocks;
	}

	/**
	 * Gets the width of the level.
	 * @return The width of the level.
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Gets the height of the level.
	 * @return The height of the level.
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Gets the depth of the level.
	 * @return The depth of the level.
	 */
	public int getDepth() {
		return depth;
	}

	public String getName() {
		return name;
	}

	public String getCreator() {
		return creator;
	}

	public long getCreateTime() {
		return createTime;
	}

	/**
  	 * Match this object to a locally created copy made when loading a map.
  	 */
  	private void setData(int width, int depth, int height, byte[] blocks, 
  			int xSpawn, int ySpawn, int zSpawn, float rotSpawn, 
  			String name, String creator, long createTime, boolean networkMode, boolean creativeMode, 
  			int waterLevel, int skyColor, int fogColor, int cloudColor, int unprocessed, int tickCount) {
  		this.width = width;
  		this.depth = depth;
  		this.height = height;
  		this.blocks = blocks;
  		this.xSpawn = xSpawn;
  		this.ySpawn = ySpawn;
  		this.zSpawn = zSpawn;
  		this.rotSpawn = rotSpawn;
  		this.name = name;
  		this.creator = creator;
  		this.createTime = createTime;
  		this.networkMode = networkMode;
  		this.creativeMode = creativeMode;
  		this.waterLevel = waterLevel;
  		this.skyColor = skyColor;
  		this.fogColor = fogColor;
  		this.cloudColor = cloudColor;
  		this.unprocessed = unprocessed;
  		this.tickCount = tickCount;
  		System.gc();
  	}
  	
	/**
  	 * Match this object to a locally created copy made when loading a map.
  	 */
  	public void updateSerializableLevel(int width, int depth, int height, byte[] blocks, 
  			int xSpawn, int ySpawn, int zSpawn, float rotSpawn) {
  		this.width = width;
  		this.depth = depth;
  		this.height = height;
  		this.blocks = blocks;
  		this.xSpawn = xSpawn;
  		this.ySpawn = ySpawn;
  		this.zSpawn = zSpawn;
  		this.rotSpawn = rotSpawn;
  		this.save();
  	}

  	/**
  	 * Load a map.
  	 * @throws ClassNotFoundException 
  	 * @throws IOException 
  	 */
  	public void load() {
  		logger.info("Loading map file...");
  		try {
  			FileInputStream fis = new FileInputStream(this.filename);
  			GZIPInputStream gzis = new GZIPInputStream(fis);
  			DataInputStream dis = new DataInputStream(gzis);
  			if (dis.readInt() != 656127880) {
  				logger.log(Level.WARNING, "Map file is invalid or corrupt.");
  	  			dis.close();
  	  			return;
  			}
  			if (dis.readByte() != 2) {
  				logger.log(Level.WARNING, "Map file is not of version 2. Cannot be loaded.");
  	  			dis.close();
  	  			return;
  			}
  			ObjectInputStream ois = new ObjectInputStream(gzis);
System.out.println("2");
  			SerializableLevel localLevel = (SerializableLevel)ois.readObject();
System.out.println("3");
  			dis.close();
System.out.println("4");
  			ois.close();
System.out.println("5");
  			setData(localLevel.width, localLevel.depth, localLevel.height, localLevel.blocks, 
  					localLevel.xSpawn, localLevel.ySpawn, localLevel.zSpawn, localLevel.rotSpawn, 
  					localLevel.name, localLevel.creator, localLevel.createTime, localLevel.networkMode, 
  					localLevel.creativeMode, localLevel.waterLevel, localLevel.skyColor, localLevel.fogColor,
  					localLevel.cloudColor, localLevel.unprocessed, localLevel.tickCount);
System.out.println("6");
  			this.loadSuccess = true;
  		} catch(FileNotFoundException e) {
  			logger.info("Could not load map file. File not found.");
  			return;
  		} catch (IOException e) {
  			logger.info("Could not load map file. IO Exception");
  			return;
		} catch (ClassNotFoundException e) {
  			logger.info("Could not load map file.");
  			return;
		}
  	}

	// save in file called filename
	public void save() {
		FileOutputStream fos = null;
		GZIPOutputStream gzos = null;
		ObjectOutputStream out = null;
		DataOutputStream outputstream = null;
		try {
			fos = new FileOutputStream("./data/" + this.filename);
			gzos = new GZIPOutputStream(fos);
			outputstream = new DataOutputStream(gzos);
			outputstream.writeInt(0x271bb788);
			outputstream.writeByte(2);
			out = new ObjectOutputStream(gzos);
			out.writeObject(this);
			outputstream.close();
			out.close();
			logger.info("Successfully saved " + this.filename + ".");
		} catch(IOException e) {
			logger.log(Level.WARNING, "Could not save map.");
		}
	}
}