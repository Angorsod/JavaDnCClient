import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import SQLite.TableResult;


/**
 * @author Angor
 *
 */
public class Region
{
	private static final String GET_FLAGS_QUERY = 
		"SELECT x, y, name, reported_id, owner_id FROM Planets " + 
		"WHERE x >= '%q' AND x <= '%q' AND y >= '%q' AND y <= '%q';";

	private static Logger logger = Logger.getLogger(Region.class);

	public final static short galaxySpread = 2000;
	public final static short regionSpread = 200;
	private final static byte recordSize = 7;

	private static Region[] regions = new Region[5];
	private static byte[] regionIndices = { -1, -1, -1, -1, -1 };
	private static byte currentIndex = 0;
	private static boolean initialized = false;
	private static RandomAccessFile galaxyLayout = null;
	
	private Planet[] region;
	private byte regionIndex;
	public int regionIndex() { return regionIndex; }

	public boolean isBelongToRegion(int x, int y)
	{
		return getRegionIndex(x, y) == regionIndex;
	}

	public Planet[] getOccupied(int left, int top, int right, int bottom)
	{
		if (!isBelongToRegion(left, top)) return null;
		ArrayList<Planet> planets = new ArrayList<Planet>();
		right = Math.min(right, ((regionIndex % (galaxySpread / regionSpread)) * regionSpread) + regionSpread);
		bottom = Math.min(bottom, ((regionIndex / (galaxySpread / regionSpread)) * regionSpread) + regionSpread);
		for (int y = top; y <= bottom; y++)
		{
			int ry = y % regionSpread;
			for (int x = left; x <= right; x++)
			{
				int rx = x % regionSpread;
				Planet p = region[(ry - 1) * regionSpread + rx - 1];
				if (p.ownerId() >= 0)
					planets.add(p);
			}
		}
		return planets.toArray(new Planet[0]);
	}

	public static void dispose()
	{
		initialized = false;
		try { galaxyLayout.close(); } catch (Exception e) {}
		for (int i = 0; i < 5; i++)
		{
			regions[i] = null;
			regionIndices[i] = -1;
			currentIndex = 0;
		}
	}

	public static Region getRegion(int x, int y)
	{
		int idx = getRegionIndex(x, y); 
		if (idx < 0)
			return null;
		if (idx == regionIndices[0])
			return regions[0];
		if (idx == regionIndices[1])
			return regions[1];
		if (idx == regionIndices[2])
			return regions[2];
		if (idx == regionIndices[3])
			return regions[3];
		if (idx == regionIndices[4])
			return regions[4];
		if (regionIndices[currentIndex] >= 0)
		{
			currentIndex++;
			if (currentIndex > 4) currentIndex = 0;
		}
		Region r = new Region(idx);
		regions[currentIndex] = r;
		regionIndices[currentIndex] = (byte)idx;
		return r;
	}

	public boolean updatePlanet(Planet p, boolean updateExplored)
	{
		if (galaxyLayout == null) return false;
		if (!updateExplored)
		{
			Planet op = getPlanet(p.x(), p.y());
			if (op.isExplored()) return false;
		}
		try
		{
			galaxyLayout.seek(((p.x() - 1) + (p.y() - 1) * galaxySpread) * recordSize + 2);
			galaxyLayout.writeByte(p.organics());
			galaxyLayout.writeByte(p.energy());
			galaxyLayout.writeByte(p.minerals());
			galaxyLayout.writeByte(p.temperature());
			galaxyLayout.writeByte(p.surface());
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	public Planet getPlanet(int x, int y)
	{
		return region[(y - 1) % regionSpread * regionSpread + (x - 1) % regionSpread];
	}
	
	static
	{
		initGalaxy();
	}

	private Region(int regionIndex)
	{
		if (regionIndex < 0 || regionIndex > 99)
			return;

		this.regionIndex = (byte)regionIndex;

		logger.info(String.format("Create Region: %d", regionIndex));
		region = new Planet[regionSpread * regionSpread];
		if (galaxyLayout == null) return;
		try
		{
			logger.info("Load region");
			int right = ((regionIndex % (galaxySpread / regionSpread)) * regionSpread) + regionSpread + 1;
			int bottom = ((regionIndex / (galaxySpread / regionSpread)) * regionSpread) + regionSpread + 1;
			for (int top = bottom - regionSpread; top < bottom; top++)
			{
				int left = right - regionSpread;
				galaxyLayout.seek(((left - 1) + (top - 1) * galaxySpread) * recordSize);
				for (; left < right; left++)
				{
					byte[] planetData = new byte[7];
					galaxyLayout.read(planetData);
					Planet p = new Planet(left, top, planetData);
					region[(((top - 1) % regionSpread) * regionSpread) + ((left - 1) % regionSpread)] = p;
				}
			}
			TableResult tr = DnC.db().get_table(GET_FLAGS_QUERY, new String[] { 
				Integer.toString(right - regionSpread), Integer.toString(right),
				Integer.toString(bottom - regionSpread), Integer.toString(bottom)});
			int x = -1, y = -1;
			String[] ps = null;
			for (int i = 0; i < tr.nrows; i++)
			{
				try
				{
					ps = (String[])tr.rows.get(i);
					x = Integer.parseInt(ps[0]);
					y = Integer.parseInt(ps[1]);
					getPlanet(x, y).flags(ps[2], Integer.parseInt(ps[4]), Integer.parseInt(ps[3]), false);
				} catch (Exception e) { continue; }
			}
		}
		catch (Exception e)
		{
			logger.info(String.format("Error when loading region: %s", e.getMessage()));
			//System.out.println("Error when loading region");
			//System.out.println(e.getMessage());
			//e.printStackTrace();
		}
	}
	
	private static int getRegionIndex(int x, int y)
	{
		if (x < 1 || y < 1 || x > galaxySpread || y > galaxySpread)
			return -1;
		return ((x - 1) / regionSpread + ((y - 1) / regionSpread) * galaxySpread / regionSpread); 
	}

	public static void initGalaxy()
	{
		if (initialized)
			return;
		String galaxyFileName = DnC.getDnCHomeDir();

//		galaxyFileName = "C:" + File.separatorChar + "Toys" + File.separatorChar + "DnC" + 
//			File.separatorChar + "DnCBot";
//
//		if (galaxyFileName == null)
//			return;
		galaxyFileName = galaxyFileName + File.separatorChar + "Data" + File.separatorChar + "galaxy.dat";
		//galaxyFileName = galaxyFileName + "\\..\\DnCBot\\Data\\galaxy.dat";
		//System.out.println(galaxyFileName);
		logger.info(galaxyFileName);
		File galaxyFile = new File(galaxyFileName);
		if (galaxyFile.exists())
		{
			initialized = true;
			try
			{
				galaxyLayout = new RandomAccessFile(galaxyFile, "rw");
				//System.out.println("galaxyLayout opened");
				logger.info("galaxyLayout opened");
			}
			catch (Exception e)
			{
				//System.out.println("galaxyLayout not opened");
				logger.info("galaxyLayout not opened");
			}
			return;
		}
		int galaxySize = galaxySpread * galaxySpread;
		byte[] rec = { 0, 0, -101, -101, -101, -101, -101};
		FileOutputStream stream = null;
		try
		{
			stream = new FileOutputStream(galaxyFile);
			for (int i = 0; i < galaxySize; i++)
			{
				stream.write(rec);
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			try { if (stream != null) stream.close(); } catch (Exception e) {}
		}
		try { galaxyLayout = new RandomAccessFile(galaxyFile, "rw"); } catch (Exception e) {}
		initialized = true;
	}

}
