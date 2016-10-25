import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import com.vlsolutions.swing.tabbedpane.JTabbedPaneSmartIcon;
import com.vlsolutions.swing.tabbedpane.SmartIconJButton;

/**
 * @author Angor
 *
 */
public class DnCTabbedPaneSmartIcon extends JTabbedPaneSmartIcon
{

	/** calculated used width (icon + label + other icons) + gaps */
	protected int width;
	/** tab height */
	protected int height;
	/** x location where the other icons are drawn*/
	protected int _otherIconsOffset;
	/** gap between the icon and the text*/
	protected int _textIconGap;
	/** gap between the text and the following icons*/
	protected int _otherIconsGap;
	protected int _inBetweenOtherIconsGap;

	/* The container this icon is for (required to calculate proper widths and heights) */
	protected JTabbedPane _container;

	// list of additional icons (presented as buttons)
	protected SmartIconJButton[] otherIcons;
	/** currently pressed inner button */
	protected SmartIconJButton pressedButton;
	/** currently rolled-over inner button */
	protected SmartIconJButton rolloverButton;

	/** the traditional tab selector tooltip text*/
	protected String tooltipText;
	/** local tooltip text : the one to use during mouse movements (depends on the mouse position,
	 * and can be either the tab selector tooltip text or one of the smart buttons included into this icon).
	 */
	protected String localTooltipText;

	protected boolean antialiased = false;

	/** Constructs a new smart icon with a given set of additional buttons */
	public DnCTabbedPaneSmartIcon(Icon icon, String label, SmartIconJButton[] otherIcons)
	{
		super(icon, label, otherIcons);
		this.otherIcons = otherIcons;
		_textIconGap = UIManager.getInt("TabbedPane.textIconGap");
		_otherIconsGap = UIManager.getInt("TabbedPane.otherIconsGap");
		_inBetweenOtherIconsGap = UIManager.getInt("TabbedPane.inBetweenOtherIconsGap");
		invalidateSize();
		try
		{
			//mimic the unofficial aa settings of Swing
			this.antialiased = "true".equals(System.getProperty("swing.aatext"));
		} catch (SecurityException ignore) { /* for untrusted web start apps failing gracefully */ }
	}

	protected void invalidateSize()
	{
		this.width = this.height = -1;
	}

	/** paints the icon (and the associated label and sub-icons) */
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Graphics2D g2 = (Graphics2D) g;
		Object renderingValue = null;
		if (this.antialiased)
		{
			renderingValue = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    }

		// Angor: add border
		x += 5;

	    Icon icon = getIcon();
	    if (icon != null)
	    	icon.paintIcon(c, g, x, y);

	    Font f = UIManager.getFont("JTabbedPaneSmartIcon.font"); // 2006/01/23
	    if (f != null)
	    	g.setFont(f);

	    // reevaluate the width with correct graphics object, in case something has changed
	    FontMetrics fm = g.getFontMetrics();
	    int iconsOffset = _otherIconsOffset;

	    String label = getLabel();
	    if (icon != null)
	    	g.drawString(label, x + icon.getIconWidth() + _textIconGap, y + height - fm.getDescent());
	    else
	    	g.drawString(label, x, y + height - fm.getDescent());

	    if (otherIcons != null)    //2005/11/01
	    {
	    	for (int i = 0; i < otherIcons.length; i++)
	    	{
	    		otherIcons[i].paintIcon(c, g, x + iconsOffset, y + height / 2 - otherIcons[i].getIconHeight() / 2);
	    		iconsOffset += otherIcons[i].getIconWidth();
	    		if (i < otherIcons.length - 1)
	    			iconsOffset += _inBetweenOtherIconsGap;
	    	}
	    }

	    if (antialiased)
	    	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, renderingValue);
	}

	/** Specify which container will use this icon.
	 * <p> If the icon is shared between containers, please provide at least one as
	 * this allows the icon to properly estimate its dimension.
	 */
	public void setIconForTabbedPane(JTabbedPane container)
	{
		this._container = container;
		super.setIconForTabbedPane(container);
	}

	/** Returns the width of this icon */
	public int getIconWidth()
	{
		if (width < 0)
		{
			if (_container == null)
				throw new NullPointerException("container for this smart icon not specified with setIconForTabbedPane()");
			this.width = _container.getWidth();
			this._otherIconsOffset = width - 10; 		// Angor: add border
			if (otherIcons != null)
			{
				for (int i = otherIcons.length - 1; i >= 0; i--)
				{
					_otherIconsOffset -= otherIcons[i].getIconWidth(); // additional width for icons
					if (i < otherIcons.length - 1)
						_otherIconsOffset -= _inBetweenOtherIconsGap;
				}
			}
		}
		return width;
	}

	/** Returns the height of this icon */
	public int getIconHeight()
	{
		if (height < 0)
		{
			Icon icon = getIcon();
			height = (icon != null) ? icon.getIconHeight() : 16; // standard height (as if there was an icon) : should be calculated instead of fixed
		}
		return height;
	}

	public void setSmartButton(int index, SmartIconJButton btn)
	{
		super.setSmartButton(index, btn);
		invalidateSize();
	}

	/** Update the label to be displayed on the tab */
	public void setLabel(String label)
	{
		super.setLabel(label);
		invalidateSize();
	}

	/** Update the main icon (left) to be displayed on the tab */
	public void setIcon(Icon icon)
	{
		super.setIcon(icon);
		invalidateSize();
	}

	/** Update the tooltip of this icon */
	public void setTooltipText(String tooltip)
	{
		super.setTooltipText(tooltip);
	    this.tooltipText = tooltip;
	}

	/** Return the tooltip of this icon */
	public String getTooltipText()
	{
		return this.tooltipText;
	}

	/** Return the local tooltip of this icon (the one associated with inner mouse movements)*/
	public String getLocalTooltipText()
	{
		return this.localTooltipText;
	}

	protected SmartIconJButton findButtonAt(Point p)
	{
		int start = _otherIconsOffset;
		if (otherIcons != null)    //2005/11/01
		{
			for (int i = 0; i < otherIcons.length; i++)
			{
				SmartIconJButton btn = otherIcons[i];
				if (p.x >= start && p.x < start + btn.getIconWidth())
				{
					if (p.y >= height / 2 - btn.getIconHeight() / 2 && 
						p.y <  height / 2 + btn.getIconHeight() / 2)
					{
						return btn;
					}
					else return null;
				}
				start += btn.getIconWidth(); // bug corrected thanks to Emmanuel GAUVRIT 2005/11/08
				if (i < otherIcons.length - 1)
					start += _inBetweenOtherIconsGap;
			}
		}
		return null;
	}


	/** Process the mouse pressed event.
	 *<p>
	 * Mouse coordinates are given relative to this icon
	 */
	public boolean onMousePressed(MouseEvent e)
	{
		// find the icon under the point
		SmartIconJButton btn = findButtonAt(e.getPoint());
		if (btn != null && btn.isEnabled())
		{
			btn.setPressed(true);
			pressedButton = btn;
			return true;
		}
		return false;
	}

	/** Process the mouse released event.
	 *<p>
	 * Mouse coordinates are given relative to this icon
	 */
	public boolean onMouseReleased(final MouseEvent e)
	{
		if (pressedButton != null && pressedButton.isEnabled())
		{
			pressedButton.setPressed(false);
			final SmartIconJButton btn = findButtonAt(e.getPoint());
			if (btn == pressedButton)
			{
				btn.fireAction(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, ""));
				return true;
			}
		}
		return false;
	}

	/** Process the mouse exited event.
	 *<p>
	 * Mouse coordinates are given relative to this icon
	 */
	public boolean onMouseExited(MouseEvent e)
	{
		// reset all rollover states
		if (rolloverButton != null)
		{
			rolloverButton.setRollover(false);
			rolloverButton = null;
			localTooltipText = tooltipText;
			return true;
		}
		rolloverButton = null;
		return false;
	}

	/** Process the mouse moved event.
	 *<p>
	 * Mouse coordinates are given relative to this icon
	 */
	public boolean onMouseMoved(MouseEvent e)
	{
		// check for a rollover effect
		SmartIconJButton btn = findButtonAt(e.getPoint());
		boolean shouldRepaint = false;
		if (btn != null)
		{
			String tip = btn.getTooltipText();
			if (tip != null)
			{
				if (!tip.equals(localTooltipText))
				{
					localTooltipText = tip;
					shouldRepaint = true;
				}
			}
			else if (localTooltipText != tooltipText)
			{
				localTooltipText = tooltipText;
				shouldRepaint = true;
			}
			if (btn == rolloverButton)
			{
				// still on the same button
			}
			else
			{
				// another button
				if (rolloverButton != null)
					rolloverButton.setRollover(false);
				rolloverButton = btn;
				rolloverButton.setRollover(true);
				shouldRepaint = true;
			}
		}
		else if (rolloverButton != null)
		{
			rolloverButton.setRollover(false);
			rolloverButton = null;
			localTooltipText = tooltipText;
			shouldRepaint = true;
		}

		return shouldRepaint;
	}

}
