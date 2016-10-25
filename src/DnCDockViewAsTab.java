import java.util.ArrayList;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.DockViewAsTab;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.tabbedpane.SmartIconJButton;

/**
 * @author Angor
 *
 */
@SuppressWarnings("serial")
public class DnCDockViewAsTab extends DockViewAsTab
{

	/** smart icon used to display the title  */
//	protected DnCTabbedPaneSmartIcon smartIcon = new DnCTabbedPaneSmartIcon(null, " ", null);

	public DnCDockViewAsTab(Dockable dockable)
	{
		super(dockable);
		tabHeader.setUI(new DnCTabbedPaneUI());
	}

	public void resetTabIcons()
	{
		ArrayList<SmartIconJButton> icons = new ArrayList<SmartIconJButton>();
		DockKey k = getDockable().getDockKey();
		if (k.isCloseEnabled() && isCloseButtonDisplayed)
		{
			icons.add(closeSmartIcon);
		}
		if (k.isMaximizeEnabled() && isMaximizeButtonDisplayed)
		{
			icons.add(maximizeSmartIcon);
		}
		if (k.isAutoHideEnabled()&& isHideButtonDisplayed)
		{
			icons.add(hideSmartIcon);
		}
		if (k.isFloatEnabled() && isFloatButtonDisplayed)
		{
			icons.add(floatSmartIcon);
		}

		// this is an attempt to create the set of icons displayed for this dockable
		// currently, only "close" is managed, but other buttons could be added, as
		// the jTabebdPaneSmartIcon supports(simulates) many sub-buttons.
		if (icons.size()> 0)
		{
			SmartIconJButton[] iconsArray = (SmartIconJButton[])icons.toArray(new SmartIconJButton[0]);
			smartIcon = new DnCTabbedPaneSmartIcon(k.getIcon(), k.getName(), iconsArray);
			smartIcon.setIconForTabbedPane(tabHeader);
			tabHeader.addTab("", smartIcon, getDockable().getComponent());
		}
		else
		{
			tabHeader.addTab(k.getName(), k.getIcon(), getDockable().getComponent());
		}
	}

}
