import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
//import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;


public class MapPanelMenu implements ActionListener
{

	private static JPopupMenu menu = null;
	
	public static JPopupMenu getPopupMenu()
	{
		if (menu == null)
			new MapPanelMenu();
		return menu;
	}

	public static void updateZoomSubmenu()
	{
		zoomInMenu.setEnabled((Planet.scale() >= 40) ? false : true);
		zoomOutMenu.setEnabled((Planet.scale() <= 10) ? false : true);
	}

	public void actionPerformed(ActionEvent e)
	{
		JMenuItem source = (JMenuItem)(e.getSource());
		if (source == zoomInMenu)
		{
			DnC.mapPanel().zoomIn();
		}
		else if (source == zoomOutMenu)
		{
			DnC.mapPanel().zoomOut();
		}
		else if (source == centerOn)
		{
			DnC.mapPanel().center();
		}
		else if (source == login)
		{
			DefaultCommandListener.login();
		}
		else if (source == extractCurrPlayer)
		{
			DefaultCommandListener.setCurrentPlayer();
//			GameDataSink.performCommand(GameDataSink.GET_RACE_INFO);
		}
		else if (source == extractRace)
		{
			//int id = DnC.currentRace().id();
			GameDataSink.setId(DnC.currentPlayer().raceId());
			GameDataSink.performCommand(GameDataSink.OPEN_RACE_INFO);
		}
		else if (source == extractPlayerInfo)
		{
			//int id = DnC.currentRace().id();
			GameDataSink.setId(DnC.currentPlayer().id());
			GameDataSink.performCommand(GameDataSink.OPEN_PLAYER_INFO);
		}
		else if (source == extractVisibleGeo)
		{
//			GameDataSink.setLocation(DnC.mapPanel().getCenterX(), DnC.mapPanel().getCenterY());
//			GameDataSink.performCommand(GameDataSink.GET_VISIBLE_KNOWN_PLANET);
			MapPanel mp = DnC.mapPanel();
			DefaultCommandListener.updatePlanetsProperties(
				mp.getOriginX(), mp.getOriginY(), mp.getVisibleWidth(), mp.getVisibleWidth());
//			int x = DnC.mapPanel().getOriginX();
//			int y = DnC.mapPanel().getOriginY();
//			DnC.empire().setProperty("extractVisibleGeoX", Integer.toString(x));
//			DnC.empire().setProperty("extractVisibleGeoY", Integer.toString(y));
//			DnC.centerGameSkyMap(x, y);
		}
		else if (source == extractOwners)
		{
			MapPanel mp = DnC.mapPanel();
			int x = mp.getOriginX();
			int y = mp.getOriginY();
			Region r = Region.getRegion(x, y);
			Planet[] p = r.getOccupied(x, y, x + mp.getVisibleWidth(), y + mp.getVisibleHeight());
			if (p != null && p.length > 0)
			{
				DefaultCommandListener.getOwners(p);
			}
			//GameDataSink.performCommand(GameDataSink.GET_OWNER);
		}
		else if (source == accounts)
		{
			Account.openAccountsDialog();
		}
/*		else if (source == showGeo || source == showNatality || source == showFake)
		{
			Planet.showGeo = showGeo.getState();
			Planet.showNatality = showNatality.getState();
			Planet.showFake = showFake.getState();
			repaintLayout();
			repaint();
		}
		else if (source == helpMenu)
        {
			Applet app = DnC.empire;
			URL url = app.getCodeBase();
			try
			{
				url = new URL(url.toString() + "help.html");
			}
			catch (Exception exc)
			{
			}
			app.getAppletContext().showDocument(url, "_blank");
		}
*/
	}

	private static JMenuItem zoomInMenu;
	private static JMenuItem zoomOutMenu;
	private static JMenuItem centerOn;
//	private static JCheckBoxMenuItem showGeo;
//	private static JCheckBoxMenuItem showFake;
//	private static JCheckBoxMenuItem showNatality;
	private static JMenuItem extractVisibleGeo;
	private static JMenuItem extractOwners;
	private static JMenuItem extractCurrPlayer;
	private static JMenuItem extractPlayerInfo;
	private static JMenuItem extractRace;
	private static JMenuItem login;
	private static JMenuItem accounts;

	private MapPanelMenu()
	{
		menu = new JPopupMenu();

		JMenu submenu = new JMenu("Map");
		menu.add(submenu);
		zoomInMenu = new JMenuItem("Zoom In");
		zoomInMenu.addActionListener(this);
		zoomInMenu.setEnabled((Planet.scale() >= 40) ? false : true);
		submenu.add(zoomInMenu);
		zoomOutMenu = new JMenuItem("Zoom Out");
		zoomOutMenu.addActionListener(this);
		zoomOutMenu.setEnabled((Planet.scale() <= 10) ? false : true);
		submenu.add(zoomOutMenu);
		submenu.addSeparator();
		menu.add(submenu);
		centerOn = new JMenuItem("Center on ...", KeyEvent.VK_C);
		centerOn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
		centerOn.addActionListener(this);
		submenu.add(centerOn);
		menu.addSeparator();

/*		submenu = new JMenu("Filter");
		menu.add(submenu);
		showGeo = new JCheckBoxMenuItem("Show Geodata");
		//showGeo.setState(Planet.showGeo);
		showGeo.addActionListener(this);
		submenu.add(showGeo);
		showFake = new JCheckBoxMenuItem("Show Fake");
		//showFake.setState(Planet.showFake);
		showFake.addActionListener(this);
		submenu.add(showFake);
		showNatality = new JCheckBoxMenuItem("Show Natality");
		//showNatality.setState(Planet.showNatality);
		showNatality.addActionListener(this);
		submenu.add(showNatality);
		menu.addSeparator();
*/		
		submenu = new JMenu("Extract");
		menu.add(submenu);
		login = new JMenuItem("Login");
		login.addActionListener(this);
		submenu.add(login);
		submenu.addSeparator();
		extractVisibleGeo = new JMenuItem("Visible Geo");
		extractVisibleGeo.addActionListener(this);
		submenu.add(extractVisibleGeo);
		extractOwners = new JMenuItem("Owners");
		extractOwners.addActionListener(this);
		submenu.add(extractOwners);
		extractCurrPlayer = new JMenuItem("Current Player");
		extractCurrPlayer.addActionListener(this);
		submenu.add(extractCurrPlayer);
		extractPlayerInfo = new JMenuItem("Player Info");
		extractPlayerInfo.addActionListener(this);
		submenu.add(extractPlayerInfo);
		extractRace = new JMenuItem("Race Param");
		extractRace.addActionListener(this);
		submenu.add(extractRace);
		menu.addSeparator();

		accounts = new JMenuItem("Accounts...");
		accounts.addActionListener(this);
		menu.add(accounts);
	}

}
