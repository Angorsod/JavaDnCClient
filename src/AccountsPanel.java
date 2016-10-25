import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
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
public class AccountsPanel extends JPanel implements ActionListener, ListSelectionListener, FocusListener
{

	public AccountsPanel()
	{
		setLayout(null);
		setBounds(320, 5, 305, 434);
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Accounts"));
		removeAccountButton = new JButton("Remove");
		removeAccountButton.setBounds(160, 370, 140, 20);
		removeAccountButton.setEnabled(false);
		removeAccountButton.addActionListener(this);
		add(removeAccountButton);
		accountListModel = new DefaultListModel();
		groupListModel = new DefaultComboBoxModel();
		groupList = null;
		//updateGroupContent();
		cellRenderer = new AccountListRenderer();
		accountList = new JList(accountListModel);
		accountList.setCellRenderer(cellRenderer);
		accountList.setDragEnabled(true);
		accountList.setTransferHandler(new AccountTransferHandler());
		accountList.addListSelectionListener(this);
		accountList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane = new JScrollPane(accountList);
		scrollPane.setBounds(10, 20, 285, 280);
		scrollPane.setBorder(BorderFactory.createLineBorder(Color.black));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		add(scrollPane);
		addAccountButton = new JButton("Add");
		addAccountButton.setBounds(160, 310, 140, 20);
		addAccountButton.addActionListener(this);
		add(addAccountButton);
		loginField = new JTextField();
		loginField.setBounds(10, 310, 145, 21);
		loginField.addFocusListener(this);
		loginField.setTransferHandler(null);
		add(loginField);
		passwdField = new JPasswordField();
		passwdField.setEchoChar('*');
		passwdField.setBounds(10, 340, 145, 21);
		passwdField.addFocusListener(this);
		passwdField.setTransferHandler(null);
		add(passwdField);
		unmaskButton = new JButton("Unmask pass");
		unmaskButton.setBounds(160, 340, 140, 20);
		unmaskButton.addActionListener(this);
		add(unmaskButton);
		groupList = new JComboBox(groupListModel);
		groupList.setBounds(10, 400, 145, 21);
		add(groupList);
		toGroupButton = new JButton("Add to group");
		toGroupButton.setBounds(160, 400, 140, 20);
		toGroupButton.addActionListener(this);
		toGroupButton.setEnabled(groupList.getSelectedIndex() > 0);
		add(toGroupButton);
		applyButton = new JButton("Apply");
		applyButton.setBounds(10, 370, 145, 20);
		applyButton.addActionListener(this);
		add(applyButton);
		applyButton.setEnabled(false);
	}

	public void updateGroupContent()
	{
		accountListModel.clear();
		Account[] accounts = Account.getAccounts();
		if (accounts == null) return;
		for (int i = 0; i < accounts.length; i++)
		{
			if (accounts[i] == null) continue;
			accountListModel.addElement(accounts[i]);
		}
		if (accountListModel.getSize() > 0) removeAccountButton.setEnabled(true);
	}
	public void updateGroupList()
	{
		groupListModel.removeAllElements();
		groupListModel.addElement("Select group...");
		groupListModel.addElement("<none>");
		int[] groups = Account.getGroups();
		if (groups == null) return;
		for (int i = 0; i < groups.length; i++)
		{
			String gn = Account.getGroupName(groups[i]);
			if (gn.isEmpty()) continue;
			groupListModel.addElement(gn);
		}
	}
	public void selectGroup(int selectIndex)
	{
		//currentGroup = selectIndex;
		if (selectIndex > 0 && selectIndex < groupListModel.getSize())
			groupList.setSelectedIndex(selectIndex);
	}

	private DefaultListModel accountListModel;
	private JList accountList;
	private JButton removeAccountButton;
	private JTextField loginField;
	private JPasswordField passwdField;
	private JButton addAccountButton;
	private JButton unmaskButton;
	private JComboBox groupList;
	private JButton toGroupButton;
	private JButton applyButton;
	private DefaultComboBoxModel groupListModel;
	private ListCellRenderer cellRenderer;
	//private int currentGroup;

	@Override
	public void actionPerformed(ActionEvent event)
	{
		Object src = event.getSource();
		if (src == unmaskButton)
		{
			if (passwdField.getEchoChar() != 0)
			{
				passwdField.setEchoChar((char)0);
				unmaskButton.setText("Mask pass");
			}
			else
			{
				passwdField.setEchoChar('*');
				unmaskButton.setText("Unmask pass");
			}
		}
		else if (src == addAccountButton)
		{
			String passwd = new String(passwdField.getPassword());
			String login = loginField.getText();
			Account account = Account.getAccountByLogin(login);
			// User did not type in a unique login...
			if (login.isEmpty() || account != null)
			{
				Toolkit.getDefaultToolkit().beep();
				loginField.requestFocusInWindow();
				loginField.selectAll();
				return;
			}
			// Empty password
			if (passwd.isEmpty())
			{
				Toolkit.getDefaultToolkit().beep();
				passwdField.requestFocusInWindow();
				return;
			}
			if (!Account.addAccount(login, passwd, -1))
			{
				Toolkit.getDefaultToolkit().beep();
				return;
			}
			updateGroupContent();
		}
		else if (src == applyButton)
		{
			Account a = (Account)accountList.getSelectedValue();
			a.login(loginField.getText());
			a.passwd(new String(passwdField.getPassword()));
			Account.updateAccount(a);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent event)
	{
		JList list = (JList)event.getSource();
		int[] indices = list.getSelectedIndices();
		if (indices.length > 1)
		{
			loginField.setText("");
			passwdField.setText("");
			applyButton.setEnabled(false);
			return;
		}
		Account acc = (Account)list.getSelectedValue();
		loginField.setText(acc.login());
		passwdField.setText(acc.passwd());
		applyButton.setEnabled(true);
		Account.setCurrent(acc);
	}

	@Override
	public void focusGained(FocusEvent event)
	{
	}

	@Override
	public void focusLost(FocusEvent event)
	{
	}

	class AccountListRenderer extends JLabel implements ListCellRenderer
	{
		AccountListRenderer()
		{
			setOpaque(true);
			setIcon(new ImageIcon());
			setIconTextGap(10);
		}

		@Override
		public Component getListCellRendererComponent(
			JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus)
		{
			setText(value.toString());

			Color background;
			Color foreground;

			// check if this cell represents the current DnD drop location
			JList.DropLocation dropLocation = list.getDropLocation();
			if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index)
			{
				background = list.getSelectionBackground();
				foreground = list.getSelectionForeground();
			}
			else if (isSelected) // check if this cell is selected
			{
				background = list.getSelectionBackground();
				foreground = list.getSelectionForeground();
			}
			else // unselected, and not the DnD drop location
			{
				background = list.getBackground();
				foreground = list.getForeground();
			};

			setBackground(background);
			setForeground(foreground);

			return this;
		}
	}

	class AccountTransferHandler extends TransferHandler
	{
		protected Transferable createTransferable(JComponent c)
		{
			return new StringSelection(exportString(c));
		}

		public int getSourceActions(JComponent c)
		{
			return TransferHandler.MOVE;
		}

		protected void exportDone(JComponent c, Transferable data, int action)
		{
		}

		// Bundle up the selected items in the list
		// as a single string, for export.
		protected String exportString(JComponent c)
		{
			JList list = (JList)c;
			Object[] values = list.getSelectedValues();

			StringBuffer buff = new StringBuffer();

			for (int i = 0; i < values.length; i++)
			{
				Account val = (Account)values[i];
				if (val != null)
				{
					buff.append(val.accountId());
					if (i != values.length - 1)
						buff.append(';');
				}
			}

			return buff.toString();
		}
	}
	
}
