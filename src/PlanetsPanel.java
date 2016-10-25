import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingConstants;

/**
 * @author Angor
 *
 */
@SuppressWarnings("serial")
public class PlanetsPanel extends JPanel implements Dockable
{

	private DockKey key = new DockKey("planetsPanel");

	public PlanetsPanel()
	{
		key.setAutoHideBorder(DockingConstants.HIDE_RIGHT);
		key.setFloatEnabled(true);
		key.setName("Planets");
		//key.setAutoHideEnabled(true);
		key.setResizeWeight(0.15F);
		setPreferredSize(new Dimension(240, 320));
	}

	@Override public Component getComponent()
	{
		return this;
	}

	@Override public DockKey getDockKey()
	{
		return key;
	}

}
