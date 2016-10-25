import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author Angor
 *
 */
@SuppressWarnings("serial")
public class GroupsPanel extends JPanel implements ActionListener, ListSelectionListener, FocusListener
{

	public GroupsPanel(AccountsPanel accounts)
	{
		this.accounts = accounts;
		accounts.updateGroupList();
		setLayout(null);
		setBounds(10, 5, 305, 434);
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Groups"));
		removeGroupButton = new JButton("Remove");
		removeGroupButton.setBounds(160, 340, 140, 20);
		removeGroupButton.setEnabled(false);
		removeGroupButton.addActionListener(this);
		add(removeGroupButton);
		groupListModel = new DefaultListModel();
		fillList();
		groupList = new JList(groupListModel);
		groupList.setTransferHandler(new GroupTransferHandler());
		groupList.addListSelectionListener(this);
		groupList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane = new JScrollPane(groupList);
		scrollPane.setBounds(10, 20, 285, 280);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane);
		addGroupButton = new JButton("Add");
		addGroupButton.setBounds(160, 310, 140, 20);
		addGroupButton.addActionListener(this);
		add(addGroupButton);
		applyButton = new JButton("Apply new name");
		applyButton.setBounds(10, 340, 145, 20);
		applyButton.setEnabled(false);
		applyButton.addActionListener(this);
		add(applyButton);
		groupName = new JTextField();
		groupName.setBounds(10, 310, 145, 21);
		groupName.setTransferHandler(null);
		groupName.addFocusListener(this);
		add(groupName);
		groupList.setSelectedIndex(0);
	}

	private void fillList()
	{
		groupListModel.clear();
		groupListModel.addElement(" <all>");
		groupListModel.addElement(" <none>");
		int[] groups = Account.getGroups();
		if (groups == null) return;
		for (int i = 0; i < groups.length; i++)
		{
			String gn = Account.getGroupName(groups[i]);
			if (gn.isEmpty()) continue;
			groupListModel.addElement(" " + gn);
		}
		if (groupListModel.getSize() > 2) removeGroupButton.setEnabled(true);
	}

	private DefaultListModel groupListModel;
	private JList groupList;
	private JButton removeGroupButton;
	private JTextField groupName;
	private JButton addGroupButton;
	private JButton applyButton;
	private AccountsPanel accounts;

	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		String name = " " + groupName.getText();
		int index = groupList.getSelectedIndex();
		if (src == addGroupButton)
		{
			// User did not type in a unique name...
			if (name.length() <= 1 || groupListModel.contains(name))
			{
				Toolkit.getDefaultToolkit().beep();
				groupName.requestFocusInWindow();
				groupName.selectAll();
				return;
			}
			if (!Account.addGroup(groupName.getText()))
			{
				Toolkit.getDefaultToolkit().beep();
				groupName.requestFocusInWindow();
				groupName.selectAll();
				return;
			}
			groupListModel.addElement(name);
			//Reset the text field.
			groupName.requestFocusInWindow();
			groupName.setText("");
			removeGroupButton.setEnabled(true);
		}
		else if (src == removeGroupButton)
		{
			if (index < 2)
			{
				return;
			}
			if (!Account.removeGroup(groupName.getText()))
			{
				return;
			}
		    groupListModel.remove(index);

		    int size = groupListModel.getSize();

		    if (size <= 2) // Nobody's left, disable firing.
		    {
		        removeGroupButton.setEnabled(false);
		    }
		    else // Select an index.
		    {
		        if (index == size)
		        {
		            // removed item in last position
		            index--;
		        }

		        groupList.setSelectedIndex(index);
		        groupList.ensureIndexIsVisible(index);
		    }
		}
		else if (src == applyButton)
		{
			if (index < 2)
			{
				return;
			}
			if (!Account.updateGroup(((String)groupListModel.get(index)).substring(1), groupName.getText()))
			{
				return;
			}
			groupListModel.set(index, name);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent event)
	{
		JList list = (JList)event.getSource();
		int[] indices = list.getSelectedIndices();
		try
		{
			if (indices.length > 1 || indices[0] < 2)
			{
				if (indices.length > 1)
				{
					for (int i = 0; i < indices.length; i++)
					{
						if (indices[i] == 0)
						{
							selectAllGroup();
							return;
						}
					}
				}
				groupName.setText("");
				applyButton.setEnabled(false);
				accounts.selectGroup(list.getSelectedIndex());
				accounts.updateGroupContent();
				return;
			}
		}
		catch (Exception e)
		{
			selectAllGroup();
			return;
		}
		accounts.selectGroup(list.getSelectedIndex());
		accounts.updateGroupContent();
		String gn = ((String)list.getSelectedValue()).substring(1);
		groupName.setText(gn);
		applyButton.setEnabled(true);
		getRootPane().setDefaultButton(removeGroupButton);
	}
	
	private void selectAllGroup()
	{
		groupList.removeListSelectionListener(this);
		groupList.setSelectedIndex(0);
		groupList.ensureIndexIsVisible(0);
		groupList.addListSelectionListener(this);
		groupName.setText("");
		applyButton.setEnabled(false);
	}

	@Override
	public void focusGained(FocusEvent e)
	{
		if (groupName.getText().isEmpty())
			getRootPane().setDefaultButton(addGroupButton);
		else
			getRootPane().setDefaultButton(applyButton);
	}
	@Override
	public void focusLost(FocusEvent e)
	{
		getRootPane().setDefaultButton(null);
	}
	
	class GroupTransferHandler extends TransferHandler
	{
		public boolean canImport(TransferHandler.TransferSupport support)
		{
			// we'll only support drops (not clipboard paste)
			if (!support.isDrop()) return false;
			// we only import Strings
			if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) return false;
			// fetch the drop location
			JList.DropLocation dl = (JList.DropLocation)support.getDropLocation();
			int i = dl.getIndex();
			// we don't support <all> group
			if (i <= 0) return false;
			return true;
		}
		
		public boolean importData(TransferHandler.TransferSupport support)
		{
			return true;
		}
	}

}
