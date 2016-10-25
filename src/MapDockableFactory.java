import java.awt.Dimension;
import com.vlsolutions.swing.docking.DefaultDockableContainerFactory;
import com.vlsolutions.swing.docking.DockView;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.MaximizedDockView;
import com.vlsolutions.swing.docking.SingleDockableContainer;

/**
 * @author Angor
 *
 */
public class MapDockableFactory extends DefaultDockableContainerFactory
{

	public MapDockableFactory()
	{
		super();
	}

	@Override public SingleDockableContainer createDockableContainer(Dockable dockable, int parentType)
	{
		if (dockable == DnC.mapPanel())
		{
			MaximizedDockView dv = new MaximizedDockView(dockable);
			dv.getTitleBar().setVisible(false);
			return dv;
		}
		else if (parentType == PARENT_TABBED_CONTAINER)
		{
			return new DnCDockViewAsTab(dockable);
		}
		else if (parentType == PARENT_SPLIT_CONTAINER)
		{
			DockView dv = new DockView(dockable);
			dv.setMinimumSize(new Dimension(240, 0));
			return dv;
		}
		return super.createDockableContainer(dockable, parentType);
	}
}
