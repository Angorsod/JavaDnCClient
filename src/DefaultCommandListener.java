
/**
 * @author Angor
 *
 */
public class DefaultCommandListener implements GameCommandListener
{

	private static final byte UPDATE_PLANETS = 0;
	private static final byte CURRENT_PLAYER = 1;
	private static final byte GET_PLAYER_INFO = 2;
	private static final byte LOGIN = 3;
	private static final byte GET_OWNER = 4;
	private static final byte GET_OWNERS = 5;

	private static byte currentAction = -1;
	private static int x = 500;
	private static int y = 500;
	private static int width = 15;
	private static int height = 15;

	private static DefaultCommandListener instance = null;
	public static DefaultCommandListener instance()
	{
		if (instance == null)
			instance = new DefaultCommandListener();
		return instance;
	}
	private DefaultCommandListener()
	{
		GameDataSink.addCommandListener(this);
	}

	public static void login()
	{
		currentAction = LOGIN;
		GameDataSink.performCommand(GameDataSink.GET_STATUS);
	}

	public static void setCurrentPlayer()
	{
		currentAction = CURRENT_PLAYER;
		GameDataSink.performCommand(GameDataSink.GET_CURRENT_PLAYER_ID);
	}

	public static void updatePlanetsProperties(int x, int y, int w, int h)
	{
		currentAction = UPDATE_PLANETS;
		DefaultCommandListener.x = x;
		DefaultCommandListener.y = y;
		width = w;
		height = h;
		GameDataSink.setLocation(x + 7, y + 7);
		GameDataSink.performCommand(GameDataSink.CENTER_ARMY_TAB);
	}

	public static void getOwner(int x, int y)
	{
		currentAction = GET_OWNER;
		GameDataSink.setLocation(x, y);
		GameDataSink.performCommand(GameDataSink.OPEN_PLANET_TAB);
	}

	private static Planet[] planets = null;
	private static int planetsIdx = 0;
	public static void getOwners(Planet[] planets)
	{
		DefaultCommandListener.planets = planets;
		planetsIdx = 0;
		if (DefaultCommandListener.planets == null || DefaultCommandListener.planets.length <= 0)
			return;
		currentAction = GET_OWNERS;
		GameDataSink.setLocation(DefaultCommandListener.planets[planetsIdx].x(), DefaultCommandListener.planets[planetsIdx].y());
		GameDataSink.performCommand(GameDataSink.OPEN_PLANET_TAB);
	}

	@Override public void currentUrl(String url)
	{
	}

	@Override public void planetOpened(int x, int y)
	{
		if (currentAction == GET_OWNER || currentAction == GET_OWNERS)
		{
			GameDataSink.performCommand(GameDataSink.GET_OWNER);
		}
	}

	@Override public void skymapLoaded(int x, int y)
	{
		if (currentAction == UPDATE_PLANETS)
		{
			try { Thread.sleep(500); } catch (Exception e) {}
			GameDataSink.performCommand(GameDataSink.GET_VISIBLE_KNOWN_PLANET);
		}
	}

	@Override public void skymapUpdated(int x, int y)
	{
		if (currentAction == UPDATE_PLANETS)
		{
			x += 15;
			if (x - DefaultCommandListener.x > width)
			{
				x = DefaultCommandListener.x + 7;
				y += 15;
				if (y - DefaultCommandListener.y > height)
				{
					currentAction = -1;
					return;
				}
			}
			GameDataSink.setLocation(x, y);
			GameDataSink.performCommand(GameDataSink.CENTER_ARMY_TAB);
		}
	}

	@Override public void currentPlayerId(int id)
	{
		if (currentAction == CURRENT_PLAYER)
		{
			Player p = Player.getPlayer(id);
			if (p != null)
				DnC.currentPlayer(p);
			else
			{
				currentAction = GET_PLAYER_INFO;
				GameDataSink.setId(id);
				GameDataSink.performCommand(GameDataSink.OPEN_PLAYER_INFO);
			}
		}
	}

	@Override public void playerOpened(int id)
	{
		if (currentAction == GET_PLAYER_INFO)
		{
			//System.out.println(id);
			GameDataSink.performCommand(GameDataSink.GET_PLAYER_INFO);
		}
	}

	@Override public void playerInfo(Player p)
	{
		DnC.currentPlayer(p);
		//System.out.println(id);
	}

	@Override public void raceInfo(Race r)
	{
		DnC.currentPlayer(DnC.currentPlayer());
	}

	@Override public void raceOpened(int id)
	{
		GameDataSink.performCommand(GameDataSink.GET_RACE_INFO);
	}

	@Override public void communicatorOpened()
	{
	}

	@Override public void currentTurn(int turn)
	{
	}

	@Override public void designerOpened()
	{
	}

	@Override public void homeWorld(int x, int y)
	{
	}

	@Override public void overviewOpened()
	{
	}

	@Override public void politicsOpened()
	{
	}

	@Override public void settingsOpened()
	{
	}

	@Override public void status(int stat)
	{
		Account.status(stat);
		if (stat == GameDataSink.LOGGED_OFF && currentAction == LOGIN)
		{
			GameDataSink.performCommand(GameDataSink.LOGIN);
		}
	}
	@Override public void entered()
	{
	}

	@Override public void ownerUpdated(int x, int y)
	{	
		if (currentAction != GET_OWNERS) return;
		planetsIdx++;
		if (planetsIdx >= planets.length)
		{
			currentAction = -1;
			planets = null;
			planetsIdx = 0;
			return;
		}
		GameDataSink.setLocation(planets[planetsIdx].x(), planets[planetsIdx].y());
		GameDataSink.performCommand(GameDataSink.OPEN_PLANET_TAB);
	}

}
