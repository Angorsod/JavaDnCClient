import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;

import SQLite.TableResult;


public class Race
{

	private static final String GET_RACE_QUERY = 
		"SELECT id, name, growth, speed, defense, prices, science, civil_speed, military_speed, strength, damage, mining, stealth, detect, d_t, temperature, " + 
		"nature, basic FROM Races WHERE id='%q';";
	private static final String UPDATE_RACE_QUERY = 
		"UPDATE Races SET name='%q',growth='%q',speed='%q',defense='%q',prices='%q',science='%q',civil_speed='%q',military_speed='%q',strength='%q',damage='%q'," + 
		"mining='%q',stealth='%q',detect='%q',d_t='%q',temperature='%q',nature='%q',basic='%q' WHERE id='%q';";
	private static final String INSERT_RACE_QUERY = 
		"INSERT INTO Races (name,growth,speed,defense,prices,science,civil_speed,military_speed,strength,damage,mining,stealth,detect,d_t,temperature,nature,basic,id) " + 
		"VALUES ('%q','%q','%q','%q','%q','%q','%q','%q','%q','%q','%q','%q','%q','%q','%q','%q','%q','%q');";
	private static final String DELETE_RACE_QUERY = 
		"DELETE FROM Races WHERE id='%q';";

	public static final byte ORGANICS = 0;
	public static final byte ENERGY = 1;
	public static final byte MINERALS = 2;

//	private static DataStorage races = null;

	public static Race getRace(int id)
	{
		Race r = new Race(-1);
		try
		{
			TableResult tr = DnC.db().get_table(GET_RACE_QUERY, 1, new String[] { Integer.toString(id) });
			String[] sa = (String[])tr.rows.get(0);
			r.id = Integer.parseInt(sa[0]);
			r.name = sa[1];
			r.growth = Float.parseFloat(sa[2]);
			r.speed = Float.parseFloat(sa[3]);
			r.defense = Float.parseFloat(sa[4]);
			r.prices = Float.parseFloat(sa[5]);
			r.science = Float.parseFloat(sa[6]);
			r.civilSpeed = Float.parseFloat(sa[7]);
			r.militarySpeed = Float.parseFloat(sa[8]);
			r.strength = Float.parseFloat(sa[9]);
			r.damage = Float.parseFloat(sa[10]);
			r.mining = Float.parseFloat(sa[11]);
			r.stealth = Float.parseFloat(sa[12]);
			r.detection = Float.parseFloat(sa[13]);
			r.dT = Float.parseFloat(sa[14]);
			r.temperature = Byte.parseByte(sa[15]);
			r.nature = Byte.parseByte(sa[16]);
			r.basic = Byte.parseByte(sa[17]);
		} catch (Exception e) { r.id = -1; }
		if (r.id <= 0) r = null;
		return r;
	}

	public boolean remove()
	{
		try
		{
			DnC.db().exec(DELETE_RACE_QUERY, null, new String[] { Integer.toString(id) });
		} catch (Exception e) { return false; }
		return true;
	}

	public boolean store()
	{
		try
		{
			boolean present = false;
			String query;
			TableResult tr = DnC.db().get_table("SELECT count(*) FROM Races WHERE id = '%q';", 1, new String[] { Integer.toString(id) });
			if (tr.nrows > 0 && tr.ncolumns > 0)
			{
				try { present = Integer.parseInt(((String[])(tr.rows.get(0)))[0]) != 0; } catch (Exception e) {}
			}
			query = (present) ? UPDATE_RACE_QUERY : INSERT_RACE_QUERY;
			String[] params = new String[]
			{
				name, 
				Float.toString(growth), 
				Float.toString(speed), 
				Float.toString(defense), 
				Float.toString(prices), 
				Float.toString(science), 
				Float.toString(civilSpeed), 
				Float.toString(militarySpeed), 
				Float.toString(strength), 
				Float.toString(damage), 
				Float.toString(mining), 
				Float.toString(stealth), 
				Float.toString(detection), 
				Float.toString(dT), 
				Byte.toString(temperature), 
				Byte.toString(nature), 
				Byte.toString(basic), 
				Integer.toString(id)
			};
			DnC.db().get_table(query, 0, params);
		} catch (Exception e) { return false; }
		return true;
/*
		if (races == null)
		{
			races = new DataStorage(DnC.getDnCDataDir() + "races.dat", Race.class);
		}
		return races.addData(this);
*/
	}

	public Integer getInt(Field f)
	{
		String s = f.getName();
		if (s.equals("id"))
			return new Integer(id);
		return null;
	}

	/*
	UTF string (race name)
	id (int) (4)
	growth (float) (4)
	speed (float) (4)
	defense (float) (4)
	prices (float) (4)
	science (float) (4)
	civil speed (float) (4)
	military speed (float) (4)
	strength (float) (4)
	damage (float) (4)
	mining (float) (4)
	stealth (float) (4)
	detect (float) (4)
	dT (float) (4)
	temperature (byte) (1)
	nature (byte) (1)
	basic (byte) (1) (59)
	name hash (int) (4) (63)
	*/
	public static Race deserialize(byte[] data, String name)
	{
		Race r = null;
		try
		{
			r = new Race(-1);
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bis);
			r.id = dis.readInt();
			r.growth = dis.readFloat();
			r.speed = dis.readFloat();
			r.defense = dis.readFloat();
			r.prices = dis.readFloat();
			r.science = dis.readFloat();
			r.civilSpeed = dis.readFloat();
			r.militarySpeed = dis.readFloat();
			r.strength = dis.readFloat();
			r.damage = dis.readFloat();
			r.mining = dis.readFloat();
			r.stealth = dis.readFloat();
			r.detection = dis.readFloat();
			r.dT = dis.readFloat();
			r.temperature = dis.readByte();
			r.nature = dis.readByte();
			r.basic = dis.readByte();
			//r.name = (String)DnC.strings().getDataByHash(dis.readInt());
			dis.readInt(); r.name = name;
			dis.close();
			bis.close();
		} catch (Exception e) {}
		return r;
	}
	public byte[] serialize()
	{
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);
			dos.writeInt(id);
			dos.writeFloat(growth);
			dos.writeFloat(speed);
			dos.writeFloat(defense);
			dos.writeFloat(prices);
			dos.writeFloat(science);
			dos.writeFloat(civilSpeed);
			dos.writeFloat(militarySpeed);
			dos.writeFloat(strength);
			dos.writeFloat(damage);
			dos.writeFloat(mining);
			dos.writeFloat(stealth);
			dos.writeFloat(detection);
			dos.writeFloat(dT);
			dos.writeByte(temperature);
			dos.writeByte(nature);
			dos.writeByte(basic);
			dos.writeInt(name.hashCode());
			byte[] rv = bos.toByteArray();
			dos.close();
			bos.close();
			return rv;
		} catch (Exception e) {}
		return null;
	}

	private Race(int id)
	{
		this.id = id;
	}

	private String name = "";
	public String name() { return name; }

	private int id = 1;
	public int id() { return id; }
	
	private float growth = -0.002F;
	public float growth() { return growth; }

	private float speed = 0.078F;
	public float speed() { return speed; }

	private float defense = -0.157F;
	public float defense() { return defense; }

	private float prices = -0.113F;
	public float prices() { return prices; }

	private float science = -0.15F;
	public float science() { return science; }

	private float civilSpeed = -0.024F;
	public float civilSpeed() { return civilSpeed; }

	private float militarySpeed = -0.049F;
	public float militarySpeed() { return militarySpeed; }

	private float strength = -0.112F;
	public float strength() { return strength; }

	private float damage = -0.203F;
	public float damage() { return damage; }

	private float mining = 0.048F;
	public float mining() { return mining; }

	private float stealth = 0.075F;
	public float stealth() { return stealth; }

	private float detection = -0.106F;
	public float detection() { return detection; }

	private byte temperature = 20;
	public int temperature() { return temperature; }

	private float dT = 14.7F;
	public float dT() { return dT; }
	
	public float natality(int temperature, int natureLvl)
	{
		int dT = Math.abs(temperature() - temperature);
		int divider = (dT > 2 * dT()) ? 2 : 1;
		return Math.min(1.0F, 2 - dT / dT()) * natureLvl * 0.05F * (1 + growth()) / divider;
	}

	private byte nature = ORGANICS;
	public byte nature() { return nature; }

	private byte basic = MINERALS;
	public byte basic() { return basic; }

	public byte extra()
	{
		byte extra = ORGANICS;
		while (extra == nature || extra == basic)
		{
			extra++;
		}
		return extra;
	}
	
}
