import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import com.vlsolutions.swing.docking.DockableContainerFactory;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingContext;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.RelativeDockablePosition;
import com.vlsolutions.swing.docking.event.DockableStateChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateChangeListener;
import SQLite.Database;


/**
 * @author Angor
 *
 */
//@SuppressWarnings("serial")
public class DnC implements WindowListener, ComponentListener
{

	private static DockingDesktop desk = new DockingDesktop();

	private static Logger logger = Logger.getLogger(DnC.class);

	private static DnC empire = null;
	public static DnC empire() { return empire; }
	private DnC()
	{
	}

	private static Database database;// = new Database();
	public static Database db() { return database; }
    private static String _DnCHomeDir = "";
    public static String getDnCHomeDir() { return _DnCHomeDir; }
    public static String getDnCDataDir()
    {
    	//String DnCHomeDir = "C:" + File.separatorChar + "Toys" + File.separatorChar + "DnC" + 
		//	File.separatorChar + "DnCBot";
		// C:\Toys\DnC\DnCBot\Data\
    	//return DnCHomeDir + File.separatorChar + "Data" + File.separatorChar;
    	return _DnCHomeDir + File.separatorChar + "Data" + File.separatorChar;
    }

    private static JFrame mainFrame = null;
    public static JFrame mainFrame() { return mainFrame; }
	private static MapPanel mapPanel = null;
	public static MapPanel mapPanel() { return mapPanel; }

	private static FleetsPanel fleetsPanel = new FleetsPanel();
	private static PlanetsPanel planetsPanel = new PlanetsPanel();
	private static MapPropertiesPanel mapPropertiesPanel = null;

	private static GameDataSink gameData = null;
	public static GameDataSink gameData() { return gameData; }
	private static GameCommandListener gameListener = null;
	public static GameCommandListener gameListener() { return gameListener; }
    private static DataStorage strings = null; 
    public static DataStorage strings() { return strings; }
    
    private static Race currentRace = null;
    public static Race currentRace() { return currentRace; }
    private static Player currentPlayer = null;
    public static Player currentPlayer() { return currentPlayer; }
    public static void currentPlayer(Player p)
    {
    	currentPlayer = p;
    	if (p == null) return;
    	empire.setProperty("currentPlayer", Integer.toString(p.id()));
		mainFrame.setTitle(" Divide & Conquer - " + p.name());
		currentRace = Race.getRace(p.raceId());
		mapPanel.repaintBackground();
	}

    private static Properties mainProps = null;
    private void initMainProps()
    {
		if (mainProps != null) return;
		mainProps = new Properties();
		try
		{
			FileInputStream is = new FileInputStream(getDnCDataDir() + "DnC.props");
			mainProps.load(is);
			is.close();
		} catch (Exception e) {}
	}

    public String getProperty(String key)
    {
		if (mainProps == null) return "";
		return mainProps.getProperty(key, "");
	}

    private void storeProperties()
    {
		try
		{
			FileOutputStream os = new FileOutputStream(getDnCDataDir() + "DnC.props");
			mainProps.store(os, "");
			os.close();
		} catch (Exception e) {}
	}

    public void setProperty(String key, String prop)
    {
		if (mainProps == null) return;
		mainProps.setProperty(key, prop);
		storeProperties();
	}

    public void removeProperty(String key)
    {
		if (mainProps == null) return;
		mainProps.remove(key);
		storeProperties();
	}

	private void createAndShowGUI()
	{
		UIManager.put("TabbedDockableContainer.tabPlacement", SwingConstants.BOTTOM);
		desk.addDockableStateChangeListener(new DockableStateChangeListener()
			{
				public void dockableStateChanged(DockableStateChangeEvent event)
				{
					DockableState newState = event.getNewState();
					if (newState.isClosed())
					{
						desk.unregisterDockable(newState.getDockable());
						mapPanel.requestFocus();
					}
					else if (newState.isDocked())
					{
//						desk.setDockableWidth(newState.getDockable(), 0.15);
					}
				}
			});

		//Create and set up the window.
		mainFrame = new JFrame(" Divide & Conquer");
		mainFrame.addWindowListener(this);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setExtendedState(Frame.MAXIMIZED_BOTH);

		//Set the frame icon to an image loaded from a file.
		mainFrame.setIconImage(new ImageIcon("res/App.gif").getImage());
		mainFrame.setMenuBar(null);

		//Set the menu bar and add the label to the content pane.
		//frame.setJMenuBar(greenMenuBar);
		//frame.getContentPane().add(yellowLabel, BorderLayout.CENTER);

		//Display the window.
		mainFrame.pack();
		mainFrame.setVisible(true);
		//mainFrame.setContentPane(mapPanel);
		mainFrame.setContentPane(desk);
		mapPanel = new MapPanel();
		desk.addDockable(mapPanel);
		mapPanel.requestFocus();

		mapPropertiesPanel = new MapPropertiesPanel();

		initDockables();

		mapPropertiesPanel.loadCheckBoxes();
		//mapPanel.repaintBackground();
	}

	private boolean loadDocables()
	{
		BufferedInputStream in = null;
		try
		{
			in = new BufferedInputStream(new FileInputStream(getDnCDataDir() + "docking.props"));
		} catch (Exception e) { return false; }
		try
		{
			desk.registerDockable(mapPropertiesPanel);
			desk.registerDockable(planetsPanel);
			desk.registerDockable(fleetsPanel);
			desk.readXML(in);
			in.close();
		}
		catch (Exception e)
		{
			desk.unregisterDockable(fleetsPanel);
			desk.unregisterDockable(planetsPanel);
			desk.unregisterDockable(mapPropertiesPanel);
		}
		return true;
	}

	private void initDockables()
	{
		if (loadDocables()) return;

		desk.addHiddenDockable(mapPropertiesPanel, RelativeDockablePosition.RIGHT);
		desk.addHiddenDockable(planetsPanel, RelativeDockablePosition.RIGHT);
		desk.addHiddenDockable(fleetsPanel, RelativeDockablePosition.RIGHT);
		DockingContext context = desk.getContext();
		DockableState state = context.getDockableState(mapPropertiesPanel);
		RelativeDockablePosition pos = new RelativeDockablePosition(0.845, 0.5, 0.155, 0.5);
		context.setDockableState(mapPropertiesPanel, new DockableState(state, pos));
		state = context.getDockableState(planetsPanel);
		context.setDockableState(planetsPanel, new DockableState(state, pos));
		state = context.getDockableState(fleetsPanel);
		context.setDockableState(fleetsPanel, new DockableState(state, pos));

		//desk.split(mapPanel, fleetsPanel, DockingConstants.SPLIT_RIGHT);
		//desk.setDockableWidth(fleetsPanel, 0.15);
		//desk.setDockableHeight(fleetsPanel, 0.5);
		//desk.split(fleetsPanel, planetsPanel, DockingConstants.SPLIT_BOTTOM);
		//desk.createTab(fleetsPanel, planetsPanel, 1);
		//desk.setDockableWidth(planetsPanel, 0.15);
		//desk.setDockableHeight(planetsPanel, 0.5);
		//desk.setAutoHide(fleetsPanel, true);
		//desk.setAutoHide(planetsPanel, true);
		//desk.split(mapPanel, fleetsPanel, DockingConstants.SPLIT_RIGHT);
		//desk.createTab(fleetsPanel, planetsPanel, 1);
		//desk.setAutoHide(fleetsPanel, true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.config");
		logger.info("DnC application started");

		//System.out.println(System.getProperty("java.library.path"));
		//System.setProperty("java.library.path", "C:\\Toys\\DnC\\DnC\\lib;" + System.getProperty("java.library.path"));
		//System.out.println(System.getProperty("java.library.path"));
		//System.out.println();
		
		database = new Database();

		empire = new DnC();

		// /home/angor/stuck/toys/DnC
		_DnCHomeDir = File.separatorChar + "home" + File.separatorChar + "angor" + File.separatorChar + "shared" + File.separatorChar + "Java" + 
				File.separatorChar + "stuck" + File.separatorChar + "toys" + File.separatorChar + "DnC";
		// _DnCHomeDir = "C:" + File.separatorChar + "Toys" + File.separatorChar + "DnC" + 
		//	File.separatorChar + "DnC";
		// C:\Toys\DnC\DnCBot\Data\

		empire.initMainProps();

		try { database.open(getDnCDataDir() + "DnC.db", 0);
		//try { database.open(_DnCHomeDir + "\\..\\DnCBot\\Data\\" + "DnC.db", 0);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		Region.initGalaxy();
		Planet.init();

		gameData = GameDataSink.instance();
		gameListener = DefaultCommandListener.instance();

		//strings = new DataStorage(getDnCDataDir() + "strings.dat", String.class);

		DockableContainerFactory.setFactory(new MapDockableFactory());
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				empire.createAndShowGUI();
			}
		});
	}

	// WindowListener interface methods
	@Override
	public void windowActivated(WindowEvent e)
	{
	}
	@Override
	public void windowClosed(WindowEvent e)
	{
	}
	@Override
	public void windowClosing(WindowEvent e)
	{
		setProperty("scale", Integer.toString(Planet.scale()));
		setProperty("centerX", Integer.toString(mapPanel.getCenterX()));
		setProperty("centerY", Integer.toString(mapPanel.getCenterY()));

		try
		{
		    BufferedOutputStream out = new BufferedOutputStream(
		    	new FileOutputStream(getDnCDataDir() + "docking.props"));
		    desk.writeXML(out);
		    out.close(); // stream isn't closed in case you'd like to save something else after
		} catch (Exception ioe) {}

		mapPropertiesPanel.saveCheckBoxes();

		//strings.dispose();
		Region.dispose();
		//Planet.dispose();
	}
	@Override public void windowDeactivated(WindowEvent e)
	{
	}
	@Override public void windowDeiconified(WindowEvent e)
	{
		if (mapPanel != null)
			mapPanel.repaintBackground();
	}
	@Override public void windowIconified(WindowEvent e)
	{
		if (mapPanel != null)
			mapPanel.repaintBackground();
	}
	@Override public void windowOpened(WindowEvent we)
	{
		if (mapPanel != null)
		{
			String s = getProperty("scale");
			try { Planet.setScale(Integer.parseInt(s)); } catch (Exception e) {}
			s = getProperty("currentPlayer");
			try { currentPlayer(Player.getPlayer(Integer.parseInt(s))); } catch (Exception e) {}
			try
			{
				s = getProperty("centerX");
				int x = Integer.parseInt(s);
				s = getProperty("centerY");
				int y = Integer.parseInt(s);
				mapPanel.select(x, y);
			} catch (Exception e) {}
			mapPanel.center(-1, -1);
		}
	}

	// ComponentListener interface methods
	@Override public void componentHidden(ComponentEvent e)
	{
	}
	@Override public void componentMoved(ComponentEvent e)
	{
	}
	@Override public void componentResized(ComponentEvent e)
	{
		if (mapPanel != null)
			mapPanel.repaintBackground();
	}
	@Override public void componentShown(ComponentEvent e)
	{
	}

}
