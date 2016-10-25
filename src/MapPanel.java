import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;

/**
 * @author Angor
 *
 */
public class MapPanel extends JPanel implements ComponentListener, KeyListener,
	MouseMotionListener, MouseListener, MouseWheelListener, Dockable
{

	private static final long serialVersionUID = -4524578359316111200L;

	private Image mapPanelImage = null;
	private Graphics mapPanelGraphics;
	private Font rulerFont;
	
	private Point origin;
	private Point selected;
	
	private byte hRulerHeight = 18;
	private byte vRulerWidth = 25;
	private boolean showGrid = false;
	private JPopupMenu popMenu = null;

	private CoordDialog coordDialog;
	private DockKey dockKey = new DockKey("mapPanel");


	public MapPanel()
	{
		origin = new Point(480, 490);
		selected = new Point(500, 500);

		setFocusable(true);
		addKeyListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		setBackground(Color.black);
		setForeground(Color.white);

		initCursors();
		//Planet.init();
		setToolTipText("Map Panel");
		
		popMenu = MapPanelMenu.getPopupMenu();
		popMenu.setVisible(false);
		add(popMenu);
        // Add listener to the text area so the popup menu can come up.
		MouseListener popupListener = new PopupListener(popMenu);
		addMouseListener(popupListener);

		addComponentListener(this);

		coordDialog = new CoordDialog(DnC.mainFrame(), "", selected.x, selected.y);
		//coordDialog.pack();

		dockKey.setFloatEnabled(false);
		dockKey.setCloseEnabled(false);
		dockKey.setAutoHideEnabled(false);
		dockKey.setMaximizeEnabled(false);
		dockKey.setName(null);
		dockKey.setResizeWeight(0.85F);
		
		//DockViewTitleBar tb = getTitleBar();
		//tb.setSize(0, 0);
		//System.out.println(isFocusable());
	}

	@Override public Component getComponent()
	{
		return this;
	}

	@Override public DockKey getDockKey()
	{
		return dockKey;
	}

	public void select(int x, int y)
	{
		selected.x = x;
		selected.y = y;
		//repaintBackground();
	}

	public int getVisibleWidth()
	{
		return (getWidth() - vRulerWidth) / Planet.scale();
	}

	public int getVisibleHeight()
	{
		return (getHeight() - hRulerHeight) / Planet.scale();
	}

	public int getOriginX()
	{
		return origin.x;
	}

	public int getOriginY()
	{
		return origin.y;
	}

	public int getCenterX()
	{
		return origin.x + getVisibleWidth() / 2;
	}

	public int getCenterY()
	{
		return origin.y + getVisibleHeight() / 2;
	}

	private Planet toolTipPlanet = null;
	private PlanetToolTip planetToolTip = null;
	public String getToolTipText(MouseEvent event)
	{
		Planet p = planetFromComponent(event.getX(), event.getY());
		if (p != null)
		{
			if (toolTipPlanet != null && toolTipPlanet != p)
				repaintPlanet(toolTipPlanet, false);
			toolTipPlanet = p;
			repaintPlanet(p, true);
			if (planetToolTip == null) createToolTip();
			planetToolTip.setPlanet(p);
			return ""; // p.getToolTipText();
		}
		return null;
	}
	public Point getToolTipLocation(MouseEvent event)
	{
		byte scale = Planet.scale();
		int x = (event.getX() - vRulerWidth) / scale;
		int y = (event.getY() - hRulerHeight) / scale + 1;
		x = x * scale + vRulerWidth + scale / 2;
		y = y * scale + hRulerHeight + 3;
		return new Point(x, y);
	}
	public JToolTip createToolTip()
	{
		if (planetToolTip == null)
		{
			planetToolTip = new PlanetToolTip();
			planetToolTip.setComponent(this);
		}
		return planetToolTip;
	}
	private Planet planetFromComponent(int cx, int cy)
	{
		byte scale = Planet.scale();
		int x = (cx - vRulerWidth) / scale + origin.x;
		int y = (cy - hRulerHeight) / scale + origin.y;
		if (x > Region.galaxySpread) x -= Region.galaxySpread;
		if (y > Region.galaxySpread) y -= Region.galaxySpread;
		Region r = Region.getRegion(x, y);
		return (r != null) ? r.getPlanet(x, y) : null;
	}

	public boolean isPlanetVisible(int x, int y)
	{
		return (x >= origin.x && y >= origin.y && x <= origin.x + getVisibleWidth() && y <= origin.y + getVisibleHeight());
	}

	synchronized public void repaintPlanet(int x, int y, boolean showGeology)
	{
		if (!isPlanetVisible(x, y)) return;
		Region r = Region.getRegion(x, y);
		Planet p = (r != null) ? r.getPlanet(x, y) : null;
		if (p != null) repaintPlanet(p, showGeology);
	}
	synchronized public void repaintPlanet(Planet planet, boolean showGeology)
	{
		if (!isPlanetVisible(planet.x(), planet.y())) return;
		if (mapPanelImage == null)
		{
			repaintBackground();
			return;
		}
		int scale = Planet.scale();
		int xx = (planet.x() - origin.x) * scale + vRulerWidth;
		int yy = (planet.y() - origin.y) * scale + hRulerHeight;
		mapPanelGraphics.setColor(Color.black);
		mapPanelGraphics.fillRect(xx, yy, scale, scale);
		planet.paint(mapPanelGraphics, xx, yy, this, showGeology);
		repaint();
	}

	synchronized public void repaintBackground()
	{
 		int w = getWidth();
		int h = getHeight();
		if (w <= 0 && h <= 0) return;
		
		if (mapPanelImage == null)
		{
			mapPanelImage = createImage(w, h);
			mapPanelGraphics = mapPanelImage.getGraphics();
			rulerFont = mapPanelGraphics.getFont().deriveFont(10.0F);
		}
		mapPanelGraphics.setColor(Color.black);
		mapPanelGraphics.fillRect(0, 0, w, h);
		
		//mapPanelGraphics.setColor(Color.white);
		//mapPanelGraphics.fillRect(0, 0, w, h);
		//if (true) return;

		paintRulers();

		byte scale = Planet.scale();
		w = (w - vRulerWidth) / scale;
		h = (h - hRulerHeight) / scale;
		Region r = null;
		for (int j = 0; j < h; j++)
		{
			for (int i = 0; i < w; i++)
			{
				int x = origin.x + i, y = origin.y + j;
				if (x > Region.galaxySpread) x -= Region.galaxySpread;
				if (y > Region.galaxySpread) y -= Region.galaxySpread;
				r = Region.getRegion(x, y);
				Planet p = (r != null) ? r.getPlanet(x, y) : null;
				x = vRulerWidth + i * scale;
				y = hRulerHeight + j * scale;
				if (p != null) p.paint(mapPanelGraphics, x, y, this, false);
			}
		}
		repaint();
	}

	public boolean zoomOut()
	{
		int scale = Planet.scale();
		if (scale <= 10)
			return false;
		int w = getWidth() - vRulerWidth;
		int h = getHeight() - hRulerHeight;
		int cx = origin.x + w / scale / 2;
		int cy = origin.y + h / scale / 2;
		w = w / (w / scale + 2);
		if (w < 10)
			w = 10;
		Planet.setScale(w);
		origin.x = cx - (getWidth() - vRulerWidth) / w / 2;
		origin.y = cy - (getHeight() - hRulerHeight) / w / 2;
		if (origin.x < 1) origin.x = 1;
		if (origin.y < 1) origin.y = 1;
		repaintBackground();
		MapPanelMenu.updateZoomSubmenu();
		return true;
	}
	public boolean zoomIn()
	{
		int scale = Planet.scale();
		if (scale >= 40)
			return false;
		int w = getWidth() - vRulerWidth;
		int h = getHeight() - hRulerHeight;
		int cx = origin.x + w / scale / 2;
		int cy = origin.y + h / scale / 2;
		w = w / (w / scale - 2);
		if (w == scale)
			w++;
		if (w > 40)
			w = 40;
		Planet.setScale(w);
		origin.x = cx - (getWidth() - vRulerWidth) / w / 2;
		origin.y = cy - (getHeight() - hRulerHeight) / w / 2;
		if (origin.x < 1) origin.x = 1;
		if (origin.y < 1) origin.y = 1;
		repaintBackground();
		MapPanelMenu.updateZoomSubmenu();
		return true;
	}

	public void vScroll(int delta)
	{
		if (delta == 0)
			return;
		selected.y += delta;
		if (delta < 0)
		{
			if (selected.y < 1)
			{
				selected.y = Region.galaxySpread;
				if (origin.y < Region.galaxySpread / 2)
				{
					origin.y = selected.y;
					repaintBackground();
				}
			}
			if (selected.y < origin.y)
			{
				if (origin.y - selected.y < Region.galaxySpread / 2)
				{
					origin.y = selected.y;
					repaintBackground();
				}
			}
		}
		else
		{
			if (selected.y > Region.galaxySpread)
			{
				selected.y = 1;
			}
			int sz = (getHeight() - hRulerHeight) / Planet.scale();
			if ((selected.y > origin.y && selected.y - origin.y > sz - 1) ||
				(selected.y < origin.y && Region.galaxySpread + selected.y - origin.y > sz - 1))
			{
				origin.y += delta;
				if (origin.y > Region.galaxySpread)
					origin.y -= Region.galaxySpread;
				repaintBackground();
			}
		}
	}
	public void hScroll(int delta)
	{
		if (delta == 0)
			return;
		selected.x += delta;
		if (delta < 0)
		{
			if (selected.x < 1)
			{
				selected.x = Region.galaxySpread;
				if (origin.x < Region.galaxySpread / 2)
				{
					origin.x = selected.x;
					repaintBackground();
				}
			}
			if (selected.x < origin.x)
			{
				if (origin.x - selected.x < Region.galaxySpread / 2)
				{
					origin.x = selected.x;
					repaintBackground();
				}
			}
		}
		else
		{
			if (selected.x > Region.galaxySpread)
			{
				selected.x = 1;
			}
			int sz = (getWidth() - vRulerWidth) / Planet.scale();
			if ((selected.x > origin.x && selected.x - origin.x > sz - 1) ||
				(selected.x < origin.x && Region.galaxySpread + selected.x - origin.x > sz - 1))
			{
				origin.x += delta;
				if (origin.x > Region.galaxySpread)
					origin.x -= Region.galaxySpread;
				repaintBackground();
			}
		}
	}

	public void center()
	{
		coordDialog.setTitle("Center on planet");
		coordDialog.setLocationRelativeTo(DnC.mainFrame());
		coordDialog.setVisible(true);
	}

	public void center(int x, int y)
	{
		if (x <= 0 || y <= 0 || x > Region.galaxySpread || y > Region.galaxySpread)
		{
			x = selected.x;
			y = selected.y;
		}
		int scale = Planet.scale();
		int w = (getWidth() - vRulerWidth) / scale;
		int h = (getHeight() - hRulerHeight) / scale;
		origin.x = x - w / 2;
		origin.y = y - h / 2;
		if (origin.x < 1)
			origin.x += Region.galaxySpread;
		if (origin.y < 1)
			origin.y += Region.galaxySpread;
		repaintBackground();
	}

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		if (mapPanelImage == null)
		{
			return;
		}
		g.drawImage(mapPanelImage, 0, 0, this);
		
		int x = selected.x - origin.x;
		if (x < 0) x += Region.galaxySpread;
		int y = selected.y - origin.y;
		if (y < 0) y += Region.galaxySpread;
		int scale = Planet.scale();
		g.setColor(Color.white);
		g.drawRect(vRulerWidth + x * scale, hRulerHeight + y * scale, scale, scale);
	}

	private void paintRulers()
	{
		if (hRulerHeight < 0) hRulerHeight = 0;
		if (vRulerWidth  < 0) vRulerWidth  = 0;
		mapPanelGraphics.setFont(rulerFont);
		byte scale = Planet.scale();
		int cs = 1;
		if (hRulerHeight > 0 || showGrid)
		{
			int y = 1 + mapPanelGraphics.getFontMetrics().getAscent();
			mapPanelGraphics.setColor(Color.black);
			if (hRulerHeight > 0)
				mapPanelGraphics.fillRect(0, 0, vRulerWidth, getHeight());
			if (vRulerWidth > 0)
				mapPanelGraphics.fillRect(0, 0, getWidth(), hRulerHeight);
			mapPanelGraphics.setColor(Color.white);
			for (int i = origin.x, j = vRulerWidth; j <= getWidth() - scale; i++)
			{
				int sw = scale / 2;
				if (hRulerHeight > 0)
				{
					if (i > Region.galaxySpread) i -= Region.galaxySpread;
					String s = "" + i;
					sw = mapPanelGraphics.getFontMetrics().stringWidth(s);
					mapPanelGraphics.drawString(s, j + scale / 2 - sw / 2, y);
				}
				int x = j + scale / 2;
				if (hRulerHeight > 0) mapPanelGraphics.drawLine(x, 14, x, 17);
				if (showGrid)
				{
					mapPanelGraphics.setColor(Color.lightGray);
					mapPanelGraphics.drawLine(x, hRulerHeight, x, getHeight());
					mapPanelGraphics.setColor(Color.white);
				}
				j += scale;
				if (sw + 3 > scale)
				{
					cs = sw / scale;
					cs++;
					for (int l = 0; l < cs; l++)
					{
						x = j + scale / 2;
						mapPanelGraphics.drawLine(x, 15, x, 17);
						j += scale;
					}
					i += cs;
				}
			}
		}

		if (vRulerWidth > 0 || showGrid)
		{
			mapPanelGraphics.setColor(Color.black);
			if (hRulerHeight <= 0 && vRulerWidth > 0)
				mapPanelGraphics.fillRect(0, 0, vRulerWidth, getHeight());
			int sh = mapPanelGraphics.getFontMetrics().getAscent() / 2 - 1;
			mapPanelGraphics.setColor(Color.white);
			for (int iy = hRulerHeight; iy <= getHeight() - scale; iy += scale)
			{
				int y = iy + scale / 2;
				int i = origin.y + (iy - hRulerHeight) / scale;
				if (vRulerWidth > 0)
				{
					if (i > Region.galaxySpread) i -= Region.galaxySpread;
					if (i % cs != 0)
						mapPanelGraphics.drawLine(22, y, 24, y);
					else
					{
						mapPanelGraphics.drawLine(21, y, 24, y);
						String s = "" + i;
						int sw = mapPanelGraphics.getFontMetrics().stringWidth(s);
						mapPanelGraphics.drawString(s, 
							vRulerWidth - sw - 4, iy + scale / 2 + sh);
					}
				}
				if (showGrid && i % cs == 0)
				{
					mapPanelGraphics.setColor(Color.lightGray);
					mapPanelGraphics.drawLine(vRulerWidth, y, getWidth(), y);
					mapPanelGraphics.setColor(Color.white);
				}
			}
		}
	}

	@Override public void keyPressed(KeyEvent ke)
	{
		int m = ke.getModifiers();
		char ch = ke.getKeyChar();
		int kc = ke.getKeyCode();
		if (m == 0)
		{
			if (ch == '-')
			{
				zoomOut();
			}
			else if (ch == '+')
			{
				zoomIn();
			}
			else if (kc == KeyEvent.VK_RIGHT)
			{
				hScroll(1);
			}
			else if (kc == KeyEvent.VK_LEFT)
			{
				hScroll(-1);
			}
			else if (kc == KeyEvent.VK_UP)
			{
				vScroll(-1);
			}
			else if (kc == KeyEvent.VK_DOWN)
			{
				vScroll(1);
			}
			else if (kc == KeyEvent.VK_HOME)
			{
				vScroll(-1);
				hScroll(-1);
			}
			else if (kc == KeyEvent.VK_END)
			{
				vScroll(1);
				hScroll(-1);
			}
			else if (kc == KeyEvent.VK_PAGE_UP)
			{
				vScroll(-1);
				hScroll(1);
			}
			else if (kc == KeyEvent.VK_PAGE_DOWN)
			{
				vScroll(1);
				hScroll(1);
			}
			else if (kc == KeyEvent.VK_SPACE)
			{
				center(selected.x, selected.y);
			}
			else if (kc == KeyEvent.VK_F10)
			{
				ke.consume();
			}
		}
		else if (ke.isControlDown())
		{
			if (kc == KeyEvent.VK_G)
			{
				center();
			}
		}
		repaint();
	}

	@Override public void keyReleased(KeyEvent ke)
	{

 		if (ke.getKeyCode() == KeyEvent.VK_F10)
		{
       		Point p = MouseInfo.getPointerInfo().getLocation();
       		popMenu.show(this, p.x, p.y);
		}

	}

	@Override public void keyTyped(KeyEvent ke)
	{
	}

	private boolean imgComplete = false;
	/**
	 * Override the ImageObserver imageUpdate method and monitor the loading of
	 * the image. Set a flag when it is loaded.
	 */
	public boolean imageUpdate(Image img, int infoFlags, int x, int y, int w, int h)
	{
		if (infoFlags != ALLBITS)
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
	} // imageUpdate

	private Cursor lScrollCursor = null;
	private Cursor rScrollCursor = null;
	private Cursor uScrollCursor = null;
	private Cursor dScrollCursor = null;
	private Cursor luScrollCursor = null;
	private Cursor ruScrollCursor = null;
	private Cursor ldScrollCursor = null;
	private Cursor rdScrollCursor = null;
	private void initCursors()
	{
		if (lScrollCursor != null)
			return;
		Image scrollCursors = null;
		try
		{
			/*
			BufferedInputStream is = new BufferedInputStream(
				MapPanel.class.getResourceAsStream("scroll.gif"));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int ch = -1;
			while ((ch = is.read()) >= 0)
			{
				bos.write(ch);
			}
			*/
			imgComplete = false;
			scrollCursors = Toolkit.getDefaultToolkit().createImage("res/scroll.gif");
			//is.close();
			//bos.close();
			scrollCursors.getWidth(this);
			while (!imgComplete) { try { Thread.sleep(10); } catch (Exception e) {} }

			final int cursorSize = 32;
			Dimension d = Toolkit.getDefaultToolkit().getBestCursorSize(cursorSize, cursorSize);
			BufferedImage img = new BufferedImage(cursorSize, cursorSize, BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g = img.getGraphics();
			g.drawImage(scrollCursors, 0, 0, cursorSize, cursorSize, 
				0, 0, cursorSize, cursorSize, this);
			lScrollCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				img, new Point(0, d.height / 2), "lScroll");

			img = new BufferedImage(cursorSize, cursorSize, BufferedImage.TYPE_4BYTE_ABGR);
			g = img.getGraphics();
			g.drawImage(scrollCursors, 0, 0, cursorSize, cursorSize,
				cursorSize, 0, cursorSize * 2, cursorSize, this);
			rScrollCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				img, new Point(d.width - 1, d.height / 2), "rScroll");

			img = new BufferedImage(cursorSize, cursorSize, BufferedImage.TYPE_4BYTE_ABGR);
			g = img.getGraphics();
			g.drawImage(scrollCursors, 0, 0, cursorSize, cursorSize,
				cursorSize * 2, 0, cursorSize * 3, cursorSize, this);
			uScrollCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				img, new Point(d.height / 2, 0), "uScroll");

			img = new BufferedImage(cursorSize, cursorSize, BufferedImage.TYPE_4BYTE_ABGR);
			g = img.getGraphics();
			g.drawImage(scrollCursors, 0, 0, cursorSize, cursorSize,
				cursorSize * 3, 0, cursorSize * 4, cursorSize, this);
			dScrollCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				img, new Point(d.height / 2, d.height - 1), "dScroll");

			img = new BufferedImage(cursorSize, cursorSize, BufferedImage.TYPE_4BYTE_ABGR);
			g = img.getGraphics();
			g.drawImage(scrollCursors, 0, 0, cursorSize, cursorSize, 0,
				cursorSize, cursorSize, cursorSize * 2, this);
			luScrollCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				img, new Point(0, 0), "luScroll");

			img = new BufferedImage(cursorSize, cursorSize, BufferedImage.TYPE_4BYTE_ABGR);
			g = img.getGraphics();
			g.drawImage(scrollCursors, 0, 0, cursorSize, cursorSize,
				cursorSize, cursorSize, cursorSize * 2, cursorSize * 2, this);
			ruScrollCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				img, new Point(d.width - 1, 0), "ruScroll");

			img = new BufferedImage(cursorSize, cursorSize, BufferedImage.TYPE_4BYTE_ABGR);
			g = img.getGraphics();
			g.drawImage(scrollCursors, 0, 0, cursorSize, cursorSize,
				cursorSize * 2, cursorSize, cursorSize * 3, cursorSize * 2, this);
			ldScrollCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				img, new Point(0, d.height - 1), "ldScroll");

			img = new BufferedImage(cursorSize, cursorSize, BufferedImage.TYPE_4BYTE_ABGR);
			g = img.getGraphics();
			g.drawImage(scrollCursors, 0, 0, cursorSize, cursorSize,
				cursorSize * 3, cursorSize, cursorSize * 4, cursorSize * 2, this);
			rdScrollCursor = Toolkit.getDefaultToolkit().createCustomCursor(
				img, new Point(d.width - 1, d.height - 1), "rdScroll");
		} catch (Exception e) {}
	}

	private Runnable scrollThread = null;
	private void performScroll()
	{
		if (scrollThread != null)
			return;
		scrollThread = new Runnable()
		{
			public void run()
			{
				try { Thread.sleep(750); } catch (Exception e) {}
				while (uScroll || dScroll || lScroll || rScroll)
				{
					if (uScroll)
					{
						origin.y--;
						if (origin.y < 1) origin.y = Region.galaxySpread;
					}
					else if (dScroll)
					{
						origin.y++;
						if (origin.y > Region.galaxySpread) origin.y = 1;
					}
					if (lScroll)
					{
						origin.x--;
						if (origin.x < 1) origin.x = Region.galaxySpread;
					}
					else if (rScroll)
					{
						origin.x++;
						if (origin.x > Region.galaxySpread) origin.x = 1;
					}
					repaintBackground();
					try { Thread.sleep(100 * Planet.scale() / 50); } catch (Exception e) {}
				}
				scrollThread = null;
			}
		};
		new Thread(scrollThread).start();
	}

	private boolean showScroll = false;
	private void setScrollCursor()
	{
		if (showScroll && !uScroll && !dScroll && !lScroll && !rScroll)
		{
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			showScroll = false;
			return;
		}
		else if (lScroll && uScroll)
			setCursor(luScrollCursor);
		else if (rScroll && uScroll)
			setCursor(ruScrollCursor);
		else if (lScroll && dScroll)
			setCursor(ldScrollCursor);
		else if (rScroll && dScroll)
			setCursor(rdScrollCursor);
		else if (lScroll)
			setCursor(lScrollCursor);
		else if (rScroll)
			setCursor(rScrollCursor);
		else if (uScroll)
			setCursor(uScrollCursor);
		else if (dScroll)
			setCursor(dScrollCursor);
		showScroll = true;
	}

	private boolean lScroll = false;
	private boolean rScroll = false;
	private boolean uScroll = false;
	private boolean dScroll = false;
	private final int scrollWidth = 20;
	@Override public void mouseMoved(MouseEvent me)
	{
		if (me.getY() < scrollWidth)
		{
			uScroll = true;
			dScroll = false;
		}
		else if (me.getY() > getHeight() - scrollWidth)
		{
			uScroll = false;
			dScroll = true;
		}
		else
		{
			uScroll = false;
			dScroll = false;
		}
		if (me.getX() < scrollWidth + 20)
		{
			lScroll = true;
			rScroll = false;
		}
		else if (me.getX() > getWidth() - scrollWidth)
		{
			lScroll = false;
			rScroll = true;
		}
		else
		{
			lScroll = false;
			rScroll = false;
		}
		setScrollCursor();
		if (uScroll || dScroll || lScroll || rScroll)
		{
			performScroll();
		}
	}

	@Override public void mouseClicked(MouseEvent me)
	{
		int scale = Planet.scale();
		selected.x = origin.x + (me.getX() - vRulerWidth) / scale;
		selected.y = origin.y + (me.getY() - hRulerHeight) / scale;
		if (me.getButton() == MouseEvent.BUTTON1 && me.getClickCount() > 1)
		{
			center(selected.x, selected.y);
			//DnC.empire.setProperty("centerX", Integer.toString(selected.x));
			//DnC.empire.setProperty("centerY", Integer.toString(selected.y));
			//centerGameSkyMap(selected.x, selected.y);
			//Planet.processCurrentRectangle(DnC.empire);
		}
		repaint();
		requestFocus();
		me.consume();
	}

	@Override public void mouseEntered(MouseEvent me)
	{
		requestFocus();
	}

	@Override public void mouseExited(MouseEvent me)
	{
		lScroll = false;
		rScroll = false;
		uScroll = false;
		dScroll = false;
		setCursor(Cursor.getDefaultCursor());
	}

	@Override public void mousePressed(MouseEvent me)
	{
	}

	@Override public void mouseReleased(MouseEvent me)
	{
	}

	@Override public void mouseDragged(MouseEvent me)
	{
	}

	@Override public void mouseWheelMoved(MouseWheelEvent mwe)
	{
		if (mwe.getWheelRotation() > 0)
			zoomIn();
		else
			zoomOut();
	}

    class PopupListener extends MouseAdapter
    {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu)
        {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e)
        {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e)
        {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

	@Override public void componentHidden(ComponentEvent e)
	{
	}

	@Override public void componentMoved(ComponentEvent e)
	{
	}

	@Override public void componentResized(ComponentEvent e)
	{
		mapPanelImage = null;
		if (selected.x == origin.x && selected.y == origin.y)
			center(selected.x, selected.y);
		else
			repaintBackground();
	}

	@Override public void componentShown(ComponentEvent e)
	{
	}

}
