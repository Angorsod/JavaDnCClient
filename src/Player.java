import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;

import SQLite.TableResult;

//import java.util.ArrayList;

public class Player
{
	
	private static final String GET_PLAYER_QUERY = 
		"SELECT id, name, race_id, governors, population, vassals, level, rating, lord_id FROM Players WHERE id='%q';";
	private static final String UPDATE_PLAYER_QUERY = 
		"UPDATE Players SET name='%q',race_id='%q',governors='%q',population='%q',vassals='%q',level='%q',rating='%q' WHERE id='%q';";
	private static final String INSERT_PLAYER_QUERY = 
		"INSERT INTO Players (name,race_id,governors,population,vassals,level,rating,id) VALUES ('%q','%q','%q','%q','%q','%q','%q','%q');";
	private static final String DELETE_PLAYER_QUERY = 
		"DELETE FROM Players WHERE id='%q';";

//	private static DataStorage players = null;

	public static Player getPlayer(int id)
	{
		Player p = new Player(-1);
		try
		{
			TableResult tr = DnC.db().get_table(GET_PLAYER_QUERY, 1, new String[] { Integer.toString(id) });
			String[] ps = (String[])tr.rows.get(0);
			p.id = Integer.parseInt(ps[0]);
			p.name = ps[1];
			p.raceId = Integer.parseInt(ps[2]);
			p.governors = Byte.parseByte(ps[3]);
			p.population = Integer.parseInt(ps[4]);
			p.vassals = Byte.parseByte(ps[5]);
			p.level = Byte.parseByte(ps[6]);
			p.rating = Integer.parseInt(ps[7]);
		} catch (Exception e) { p.id = -1; }
		if (p.id <= 0) p = null;
		return p;
	}

	public boolean remove()
	{
		try
		{
			DnC.db().exec(DELETE_PLAYER_QUERY, null, new String[] { Integer.toString(id) });
		} catch (Exception e) { return false; }
		return true;
	}

	public boolean store()
	{
		try
		{
			boolean present = false;
			String query;
			TableResult tr = DnC.db().get_table("SELECT count(*) FROM Players WHERE id = '%q';", 1, new String[] { Integer.toString(id) });
			if (tr.nrows > 0 && tr.ncolumns > 0)
			{
				try { present = Integer.parseInt(((String[])(tr.rows.get(0)))[0]) != 0; } catch (Exception e) {}
			}
			query = (present) ? UPDATE_PLAYER_QUERY : INSERT_PLAYER_QUERY;
			String[] params = new String[]
			{
				name, 
				Integer.toString(raceId), 
				Integer.toString(governors), 
				Integer.toString(population), 
				Integer.toString(vassals), 
				Integer.toString(level), 
				Integer.toString(rating), 
				Integer.toString(id)
			};
			DnC.db().get_table(query, 0, params);
		} catch (Exception e) { return false; }
		return true;
	}

	public Integer getInt(Field f)
	{
		String s = f.getName();
		if (s.equals("id"))
			return new Integer(id);
		else if (s.equals("name"))
			return new Integer(name.hashCode());
		return null;
	}

	public static Player deserialize(byte[] data)
	{
		Player p = null;
		try
		{
			p = new Player(-1);
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bis);
			p.id = dis.readInt();
			//p.name = (String)DnC.strings().getDataByHash(dis.readInt());
			dis.readInt();
			p.raceId = dis.readInt();
			p.governors = dis.readByte();
			p.population = dis.readInt();
			p.vassals = dis.readByte();
			p.level = dis.readByte();
			p.rating = dis.readInt();
		} catch (Exception e) {}
		return p;
	}
	public byte[] serialize()
	{
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(id);
			dos.writeInt(name.hashCode());
			dos.writeInt(raceId);
			dos.write(governors);
			dos.writeInt(population);
			dos.write(vassals);
			dos.write(level);
			dos.writeInt(rating);
			byte[] rv = bos.toByteArray();
			dos.close();
			bos.close();
			return rv;
		} catch (Exception e) {}
		return null;
	}

	private int id = -1;
	public int id() { return id; }

	private String name = "";
	public String name() { return name; }
	public void name(String name) { this.name = name; store(); }

	private int raceId = -1;
	public int raceId() { return raceId; }
//	private Race race = DnC.empire.race();
//	public Race race() { return race; }

	private byte governors = 0;
	public int governors() { return governors; }

	private int population = 0;
	public int population() { return population; }

	private byte vassals = 0;
	public int vassals() { return vassals; }

	private byte level = 0;
	public int level() { return level; }

	private int rating = 0;
	public int rating() { return rating; }
	
//	private ArrayList planets = new ArrayList();
//	public Planet[] planets() { return (Planet[])planets.toArray(); }

	public Player(int id)
	{
		this.id = id;
	}

}
