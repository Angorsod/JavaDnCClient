import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.log4j.Logger;


/**
 * @author Angor
 *
 */
public class GameDataSink
{

	private static Logger logger = Logger.getLogger(GameDataSink.class);

	public static final int INVALID_HOST = -1;
	public static final int LOGGED_OFF = 2;
	public static final int ENTERED = 0;
	public static final int RESTRICTED = 1; // logged off

	private static final byte CHECK_CONNECTION = 0;
	public static final byte CENTER_ARMY_TAB = 1;
	public static final byte OPEN_PLANET_TAB = 2;
	public static final byte GET_CURRENT_URL = 3;
	public static final byte GET_VISIBLE_KNOWN_PLANET = 4;
	public static final byte GET_CURRENT_PLAYER_ID = 5;
	public static final byte OPEN_PLAYER_INFO = 6;
	public static final byte GET_PLAYER_INFO = 7;
	public static final byte OPEN_RACE_INFO = 8;
	public static final byte GET_RACE_INFO = 9;
	public static final byte OPEN_OVERVIEW_TAB = 10;
	public static final byte OPEN_COMMUNICATOR_TAB = 11;
	public static final byte OPEN_DESIGNER_TAB = 12;
	public static final byte OPEN_SETTINGS_TAB = 13;
	public static final byte OPEN_POLITICS_TAB = 14;
	public static final byte GET_CURRENT_TURN = 15;
	public static final byte GET_HOME = 16;
	public static final byte GET_STATUS = 17;
	public static final byte OPEN_START_PAGE = 18;
	public static final byte LOGIN = 19;
	public static final byte LOGOFF = 20;
	public static final byte GET_OWNER = 21;

	private static ArrayList<GameCommandListener> commandListeners = new ArrayList<GameCommandListener>();
	public static void addCommandListener(GameCommandListener l)
	{
		if (l != null)
			commandListeners.add(l);
	}
	public static void removeCommandListener(GameCommandListener l)
	{
		if (l != null)
			commandListeners.remove(l);
	}

	/*
	 * & - код символа ASCII 0x26
	 * D&C - 0xD26C = 53868 
	 */
	private static final int EXTRACTOR_PORT = 0xD26C;

	private static GameDataSink instance = null;
	public static GameDataSink instance()
	{
		if (instance == null)
		{
			instance = new GameDataSink();
		}
		return instance;
	}

	public static void performCommand(byte command)
	{
		while (currentCommand != CHECK_CONNECTION) { try { Thread.sleep(10); } catch (Exception e) {} }
		currentCommand = command;
	}

	private static int id = 1;
	public static void setId(int id)
	{
		GameDataSink.id = id;
	}

	private static Point location = new Point(500, 500);
	public static void setLocation(int x, int y)
	{
		location.x = x;
		location.y = y;
	}

	private static volatile byte currentCommand = CHECK_CONNECTION;
	private static volatile byte prevCommand = CHECK_CONNECTION;
	private static void gameDataCommandLoop()
	{
		logger.info("Inside gameDataCommandLoop()");
		int i;
		switch (prevCommand)
		{
			case CENTER_ARMY_TAB:
				for (i = 0; i < commandListeners.size(); i++)
				{
					commandListeners.get(i).skymapLoaded(location.x, location.y);
				}
				break;
			case OPEN_PLANET_TAB:
				for (i = 0; i < commandListeners.size(); i++)
				{
					commandListeners.get(i).planetOpened(location.x, location.y);
				}
				break;
			case OPEN_PLAYER_INFO:
				for (i = 0; i < commandListeners.size(); i++)
				{
					commandListeners.get(i).playerOpened(id);
				}
				break;
			case OPEN_RACE_INFO:
				for (i = 0; i < commandListeners.size(); i++)
				{
					commandListeners.get(i).raceOpened(id);
				}
				break;
			case LOGIN:
				for (i = 0; i < commandListeners.size(); i++)
				{
					commandListeners.get(i).entered();
				}
				break;
			default:
				break;
		}
		prevCommand = CHECK_CONNECTION;
		Thread thisThread = Thread.currentThread();
		DataLoop: for (i = 0; thisThread == dataSinkThread; i++)
		{
			if (gameDataInput != null && gameDataOutput != null)
			{
				switch (currentCommand)
				{
					case CHECK_CONNECTION:
						if (i % 100 == 0)
						{
							try
							{
								gameDataOutput.write(currentCommand);
							}
							catch (Exception e)
							{
								logger.info(e.getMessage());
								break DataLoop;
							}
						}
						break;
					case GET_CURRENT_URL:
						getCurrentUrl();
						break;
					case CENTER_ARMY_TAB:
					case OPEN_PLANET_TAB:
						openPlanetTab();
						break DataLoop;
					case GET_VISIBLE_KNOWN_PLANET:
						getKnownPlanets();
						break;
					case GET_CURRENT_PLAYER_ID:
						getCurrentPlayerId();
						break;
					case GET_PLAYER_INFO:
						getPlayerInfo();
						break;
					case OPEN_PLAYER_INFO:
					case OPEN_RACE_INFO:
						openPlayerInfo();
						break DataLoop;
					case GET_RACE_INFO:
						getRaceInfo();
						break;
					case GET_STATUS:
						getStatus();
						break;
					case OPEN_OVERVIEW_TAB:
					case OPEN_COMMUNICATOR_TAB:
					case OPEN_DESIGNER_TAB:
					case OPEN_SETTINGS_TAB:
					case OPEN_POLITICS_TAB:
					case OPEN_START_PAGE:
					case LOGOFF:
						openNonparamTab();
						break DataLoop;
					case LOGIN:
						login();
						break DataLoop;
					case GET_OWNER:
						getOwner();
						break;
					default:
						break;
				}
			}
			try { Thread.sleep(10); } catch (Exception e) {}
		}
		logger.info("Exit form gameDataCommandLoop()");
	}

	private static void openNonparamTab()
	{
		try
		{
			prevCommand = currentCommand;
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	private static void login()
	{
		Account acc = Account.getCurrent();
		if (acc == null)
		{
			try { acc = Account.accounts()[0]; } catch (Exception e) {}
			if (acc == null) return;
		}
		try
		{
			prevCommand = currentCommand;
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			gameDataOutput.writeUTF(acc.login());
			gameDataOutput.writeUTF(acc.passwd());
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	private static void getRaceInfo()
	{
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
		try
		{
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			String name = gameDataInput.readUTF();
/*			if (DnC.strings().getDataByHash(name.hashCode()) == null)
			{
				DnC.strings().addData(name);
			}
*/			byte[] rdata = new byte[63];
			gameDataInput.readFully(rdata);
			Race r = Race.deserialize(rdata, name);
			r.store();
			for (int i = 0; i < commandListeners.size(); i++)
			{
				commandListeners.get(i).raceInfo(r);
			}
		} catch (Exception e) {}
	}

	private static void getPlayerInfo()
	{
		/*
		UTF string (player name)
		id (int) (4)
		name hash (int) (4)
		race id (int) (4)
		governors (byte) (1)
		population (int) (4)
		vassals (byte) (1)
		level (byte) (1)
		rating (int) (4) (23)
		*/
		try
		{
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			String name = gameDataInput.readUTF();
/*			if (DnC.strings().getDataByHash(name.hashCode()) == null)
			{
				DnC.strings().addData(name);
			}
*/			byte[] pdata = new byte[23];
			gameDataInput.readFully(pdata);
			Player p = Player.deserialize(pdata);
			//p.store();
			p.name(name);
			for (int i = 0; i < commandListeners.size(); i++)
			{
				commandListeners.get(i).playerInfo(p);
			}
		} catch (Exception e) {}
	}

	private static void getCurrentPlayerId()
	{
		try
		{
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			String s = gameDataInput.readUTF();
			int id = Integer.valueOf(s);
			for (int i = 0; i < commandListeners.size(); i++)
			{
				commandListeners.get(i).currentPlayerId(id);
			}
			//logger.info(s);
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	private static void getStatus()
	{
		try
		{
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			int stat = gameDataInput.readInt();
			for (int i = 0; i < commandListeners.size(); i++)
			{
				commandListeners.get(i).status(stat);
			}
			//logger.info(s);
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	private static void getCurrentUrl()
	{
		try
		{
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			String s = gameDataInput.readUTF();
			for (int i = 0; i < commandListeners.size(); i++)
			{
				commandListeners.get(i).currentUrl(s);
			}
			//logger.info(s);
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	private static void openPlayerInfo()
	{
		try
		{
			prevCommand = currentCommand;
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			gameDataOutput.writeInt(id);
			//logger.info(s);
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	private static void openPlanetTab()
	{
		try
		{
			prevCommand = currentCommand;
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			gameDataOutput.writeShort(location.x);
			gameDataOutput.writeShort(location.y);
			//logger.info(s);
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	private static void getOwner()
	{
		try
		{
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			int x = gameDataInput.readShort();
			if (x == 0) return;
			int y = gameDataInput.readShort();
			int id = gameDataInput.readInt();
			String name = "";
			try { name = gameDataInput.readUTF(); } catch (Exception e) {}
			if (!name.isEmpty()) gameDataInput.readShort();
			if (id == 0 && DnC.currentPlayer() != null)
			{
				id = DnC.currentPlayer().id();
				name = DnC.currentPlayer().name();
			}
			Region r = Region.getRegion(x, y);
			Planet p = r.getPlanet(x, y);
			p.ownerId(id);
			if (id > 0)
			{
				Player pl = Player.getPlayer(id);
				if (pl != null)
				{
					if (!name.equals(pl.name()))
						pl.name(name);
				}
				else
				{
					pl = new Player(id);
					pl.name(name);
				}
			}
			for (int i = 0; i < commandListeners.size(); i++)
			{
				commandListeners.get(i).ownerUpdated(x, y);
			}
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	private static void getKnownPlanets()
	{
		try
		{
			gameDataOutput.write(currentCommand);
			currentCommand = CHECK_CONNECTION;
			byte[] props = new byte[5];
			String name = "";
			while (true)
			{
				int x = gameDataInput.readShort();
				if (x == 0) break;
				int y = gameDataInput.readShort();
				props[0] = gameDataInput.readByte(); // o
				props[1] = gameDataInput.readByte(); // e
				props[2] = gameDataInput.readByte(); // m
				props[3] = gameDataInput.readByte(); // t
				props[4] = gameDataInput.readByte(); // s
				byte flags = gameDataInput.readByte(); // flags
				try { name = gameDataInput.readUTF(); } catch (Exception e) { name = ""; }
				//if (props[0] == -101 || props[1] == -101 || props[2] == -101 || props[3] == -101 || props[4] == -101)
				//	continue;
				Region r = Region.getRegion(x, y);
				Planet p = r.getPlanet(x, y);
				if (flags != 0)
				{
					p.flags(name, ((flags & 6) != 0) ? 0 : -1, ((flags & 1) != 0) ? 0 : -1, true);
				}
				if (!p.isExplored())
				{
					p.setProperties(props);
					r.updatePlanet(p, true);
				}
			}
			DnC.mapPanel().repaintBackground();
			for (int i = 0; i < commandListeners.size(); i++)
			{
				commandListeners.get(i).skymapUpdated(location.x, location.y);
			}
		}
		catch (Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	private GameDataSink()
	{
		if (connection != null)
		{
			try { connection.close(); } catch (Exception e) {}
			connection = null;
		}
		if (listener != null)
		{
			try { listener.close(); } catch (Exception e) {}
			listener = null;
		}
		if (dataSinkThread != null)
			dataSinkThread = null;
		while (listener != null) { try { Thread.sleep(10); } catch (Exception e) {} }
		dataSinkThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						listener = new ServerSocket(EXTRACTOR_PORT);
						Thread thisThread = Thread.currentThread();
						while (thisThread == dataSinkThread && listener != null)
						{
							logger.info("Wait inbound connection from game data provider.");
							connection = listener.accept();
							logger.info("Connection from game data provider accepted.");
							gameDataInput = new DataInputStream(connection.getInputStream());
							gameDataOutput = new DataOutputStream(connection.getOutputStream());
							gameDataCommandLoop();
							connection.close();
							connection = null;
						}
					} catch (Exception e) {}
					if (connection != null)
					{
						try { connection.close(); } catch (Exception e) {}
						connection = null;
					}
					if (listener != null)
					{
						try { listener.close(); } catch (Exception e) {}
						listener = null;
					}
					instance = null;
					dataSinkThread = null;
				}
			});
		dataSinkThread.start();
	}

	private static volatile Thread dataSinkThread = null;
	private static ServerSocket listener = null;
	private static volatile Socket connection = null;
	private static DataInputStream gameDataInput = null;
	private static DataOutputStream gameDataOutput = null;

}
