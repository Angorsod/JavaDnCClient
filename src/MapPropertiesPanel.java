import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingConstants;

/**
 * @author Angor
 *
 */
@SuppressWarnings("serial")
public class MapPropertiesPanel extends JPanel implements Dockable, ItemListener
{

	public class FillLayout implements LayoutManager
	{
		protected Component theComponent = null;
		public void addLayoutComponent(String name, Component comp)
		{
			theComponent = comp;
		}
		public void removeLayoutComponent(Component comp)
		{
			if (comp == theComponent)
				theComponent = null;
		}
		public Dimension preferredLayoutSize(Container parent)
		{
			if (theComponent == null)
				return new Dimension(0, 0);
			return theComponent.getPreferredSize();
		}
		public Dimension minimumLayoutSize(Container parent)
		{
			return preferredLayoutSize(parent);    
		}
		public void layoutContainer(Container parent)
		{
			if (theComponent == null) return;
			theComponent.setBounds(0, 0, parent.getWidth(), parent.getHeight());
		}
	}

	private DockKey key = new DockKey("mapPropertiesPanel");

	// a container to put all JXTaskPane together
	JXTaskPaneContainer taskPaneContainer = new JXTaskPaneContainer();

	JXTaskPane layersCtrlPane = new JXTaskPane();
	JXTaskPane filtersPane = new JXTaskPane();
	JScrollPane scroll;

	JCheckBox geologyCheck = new JCheckBox("Geology");
	JCheckBox natalityCheck = new JCheckBox("Natality");
	JCheckBox gridCheck = new JCheckBox("Grid");
	JCheckBox coarseGridCheck = new JCheckBox("Coarse Grid");
	JCheckBox planetsCheck = new JCheckBox("Planets Icon");
	JCheckBox iconsCheck = new JCheckBox("Icons");
	JCheckBox areasCheck = new JCheckBox("Areas");
	JCheckBox tracesCheck = new JCheckBox("Traces");
	JCheckBox fleetsCheck = new JCheckBox("Fleets");

	MapPropertiesPanel()
	{
		key.setAutoHideBorder(DockingConstants.HIDE_RIGHT);
		key.setFloatEnabled(true);
		key.setName("Map Properties");
		key.setResizeWeight(0.0F);
		setPreferredSize(new Dimension(240, 320));
		setMinimumSize(new Dimension(240, 0));

		//taskPaneContainer.setLayout(new GridBagLayout());
		taskPaneContainer.setLayout(new VerticalLayout(5));
		//GridBagConstraints constraints = new GridBagConstraints();
		UIManager.put("TaskPaneContainer.border", BorderFactory.createEmptyBorder(3, 0, 3, 0));
		taskPaneContainer.updateUI();
		layersCtrlPane.setTitle("Layers control");
		//layersCtrlPane.setSpecial(true);
		//Font f = new Font("Arial", Font.PLAIN, 12);
		geologyCheck.setMnemonic(KeyEvent.VK_G);
		layersCtrlPane.add(geologyCheck);
		natalityCheck.setMnemonic(KeyEvent.VK_N);
		layersCtrlPane.add(natalityCheck);
		gridCheck.setMnemonic(KeyEvent.VK_R);
		layersCtrlPane.add(gridCheck);
		coarseGridCheck.setMnemonic(KeyEvent.VK_C);
		layersCtrlPane.add(coarseGridCheck);
		planetsCheck.setMnemonic(KeyEvent.VK_P);
		layersCtrlPane.add(planetsCheck);
		iconsCheck.setMnemonic(KeyEvent.VK_I);
		layersCtrlPane.add(iconsCheck);
		areasCheck.setMnemonic(KeyEvent.VK_A);
		layersCtrlPane.add(areasCheck);
		tracesCheck.setMnemonic(KeyEvent.VK_T);
		layersCtrlPane.add(tracesCheck);
		fleetsCheck.setMnemonic(KeyEvent.VK_F);
		layersCtrlPane.add(fleetsCheck);

		geologyCheck.addItemListener(this);
		natalityCheck.addItemListener(this);
		gridCheck.addItemListener(this);
		coarseGridCheck.addItemListener(this);
		planetsCheck.addItemListener(this);
		iconsCheck.addItemListener(this);
		areasCheck.addItemListener(this);
		tracesCheck.addItemListener(this);
		fleetsCheck.addItemListener(this);

		geologyCheck.setSelected(true);
		natalityCheck.setSelected(true);
		gridCheck.setSelected(true);
		coarseGridCheck.setSelected(true);
		planetsCheck.setSelected(true);
		iconsCheck.setSelected(true);
		areasCheck.setSelected(true);
		tracesCheck.setSelected(true);
		fleetsCheck.setSelected(true);

		// add this taskPane to the taskPaneContainer
		/*
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.ipadx = 0;
		constraints.ipady = 0;
		constraints.insets = new Insets(0, 0, 5, 0);
		//constraints.anchor = GridBagConstraints.FIRST_LINE_START;
		*/
		taskPaneContainer.add(layersCtrlPane);//, constraints);

		filtersPane.setTitle("Filters");
		Insets ins = filtersPane.getContentPane().getInsets();
		((JComponent)(filtersPane.getContentPane())).setBorder(BorderFactory.createEmptyBorder(ins.top, 8, ins.bottom, 8));
		initFilters();
		filtersPane.add(new FilterPane());
		/*
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridy = 1;
		constraints.insets = new Insets(5, 0, 0, 0);
		//constraints.anchor = GridBagConstraints.LINE_START;
		*/
		taskPaneContainer.add(filtersPane);//, constraints);

		// put the planet properties on the top
		setLayout(new FillLayout());
		scroll = new JScrollPane(taskPaneContainer, 
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(null);
		add(scroll, "FillLayout");
	}

	private void initFilters()
	{
		for (int i = 0; i < Planet.Filter.filtersCount(); i++)
		{
			FilterPane fp = new FilterPane();
			filtersPane.add(fp);
			fp.filter(Planet.Filter.getFilter(i));
		}
	}

	public void addFilter(FilterPane filter)
	{
		if (filter == null) filter = new FilterPane();
		filtersPane.add(filter);
		taskPaneContainer.doLayout();
	}

	public void loadCheckBoxes()
	{
		DnC empire = DnC.empire();
		if (empire == null) return;
		//String s = empire.getProperty("ShowGeology");
		geologyCheck.setSelected(!empire.getProperty("ShowGeology").equals("0"));
		natalityCheck.setSelected(!empire.getProperty("ShowNatality").equals("0"));
		gridCheck.setSelected(!empire.getProperty("ShowGrid").equals("0"));
		coarseGridCheck.setSelected(!empire.getProperty("ShowCoarseGrid").equals("0"));
		planetsCheck.setSelected(!empire.getProperty("ShowPlanetsIcon").equals("0"));
		iconsCheck.setSelected(!empire.getProperty("ShowIcons").equals("0"));
		areasCheck.setSelected(!empire.getProperty("ShowAreas").equals("0"));
		tracesCheck.setSelected(!empire.getProperty("ShowTraces").equals("0"));
		fleetsCheck.setSelected(!empire.getProperty("ShowFleets").equals("0"));
	}
	public void saveCheckBoxes()
	{
		DnC empire = DnC.empire();
		if (empire == null) return;
		empire.setProperty("ShowGeology", geologyCheck.isSelected() ? "1" : "0");
		empire.setProperty("ShowNatality", natalityCheck.isSelected() ? "1" : "0");
		empire.setProperty("ShowGrid", gridCheck.isSelected() ? "1" : "0");
		empire.setProperty("ShowCoarseGrid", coarseGridCheck.isSelected() ? "1" : "0");
		empire.setProperty("ShowPlanetsIcon", planetsCheck.isSelected() ? "1" : "0");
		empire.setProperty("ShowIcons", iconsCheck.isSelected() ? "1" : "0");
		empire.setProperty("ShowAreas", areasCheck.isSelected() ? "1" : "0");
		empire.setProperty("ShowTraces", tracesCheck.isSelected() ? "1" : "0");
		empire.setProperty("ShowFleets", fleetsCheck.isSelected() ? "1" : "0");
	}

	@Override public Component getComponent()
	{
		return this;
	}

	@Override public DockKey getDockKey()
	{
		return key;
	}

	@Override public void itemStateChanged(ItemEvent e)
	{
		Object source = e.getItemSelectable();
		if (source == geologyCheck)
		{
			Planet.showGeology(e.getStateChange() == ItemEvent.SELECTED);
		}
		else if (source == natalityCheck)
		{
			Planet.showNatality(e.getStateChange() == ItemEvent.SELECTED);
		}
		else if (source == planetsCheck)
		{
			Planet.showPlanetIcon(e.getStateChange() == ItemEvent.SELECTED);
		}
	}

	class FilterPane extends JPanel implements ActionListener, ItemListener
	{
		GroupLayout layout = new GroupLayout(this);
		private JComboBox field;
		private JComboBox condition;
		private MutableComboBoxModel conditionModel;
		private JTextField value = new JTextField();
		private JSpinner spinnerValue = new JSpinner();
		private JSpinner.DefaultEditor spinnerEditor;
		private JComboBox comboValue = new JComboBox();
		private JButton delete;
		private JCheckBox enableCheck;
		private JPopupMenu menu = new JPopupMenu();
		private JMenuItem organics = new JMenuItem("Organics");
		private JMenuItem energy = new JMenuItem("Energy");
		private JMenuItem minerals = new JMenuItem("Minerals");
		private JMenuItem temperature = new JMenuItem("Temperature");
		private JMenuItem surface = new JMenuItem("Surface");
		private JMenuItem xPos = new JMenuItem("X     ");
		private JMenuItem yPos = new JMenuItem("Y     ");
		private JMenuItem owner = new JMenuItem("Name");
		private JMenuItem diplomacy = new JMenuItem("Diplomacy");
		private JMenuItem organization = new JMenuItem("Organization");
		private JMenuItem name = new JMenuItem("Name");
		private JMenuItem comment = new JMenuItem("Commentary");
		private JMenuItem mainIcon = new JMenuItem("Main Icon");
		private JMenuItem extraIcon = new JMenuItem("Extra Icon");
		private JMenuItem jumpable = new JMenuItem("Jumpable");
		private JMenuItem natality = new JMenuItem("Natality");
		private JMenuItem income = new JMenuItem("Income");
		private JMenuItem mainMining = new JMenuItem("Main Mining");
		private JMenuItem extraMining = new JMenuItem("ExtraMining");
		private JMenuItem mainIncome = new JMenuItem("Main Income");
		private JMenuItem extraIncome = new JMenuItem("Extra Income");
		private JMenuItem current = null;
		private String equalItem		= "==";
		private String notEqualItem		= "!=";
		private String greaterItem		= ">";
		private String lessItem			= "<";
		private String greaterEqualItem	= ">=";
		private String lessEqualItem	= "<=";
//		private String includeItem		= "Include";
//		private String exludeItem		= "Exclude";
		private Planet.Filter filter = null;
		public FilterPane(Planet.Filter filter)
		{
			this();
			filter(filter);
		}
		public void filter(Planet.Filter filter)
		{
			this.filter = filter;
			switch (this.filter.parameter())
			{
				case Planet.Filter.ORGANICS:
					geologySelected("Organics", true);
					break;
				case Planet.Filter.ENERGY:
					geologySelected("Energy", true);
					break;
				case Planet.Filter.MINERALS:
					geologySelected("Minerals", true);
					break;
				case Planet.Filter.TEMPERATURE:
					geologySelected("Temperature", true);
					break;
				case Planet.Filter.SURFACE:
					geologySelected("Surface", false);
					break;
				case Planet.Filter.NATALITY:
					break;
				default:
					return;
			}
			int cond = filter.condition();
			if (condition.getModel().getSize() > cond)
			{
				condition.setSelectedIndex(cond + 1);
				if (spinnerValue.isVisible())
				{
					setEnableValueField(true);
					spinnerValue.setValue(filter.intValue());
					boolean en = filter.enable();
					setEnabledButtonCheck(true);
					enableCheck.setSelected(en);
				}
			}
		}
		private FilterPane()
		{
			Dimension size = new Dimension(132, 24);
			//setPreferredSize(size);
			//setMinimumSize(size);
			setLayout(layout);
			JLabel lbl = new JLabel("Field:");
			int width = lbl.getFontMetrics(lbl.getFont()).stringWidth(lbl.getText());
			JMenu submenu = new JMenu("Geology");
			submenu.add(organics);
			submenu.add(energy);
			submenu.add(minerals);
			submenu.add(temperature);
			submenu.add(surface);
			menu.add(submenu);
			submenu = new JMenu("Position");
			submenu.add(xPos);
			submenu.add(yPos);
			menu.add(submenu);
			submenu = new JMenu("Owner");
			submenu.add(owner);
			submenu.add(diplomacy);
			submenu.add(organization);
			menu.add(submenu);
			submenu = new JMenu("Planet");
			submenu.add(name);
			submenu.add(comment);
			submenu.add(mainIcon);
			submenu.add(extraIcon);
			submenu.add(jumpable);
			menu.add(submenu);
			submenu = new JMenu("Income");
			submenu.add(natality);
			submenu.add(income);
			submenu.add(mainMining);
			submenu.add(extraMining);
			submenu.add(mainIncome);
			submenu.add(extraIncome);
			menu.add(submenu);
			field = new JComboBox();
			//size.height = 24;
			//size.width = 130;
			field.setPreferredSize(size);
			field.setMaximumSize(size);
			field.setMinimumSize(size);
			field.setRenderer(new ParameterRenderer());
			field.addItem("Select parameter...");
			conditionModel = new DefaultComboBoxModel();
			conditionModel.addElement("Select...");
			condition = new JComboBox(conditionModel);
			condition.setEditable(false);
			condition.setEnabled(false);
			//condition.addItemListener(this);
			value.setEnabled(false);
			spinnerValue.setEnabled(false);
			spinnerValue.setVisible(false);
			comboValue.setEnabled(false);
			comboValue.setVisible(false);
			add(spinnerValue);
			add(comboValue);
			delete = new JButton("Add");
			delete.setEnabled(false);
			delete.addActionListener(this);
			enableCheck = new JCheckBox("Enable");
			enableCheck.setMargin(new Insets(0, 0, 0, 0));
			enableCheck.setEnabled(false);
			enableCheck.addItemListener(this);
			organics.addActionListener(this);
			energy.addActionListener(this);
			minerals.addActionListener(this);
			temperature.addActionListener(this);
			surface.addActionListener(this);
			xPos.addActionListener(this);
			yPos.addActionListener(this);
			owner.addActionListener(this);
			diplomacy.addActionListener(this);
			organization.addActionListener(this);
			name.addActionListener(this);
			comment.addActionListener(this);
			mainIcon.addActionListener(this);
			extraIcon.addActionListener(this);
			jumpable.addActionListener(this);
			natality.addActionListener(this);
			income.addActionListener(this);
			mainMining.addActionListener(this);
			extraMining.addActionListener(this);
			mainIncome.addActionListener(this);
			extraIncome.addActionListener(this);

			// Create a sequential group for the horizontal axis.
			GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();
			// The sequential group in turn contains two parallel groups.
			// One parallel group contains the labels, the other the text fields.
			// Putting the labels in a parallel group along the horizontal axis
			// positions them at the same x location.
			//
			// Variable indentation is used to reinforce the level of grouping.
			hGroup.addGroup(layout.createParallelGroup(Alignment.LEADING, false).addComponent(lbl, Alignment.CENTER).
				addComponent(condition, GroupLayout.PREFERRED_SIZE, width * 11 / 5, GroupLayout.PREFERRED_SIZE).
				addComponent(enableCheck, GroupLayout.PREFERRED_SIZE, width * 11 / 5, GroupLayout.PREFERRED_SIZE)).
					addGap(5, 5, 5);//addContainerGap(5, 5);//addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
			hGroup.addGroup(layout.createParallelGroup(Alignment.LEADING, false).
				addComponent(field).addComponent(value).addComponent(delete, Alignment.CENTER));
			//layout.linkSize(SwingConstants.HORIZONTAL, lbl, condition);
			layout.setHorizontalGroup(hGroup);

			// Create a sequential group for the vertical axis.
			GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
			vGroup.addGap(8, 8, 8);
			// The sequential group contains two parallel groups that align
			// the contents along the baseline. The first parallel group contains
			// the first label and text field, and the second parallel group contains
			// the second label and text field. By using a sequential group
			// the labels and text fields are positioned vertically after one another.
			layout.linkSize(SwingConstants.VERTICAL, condition, value);
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(lbl).addComponent(field)).
				addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(condition).addComponent(value)).
				addGap(8, 8, 8);//(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(enableCheck).addComponent(delete)).
				addGap(8, 8, 8);
			layout.setVerticalGroup(vGroup);
		}
		@Override public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == delete)
			{
				setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.black));
				addFilter(null);
				return;
			}
			current = (JMenuItem)(e.getSource());
			if (current == organics)
			{
				geologySelected("Organics", true);
			}
			else if (current == energy)
			{
				geologySelected("Energy", true);
			}
			else if (current == minerals)
			{
				geologySelected("Minerals", true);
			}
			else if (current == temperature)
			{
				geologySelected("Temperature", true);
			}
			else if (current == surface)
			{
				geologySelected("Surface", false);
			}
		}
		private void parameterSelected()
		{
			field.removeItemAt(0);
			condition.setEnabled(true);
			condition.requestFocus();
			//value.setEnabled(true);
			//enableCheck.setEnabled(true);
		}
		private void geologySelected(String param, boolean includeZero)
		{
			field.addItem(param);
			parameterSelected();
			if (!spinnerValue.isVisible())
			{
				conditionModel = new DefaultComboBoxModel(
					new String[]{ "Select...", equalItem, notEqualItem, greaterItem, lessItem, greaterEqualItem, lessEqualItem });
				condition.setModel(conditionModel);
				value.setVisible(false);
				spinnerValue.setVisible(true);
				layout.replace(value, spinnerValue);
				//spinnerValue.setBounds(value.getBounds());
				spinnerValue.setModel(new SpinnerNumberModel(90, 0, 99, 1));
				spinnerValue.setValue(90);
				spinnerEditor = new JSpinner.NumberEditor(spinnerValue, "#0");
				spinnerValue.setEditor(spinnerEditor);
				Dimension d = spinnerValue.getSize();
				d.height += 2;
				spinnerValue.setSize(d);
			}
			((SpinnerNumberModel)spinnerValue.getModel()).setMinimum(includeZero ? 0 : 1);
			spinnerValue.setValue(spinnerValue.getValue());
			//spinnerValue.setEnabled(false);
			//add(spinnerValue);
			condition.addPopupMenuListener(new PopupMenuListener()
			{
				@Override public void popupMenuCanceled(PopupMenuEvent e) {}
				@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
				{
					if (condition.getSelectedIndex() > 0)
					{
						setEnableValueField(true);
						focusValue();
					}
					else
					{
						setEnableValueField(false);
						setEnabledButtonCheck(false);
					}
				}
				@Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
			});

		}
		private void setEnableValueField(boolean enable)
		{
			if (value.isVisible())
				value.setEnabled(enable);
			else if (comboValue.isVisible())
				comboValue.setEnabled(enable);
			else
				spinnerValue.setEnabled(enable);
		}
		private void setEnabledButtonCheck(boolean enable)
		{
			enableCheck.setEnabled(enable);
			enableCheck.setSelected(enable);
			delete.setEnabled(enable);
		}
		private void focusValue()
		{
			if (value.isVisible())
				value.requestFocus();
			else if (comboValue.isVisible())
				comboValue.requestFocus();
			else
			{
				//if (spinnerValue.hasFocus()) return;
				JTextField text = ((JSpinner.DefaultEditor)spinnerValue.getEditor()).getTextField();
				text.addFocusListener(new FocusAdapter()
				{
					public void focusGained(FocusEvent e)
					{
						//System.out.println("here");
						if (e.getSource() instanceof JTextComponent)
						{
							final JTextComponent textComponent = ((JTextComponent)e.getSource());
							SwingUtilities.invokeLater(new Runnable() { public void run() { textComponent.selectAll(); }});
						}
					}
					public void focusLost(FocusEvent e) {}
				});
				text.requestFocus();
				setEnabledButtonCheck(true);
			}
		}
		@Override public void itemStateChanged(ItemEvent e)
		{
			if (e.getSource() == enableCheck && filter != null)
			{
				filter.enable(e.getStateChange() == ItemEvent.SELECTED);
				Planet.Filter.updateFilter(filter);
				DnC.mapPanel().repaintBackground();
			}
/*			if (e.getStateChange() == ItemEvent.DESELECTED) return;
			if (JComboBox.class.cast(e.getSource()).getSelectedIndex() <= 0)
			{
				setEnableValueField(false);
				setEnabledButtonCheck(false);
				return;
			}
			setEnableValueField(true);
			focusValue();
*/		}

		class ParameterRenderer extends JLabel implements ListCellRenderer
		{
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				if (field.isPopupVisible())
				{
					Dimension d = menu.getPreferredSize();
					d.width = field.getWidth();
					menu.setPreferredSize(d);
					field.hidePopup();
					menu.show(field, 0, field.getHeight());
				}
				setText((String)value);
				return this;
			}
			
		}
	}

}
