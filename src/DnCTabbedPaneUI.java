import java.awt.FontMetrics;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.View;

/**
 * @author Angor
 *
 */
public class DnCTabbedPaneUI extends BasicTabbedPaneUI
{

	protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, 
		Rectangle tabRect, Rectangle iconRect, Rectangle textRect, boolean isSelected)
	{
		textRect.x = textRect.y = iconRect.x = iconRect.y = 0;

		View v = getTextViewForTab(tabIndex);
		if (v != null)
		{
			tabPane.putClientProperty("html", v);
		}

		SwingUtilities.layoutCompoundLabel((JComponent)tabPane, metrics, title, icon,
				SwingUtilities.CENTER,
				SwingUtilities.LEADING,
				SwingUtilities.CENTER,
				SwingUtilities.TRAILING,
				tabRect, iconRect, textRect, textIconGap);

		tabPane.putClientProperty("html", null);

		int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
		int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
		iconRect.x += xNudge;
		iconRect.y += yNudge;
		textRect.x += xNudge;
		textRect.y += yNudge;
	}

	protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics)
	{
        int width = tabPane.getWidth();//super.calculateTabWidth(tabPlacement, tabIndex, metrics);
        return width;
    }

}
