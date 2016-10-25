import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

import SQLite.TableResult;


/**
 * @author Angor
 *
 */
public class Planet
{
	private static final String GET_FLAGS_QUERY = 
		"SELECT name, reported_id, owner_id FROM Planets WHERE x='%q' AND y='%q';";
	private static final String INSERT_FLAGS_QUERY = 
		"INSERT INTO Planets (x,y,name,reported_id,owner_id) VALUES ('%q','%q','%q','%q','%q');";
	private static final String UPDATE_FLAGS_QUERY = 
		"UPDATE Planets SET name = '%q', reported_id = '%q', owner_id = '%q' WHERE x='%q' AND y='%q';";
	private static final String DELETE_FLAGS_QUERY = 
		"DELETE FROM Planets WHERE x='%q' AND y='%q';";

	private boolean queryFlags(boolean update)
	{
		try
		{
			TableResult tr = DnC.db().get_table(GET_FLAGS_QUERY, 1, new String[] { Integer.toString(x), Integer.toString(y) });
			String[] ps = (String[])tr.rows.get(0);
			if (!update) return true;
			name = ps[0];
			visitedId = Integer.parseInt(ps[1]);
			owner = Integer.parseInt(ps[2]);
		} catch (Exception e) { return false; }
		return true;
	}
	public boolean addFlags()
	{
		if (queryFlags(false)) return false;
		if (name == null || getDefaultName().equals(name))
			name = "";
		try
		{
			DnC.db().get_table(INSERT_FLAGS_QUERY, 0, 
				new String[] { Integer.toString(x), Integer.toString(y), name, Integer.toString(visitedId), Integer.toString(owner) });
		} catch (Exception e) { return false; }
		return true;
	}
	public boolean removeFlags()
	{
		try
		{
			DnC.db().get_table(DELETE_FLAGS_QUERY, 0, new String[] { Integer.toString(x), Integer.toString(y) });
		} catch (Exception e) { return false; }
		return true;
	}
	public boolean updateFlags()
	{
		if (name == null || getDefaultName().equals(name))
			name = "";
		try
		{
			DnC.db().get_table(UPDATE_FLAGS_QUERY, 0, 
				new String[] { name, Integer.toString(visitedId), Integer.toString(owner), Integer.toString(x), Integer.toString(y) });
		} catch (Exception e) { return false; }
		return true;
	}

	public static final byte minScale = 10;
	public static final byte maxScale = 40;
	
	private static boolean showGeology = true;
	public static boolean showGeology() { return showGeology; }
	public static void showGeology(boolean show)
	{
		showGeology = show;
		try { DnC.mapPanel().repaintBackground(); } catch (Exception e) {}
	}
	private static boolean showNatality = true;
	public static boolean showNatality() { return showNatality; }
	public static void showNatality(boolean show)
	{
		showNatality = show;
		try { DnC.mapPanel().repaintBackground(); } catch (Exception e) {}
	}
	private static boolean showPlanetIcon = true;
	public static boolean showPlanetIcon() { return showPlanetIcon; }
	public static void showPlanetIcon(boolean show)
	{
		showPlanetIcon = show;
		try { DnC.mapPanel().repaintBackground(); } catch (Exception e) {}
	}

	private int visitedId = -1;
	private int owner = -1;
	private String name = "";
	public String name() { return (name != null && !name.isEmpty()) ? name : getDefaultName(); }
	public int ownerId() { return owner; }
	public void ownerId(int owner) { flags(this.name, owner, visitedId, true); }
	public int visitedId() { return visitedId; }
	public void flags(String name, int owner, int vid, boolean update)
	{
		boolean present = false;
		if (update) present = queryFlags(true);
		if (present && this.name == name && this.owner == owner && visitedId == vid)
			return;
		this.name = name;
		this.owner = owner;
		visitedId = vid;
		if (!update) return;
		if (present) updateFlags(); else addFlags();
	}

	private byte organics;
	private byte energy;
	private byte minerals;
	private byte temperature;
	private byte surface;

	private byte img_num;	// номер картинки планеты (0-44)
	private byte img_surf;	// номер размера планеты (0-9)

	private short x;
	private short y;
	public short x() { return x; }
	public short y() { return y; }

	public byte organics() { return organics; }
	public byte energy() { return energy; }
	public byte minerals() { return minerals; }
	public byte temperature() { return temperature; }
	public byte surface() { return surface; }

	public byte img_num() { return img_num; }
	public byte img_surf() { return img_surf; }

	public static void init()
	{
		initPlanetImages(maxScale);
//		Filter.addFilter(new Filter(Filter.GREATER_EQUAL, Filter.NATALITY, 0.5F));
//		Filter.addFilter(new Filter(Filter.GREATER_EQUAL, Filter.SURFACE, 80));
//		Filter.addFilter(new Filter(Filter.LESS, Filter.SURFACE, 20));
	}

	public Planet()
	{
	}

	public Planet(int x, int y, byte[] initData)
	{
		this.x = (short)x;
		this.y = (short)y;
		img_num = initData[0];
		img_surf = initData[1];
		organics = initData[2];
		energy = initData[3];
		minerals = initData[4];
		temperature = initData[5];
		surface = initData[6];
	}
	
	public void setProperties(byte[] data)
	{
		organics	= data[0];
		energy		= data[1];
		minerals	= data[2];
		temperature	= data[3];
		surface		= data[4];
	}

	public String getDefaultName()
	{
		return "N" + Short.toString(x) + ':' + Short.toString(y);
	}

	public boolean isExplored()
	{
		return (organics != -101);
	}

	public String getToolTipText()
	{
		StringBuffer ttt = new StringBuffer();
		String dname = getDefaultName();
		if (name != null && !dname.equals(name)) ttt.append(name);
		ttt.append(" (");
		ttt.append(dname);
		ttt.append(")    ");
		if (isExplored())
		{
			ttt.append("O:");
			ttt.append(organics);
			ttt.append("; E:");
			ttt.append(energy);
			ttt.append("; M:");
			ttt.append(minerals);
			ttt.append("; T:");
			ttt.append(temperature);
			ttt.append("; S:");
			ttt.append(surface);
/*
			// пригодность
			Race r = DnC.currentRace();
			if (r != null)//showNatality)
			{
				int nature = organics;
				if (r.nature() == Race.ENERGY)
					nature = energy;
				else if (r.nature() == Race.MINERALS)
					nature = minerals;
				float natality = r.natality(temperature(), nature);
				natality = Math.round(natality * 100.0F) / 100.0F;
				ttt.append("; Прирост:");
				ttt.append(natality);
			}
*/
			ttt.append("; Прирост:");
			ttt.append(natality());
		}
		if (owner > 0)
		{
			ttt.append(";  Владелец:");
			dname = Player.getPlayer(owner).name();
			if (dname.isEmpty())
				ttt.append(owner);
			else
				ttt.append(dname);
		}
		return ttt.toString();
	}

	public Image getPlanetImage()
	{
		if (basePlanets == null)
			init();
		if (basePlanets == null || x < 1 || y < 1 || x > Region.galaxySpread || y > Region.galaxySpread)
			return null;
		int imgId = img_num * 10 + img_surf;
		return (imgId >= 0 && imgId < planetImages.length) ? planetImages[imgId] : null;
	}

	public float natality()
	{
		Race r = DnC.currentRace();
		if (r != null)
		{
			int nature = organics;
			if (r.nature() == Race.ENERGY)
				nature = energy;
			else if (r.nature() == Race.MINERALS)
				nature = minerals;
			return r.natality(temperature(), nature);
		}
		return 0.0F;
	}

	public void paint(Graphics g, int x, int y, MapPanel map, boolean showGeo)
	{
		boolean fulfil = true;
		for (int i = Filter.filtersCount(); i > 0; i--)
		{
			if (!Filter.getFilter(i - 1).apply(this))
			{
				fulfil = false;
				break;
			}
		}

		if (Planet.showPlanetIcon)
		{
			Image img = getPlanetImage();
			if (img != null) g.drawImage(img, x, y, map);
		}
		if (isExplored())
		{
			int h = scale * 2 / 3;
			int xx = x + scale / 3;
			int yy = y + scale - 1;
			int bx = scale / 6;

			// пригодность
			if (Planet.showNatality)
			{
				Color color = Color.red;
				float natality = natality();
				if (natality > 2.75F)
					color = Color.green;
				else if (natality > 1.75F)
					color = Color.yellow;
				else if (natality > 0.75F)
					color = Color.orange;
				else if (natality > 0)
					color = Color.pink;
				g.setColor(color);
				//g.fillRect(x + 1, yy - 3, size / 3 - 1, 4);
				g.drawLine(x + 2, yy, x + scale / 3 - 2, yy);
			}

			if (showGeo || (Planet.showGeology && fulfil))
			{
				if (bx <= 1)
					bx = 2;
				int bw = bx - 3;
				if (bw <= 0)
					bw = 1;
				g.setColor(Color.white);
				g.drawLine(xx, yy, xx + scale * 2 / 3, yy);

				int per = h * organics / 100 + 2;
				yy = y + scale - per - 1;
				g.setColor(Color.green);
				g.fillRect(xx, yy, bw, per);
				if (bw > 2)
				{
					g.setColor(Color.white);
					g.drawRect(xx, yy, bw, per);
				}
	
				per = h * energy / 100 + 2;
				xx += bx;
				yy = y + scale - per - 1;
				g.setColor(Color.cyan);
				g.fillRect(xx, yy, bw, per);
				if (bw > 2)
				{
					g.setColor(Color.white);
					g.drawRect(xx, yy, bw, per);
				}
	
				per = h * minerals / 100 + 2;
				xx += bx;
				yy = y + scale - per - 1;
				g.setColor(Color.red);
				g.fillRect(xx, yy, bw, per);
				if (bw > 2)
				{
					g.setColor(Color.white);
					g.drawRect(xx, yy, bw, per);
				}
	
				per = h * temperature / 100 + 2;
				xx += bx;
				yy = y + scale - per - 1;
				g.setColor(Color.yellow);
				g.fillRect(xx, yy, bw, per);
				if (bw > 2)
				{
					g.setColor(Color.white);
					g.drawRect(xx, yy, bw, per);
				}
			}
		}
		
		if (owner != -1)
		{
			int s = scale / 3;
			int[] xPoints = { x + 1, x + 1, x + s };
			int[] yPoints = { y + s, y + 1, y + 1 };
			g.setColor(Color.lightGray);
			g.fillPolygon(xPoints, yPoints, 3);
		}

		if (!fulfil)
		{
			g.setColor(new Color(64, 64, 64, 172));
			g.fillRect(x, y, scale, scale);
		}
	}

	public static byte scale()
	{
		return scale;
	}

	public static void setScale(int newScale)
	{
		if (newScale < minScale || newScale > maxScale || newScale == scale)
			return;
		initPlanetImages(newScale);
//		MapPanelMenu.updateZoomSubmenu();
		//DnC.empire.setProperty("scale", Integer.toString(newScale));
	}


	private static byte scale = -1;
	public static Image basePlanets = null;
	private static Image[] planetImages = null;
	private static void initPlanetImages(int newScale)
	{
		if (basePlanets == null)
		{
			basePlanets = Toolkit.getDefaultToolkit().getImage("res/planets.gif");
			basePlanets.getWidth(imgObserver);
			while (!imgComplete) { try { Thread.sleep(10); } catch (Exception e) {} }
		}
		if (basePlanets == null || scale == newScale)
			return;
		if (planetImages == null)
			planetImages = new Image[450];
		scale = (byte)newScale;
		for (int i = 0; i < 10; i++)
		{
			int w = scale * 2 / 3;
			w = scale - (w * (9 - i) / 10);
			w *= 7;
			//Image img = basePlanets.getScaledInstance(w, w, Image.SCALE_SMOOTH);
			//imgComplete = false;
			//img.getWidth(imgObserver);
			//while (!imgComplete) { try { Thread.sleep(10); } catch (Exception e) {} }
			Image img = new BufferedImage(w, w, BufferedImage.TYPE_INT_ARGB);
			Graphics g = img.getGraphics();
			g.drawImage(basePlanets, 0, 0, w, w, imgObserver);

			for (int j = 0; j < 45; j++)
			{
				int index = j * 10 + i;
				planetImages[index] = new BufferedImage(scale, scale, BufferedImage.TYPE_INT_ARGB);
				g = planetImages[index].getGraphics();
				int iw = w;
				iw /= 7;
				int src = scale / 2 - iw / 2;
				int dx = j % 7 * iw;
				int dy = j / 7 * iw;
				//imgComplete = false;
				g.drawImage(img, src, src, src + iw, src + iw, 
					dx, dy, dx + iw, dy + iw, imgObserver);
				//while (!imgComplete) { try { Thread.sleep(10); } catch (Exception e) {} }
			}
		}
	}

	private static boolean imgComplete = false;
	private static ImageObserver imgObserver = new ImageObserver()
	{
		public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
		{
			if (infoflags != ALLBITS)
			{
				// Indicates image has not finished loading
				// Returning true will tell the image loading
				// thread to keep drawing until image fully
				// drawn loaded.
				imgComplete = false;
				return true;
			}
			else
			{
				imgComplete = true;
				return false;
			}
		}
	};

/*
	private static ArrayList<Filter> filters = new ArrayList<Filter>();
	public static void addFilter(Filter filter)
	{
		filters.add(filter);
		DnC.mapPanel().repaintBackground();
	}
*/

	public static class Filter
	{
		public static final int EQUAL = 0;
		public static final int NOT_EQUAL = 1;
		public static final int GREATER = 2;
		public static final int LESS = 3;
		public static final int GREATER_EQUAL = 4;
		public static final int LESS_EQUAL = 5;
		public static final int INCLUDE = 4;
		public static final int EXCLUDE = 5;

		public static final int INVALID = -1;
		public static final int ORGANICS = 10;
		public static final int ENERGY = 11;
		public static final int MINERALS = 12;
		public static final int TEMPERATURE = 13;
		public static final int SURFACE = 14;

		public static final int X = 20;
		public static final int Y = 21;

		public static final int OWNER = 30;
		public static final int DIPLOMACY = 31;
		public static final int STATE = 32;

		public static final int PLANET_NAME = 40;
		public static final int COMMENTARY = 41;
		public static final int MAIN_ICON = 42;
		public static final int EXTRA_ICON = 43;
		public static final int JUMPABLE = 44;

		public static final int NATALITY = 50;
		public static final int INCOME = 51;
		public static final int MAIN_MINING = 52;
		public static final int EXTRA_MINING = 53;
		public static final int MAIN_INCOME = 54;
		public static final int EXTRA_INCOME = 55;

		private int id;
		public int id() { return id; }

		private int condition = INVALID;
		public int condition() { return condition; }
		public void condition(int condition) { this.condition = condition; }

		private int parameter = INVALID;
		public int parameter() { return parameter; }
		public void parameter(int param) { parameter = param; }

		private int numValue = 0;
		public int intValue() { return numValue; }
		public void intValue(int value) { numValue = value; }

		private String stringValue = null;
		public String stringValue() { return stringValue; }
		public void stringValue(String value) { stringValue = value; }

		private float floatValue = 0.0F;
		public float floatValue() { return floatValue; }
		public void floatValue(float value) { floatValue = value; }

		private boolean enable = true;
		public boolean enable() { return enable; }
		public void enable(boolean enable) { 
			this.enable = enable; }

		public Filter(int condition, int parameter, int value)
		{
			this.condition = condition;
			this.parameter = parameter;
			this.numValue = value;
		}
		public Filter(int condition, int parameter, String value)
		{
			this.condition = condition;
			this.parameter = parameter;
			stringValue = value;
		}
		public Filter(int condition, int parameter, float value)
		{
			this.condition = condition;
			this.parameter = parameter;
			floatValue = value;
		}
		private Filter() {}

		public boolean apply(Planet planet)
		{
			if (!enable) return true;

			if (!planet.isExplored())
			{
				switch (parameter)
				{
					case ORGANICS:
					case ENERGY:
					case MINERALS:
					case TEMPERATURE:
					case SURFACE:
					case NATALITY:
						return false;
				}
			}
			switch (parameter)
			{
				case ORGANICS:
					switch (condition)
					{
						case EQUAL:			return planet.organics() == numValue;
						case NOT_EQUAL:		return planet.organics() != numValue;
						case GREATER:		return planet.organics() >  numValue;
						case LESS:			return planet.organics() <  numValue;
						case GREATER_EQUAL:	return planet.organics() >= numValue;
						case LESS_EQUAL:	return planet.organics() <= numValue;
						default: break;
					}
					break;
				case ENERGY:
					switch (condition)
					{
						case EQUAL:			return planet.energy() == numValue;
						case NOT_EQUAL:		return planet.energy() != numValue;
						case GREATER:		return planet.energy() >  numValue;
						case LESS:			return planet.energy() <  numValue;
						case GREATER_EQUAL:	return planet.energy() >= numValue;
						case LESS_EQUAL:	return planet.energy() <= numValue;
						default: break;
					}
					break;
				case MINERALS:
					switch (condition)
					{
						case EQUAL:			return planet.minerals() == numValue;
						case NOT_EQUAL:		return planet.minerals() != numValue;
						case GREATER:		return planet.minerals() >  numValue;
						case LESS:			return planet.minerals() <  numValue;
						case GREATER_EQUAL:	return planet.minerals() >= numValue;
						case LESS_EQUAL:	return planet.minerals() <= numValue;
						default: break;
					}
					break;
				case TEMPERATURE:
					switch (condition)
					{
						case EQUAL:			return planet.temperature() == numValue;
						case NOT_EQUAL:		return planet.temperature() != numValue;
						case GREATER:		return planet.temperature() >  numValue;
						case LESS:			return planet.temperature() <  numValue;
						case GREATER_EQUAL:	return planet.temperature() >= numValue;
						case LESS_EQUAL:	return planet.temperature() <= numValue;
						default: break;
					}
					break;
				case SURFACE:
					switch (condition)
					{
						case EQUAL:			return planet.surface() == numValue;
						case NOT_EQUAL:		return planet.surface() != numValue;
						case GREATER:		return planet.surface() >  numValue;
						case LESS:			return planet.surface() <  numValue;
						case GREATER_EQUAL:	return planet.surface() >= numValue;
						case LESS_EQUAL:	return planet.surface() <= numValue;
						default: break;
					}
					break;
				case X:
					switch (condition)
					{
						case EQUAL:			return planet.x() == numValue;
						case NOT_EQUAL:		return planet.x() != numValue;
						case GREATER:		return planet.x() >  numValue;
						case LESS:			return planet.x() <  numValue;
						case GREATER_EQUAL:	return planet.x() >= numValue;
						case LESS_EQUAL:	return planet.x() <= numValue;
						default: break;
					}
					break;
				case Y:
					switch (condition)
					{
						case EQUAL:			return planet.y() == numValue;
						case NOT_EQUAL:		return planet.y() != numValue;
						case GREATER:		return planet.y() >  numValue;
						case LESS:			return planet.y() <  numValue;
						case GREATER_EQUAL:	return planet.y() >= numValue;
						case LESS_EQUAL:	return planet.y() <= numValue;
						default: break;
					}
					break;
				case NATALITY:
					switch (condition)
					{
						case EQUAL:			return planet.natality() == floatValue;
						case NOT_EQUAL:		return planet.natality() != floatValue;
						case GREATER:		return planet.natality() >  floatValue;
						case LESS:			return planet.natality() <  floatValue;
						case GREATER_EQUAL:	return planet.natality() >= floatValue;
						case LESS_EQUAL:	return planet.natality() <= floatValue;
						default: break;
					}
					break;
				default:
					return true;
			}
			return false;
		}

		private static final String GET_FILTERS_QUERY = 
			"SELECT id, parameter, condition, value, text, options FROM Filters;";
		private static final String ADD_FILTER_QUERY = 
			"INSERT INTO Filters (parameter, condition, value, text, options) VALUES ('%q', '%q', '%q', '%q', '%q');";
		private static final String UPDATE_FILTER_QUERY = 
			"UPDATE Filters SET parameter = '%q', condition = '%q', value = '%q', text = '%q', options = '%q' WHERE id = '%q';";
		private static final String DELETE_FILTER_QUERY = 
			"DELETE FROM Filters WHERE id = '%q';";
		private static int initFilters()
		{
			if (filters == null) filters = new ArrayList<Filter>();
			filters.clear();
			try
			{
				TableResult tr = DnC.db().get_table(GET_FILTERS_QUERY);
				String[] fs = null;
				for (int i = 0; i < tr.nrows; i++)
				{
					try
					{
						fs = (String[])tr.rows.get(i);
						Filter filter = new Filter();
						filter.id = Integer.parseInt(fs[0]);
						filter.parameter = Integer.parseInt(fs[1]);
						filter.condition = Integer.parseInt(fs[2]);
						filter.stringValue = fs[4];
						try { filter.numValue = Integer.parseInt(fs[3]); }
						catch (Exception e) { try { filter.floatValue = Float.parseFloat(fs[3]); } catch (Exception e1) {} }
						filter.enable = !fs[5].equals("0");
						filters.add(filter);
					} catch (Exception e) { continue; }
				}
			} catch (Exception e) { System.out.println(e.getMessage()); }
			return filters.size();
		}

		private static ArrayList<Filter> filters = null;
		public static int filtersCount()
		{
			int cnt = 0;
			if (filters == null) initFilters();
			cnt = filters.size();
			return cnt;
		}
		public static Filter getFilter(int index)
		{
			if (filters == null)
			{
				initFilters();
				return null;
			}
			if (index < 0 || index >= filters.size()) return null;
			return filters.get(index);
		}
		public static boolean removeFilter(Filter filter)
		{
			boolean result = true;
			if (filters == null)
			{
				initFilters();
				result = false;
				return result;
			}
			try
			{
				result = filters.remove(filter);
				DnC.db().get_table(DELETE_FILTER_QUERY, 0, new String[] { Integer.toString(filter.id()) });
			} catch (Exception e) { result = false; }
			return result;
		}
		public static Filter addFilter(Filter filter)
		{
			if (filters == null)
			{
				initFilters();
			}
			if (filters.contains(filter)) return filter;
			try
			{
				DnC.db().get_table(ADD_FILTER_QUERY, 0, new String[] { 
					Integer.toString(filter.parameter()), 
					Integer.toString(filter.condition()), 
					(filter.intValue() != 0 ? Integer.toString(filter.intValue()) : Float.toString(filter.floatValue())), 
					filter.stringValue(), (filter.enable() ? "1" : "0") });
				filter.id = (int)DnC.db().last_insert_rowid();
			} catch (Exception e) { filter.id = -1; }
			if (filter.id() < 0)
				return null;
			filters.add(filter);
			return filter;
		}
		public static Filter updateFilter(Filter filter)
		{
			if (filters == null)
			{
				initFilters();
				return null;
			}
			if (!filters.contains(filter)) return null;
			try
			{
				DnC.db().get_table(UPDATE_FILTER_QUERY, 0, new String[] { 
					Integer.toString(filter.parameter()), 
					Integer.toString(filter.condition()), 
					(filter.intValue() != 0 ? Integer.toString(filter.intValue()) : Float.toString(filter.floatValue())), 
					filter.stringValue(), (filter.enable() ? "1" : "0"), 
					Integer.toString(filter.id()) });
			} catch (Exception e) { return null; }
			return filter;
		}
	}
	
}
