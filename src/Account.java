import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JPanel;
import SQLite.TableResult;


/**
 * @author Angor
 *
 */

public class Account
{

	private static final String GROUP_EXISTANCE_QUERY = 
		"SELECT id, name FROM Groups WHERE name LIKE '%q';";
	private static final String GET_GROUP_QUERY = 
		"SELECT id, name FROM Groups WHERE id = '%q';";
	private static final String GET_GROUPS_QUERY = 
		"SELECT id, name FROM Groups;";
	private static final String UPDATE_GROUP_QUERY = 
		"UPDATE Groups SET name = '%q' WHERE id = '%q';";
	private static final String INSERT_GROUP_QUERY = 
		"INSERT INTO Groups (name) VALUES ('%q');";
	private static final String DELETE_GROUP_QUERY = 
		"DELETE FROM Groups WHERE id = '%q';";
	private static final String ACCOUNT_EXISTANCE_QUERY = 
		"SELECT id, login, passwd, player_id FROM Accounts WHERE login LIKE '%q';";
	private static final String GET_ACCOUNT_QUERY = 
		"SELECT id, login, passwd, player_id FROM Accounts WHERE id = '%q';";
	private static final String GET_ACCOUNTS_QUERY = 
		"SELECT id, login, passwd, player_id FROM Accounts;";
	private static final String INSERT_ACCOUNT_QUERY = 
		"INSERT INTO Accounts (login, passwd, player_id) VALUES ('%q', '%q', '%q');";
	private static final String UPDATE_ACCOUNT_QUERY = 
		"UPDATE Accounts SET login = '%q', passwd = '%q', player_id = '%q' WHERE id = '%q';";
	private static final String DELETE_ACCOUNT_QUERY = 
		"DELETE FROM Accounts WHERE id = '%q';";

	private String login;
	private String passwd;
	private int playerId;
	private int accountId;

	public String login() { return login; }
	public void login(String login) { this.login = login; }
	public String passwd() { return passwd; }
	public void passwd(String passwd) { this.passwd = passwd; }
	public int accountId() { return accountId; }
	public Player player() { return Player.getPlayer(playerId); }

	private static Account current = null;
	public static Account getCurrent() { return current; }
	public static void setCurrent(Account acc) { current = acc; }

	private static int status = GameDataSink.LOGGED_OFF;
	public static int status() { return status; }
	public static void status(int status) { Account.status = status; }

	public static Account getAccountByLogin(String login)
	{
		Account acc = null;
		try
		{
			TableResult tr = DnC.db().get_table(ACCOUNT_EXISTANCE_QUERY, 1, new String[] { login });
			String[] as = (String[])tr.rows.get(0);
			if (!login.equals(as[1])) return acc;
			acc = new Account();
			acc.login = as[1];
			acc.passwd = as[2];
			acc.accountId = Integer.parseInt(as[0]);
			acc.playerId = Integer.parseInt(as[3]);
		} catch (Exception e) { acc = null; }
		return acc;
	}
	public static Account getAccountByPlayer(String playerName)
	{
		return null;
	}
	public static Account getAccountByPlayerId(int playerId)
	{
		return null;
	}
	public static Account getAccount(int id)
	{
		Account acc = null;
		try
		{
			TableResult tr = DnC.db().get_table(GET_ACCOUNT_QUERY, 1, new String[] { Integer.toString(id) });
			String[] as = (String[])tr.rows.get(0);
			acc = new Account();
			acc.login = as[1];
			acc.passwd = as[2];
			acc.accountId = Integer.parseInt(as[0]);
			acc.playerId = Integer.parseInt(as[3]);
		} catch (Exception e) { acc = null; }
		return acc;
	}
	public static boolean addAccount(String login, String passwd, int pid)
	{
		if (getAccountByLogin(login) != null) return false;
		try
		{
			DnC.db().get_table(INSERT_ACCOUNT_QUERY, 0, new String[] { login, passwd, Integer.toString(pid) });
		} catch (Exception e) { return false; }
		return true;
	}
	public static boolean removeAccount(int id)
	{
		try
		{
			DnC.db().get_table(DELETE_ACCOUNT_QUERY, 0, new String[] { Integer.toString(id) });
		} catch (Exception e) { return false; }
		return true;
	}
	public static boolean updateAccount(Account account)
	{
		try
		{
			DnC.db().get_table(UPDATE_ACCOUNT_QUERY, 0, 
				new String[] { account.login, account.passwd, Integer.toString(account.playerId), Integer.toString(account.accountId) });
		} catch (Exception e) { return false; }
		return true;
	}

	public static int[] getGroups()
	{
		int[] groups = null;
		try
		{
			TableResult tr = DnC.db().get_table(GET_GROUPS_QUERY);
			int nrows = tr.nrows;
			if (nrows <= 0) return groups;
			groups = new int[nrows];
			for (int i = 0; i < nrows; i++)
			{
				try
				{
					groups[i] = Integer.parseInt(((String[])tr.rows.get(i))[0]);
				} catch (Exception e) { groups[i] = -1; }
			}
		} catch (Exception e) {}
		return groups;
	}
	public static String getGroupName(int id)
	{
		String gname = "";
		if (id < 0) return gname;
		try
		{
			TableResult tr = DnC.db().get_table(GET_GROUP_QUERY, 1, new String[] { Integer.toString(id) });
			String[] gs = (String[])tr.rows.get(0);
			gname = gs[1];
		} catch (Exception e) {}
		return gname;
	}
	public static int getGroupId(String name)
	{
		int gid = -1;
		try
		{
			TableResult tr = DnC.db().get_table(GROUP_EXISTANCE_QUERY, 1, new String[] { name });
			String[] gs = (String[])tr.rows.get(0);
			if (!name.equals(gs[1])) return gid;
			gid = Integer.parseInt(gs[0]);
		} catch (Exception e) {}
		return gid;
	}
	public static boolean addGroup(String name)
	{
		if (getGroupId(name) >= 0) return false;
		try
		{
			DnC.db().get_table(INSERT_GROUP_QUERY, 0, new String[] { name });
		} catch (Exception e) { return false; }
		return true;
	}
	public static boolean removeGroup(String name)
	{
		int id = getGroupId(name);
		if (id < 0) return false;
		try
		{
			DnC.db().get_table(DELETE_GROUP_QUERY, 0, new String[] { Integer.toString(id) });
		} catch (Exception e) { return false; }
		return true;
	}
	public static boolean updateGroup(String name, String newName)
	{
		if (getGroupId(newName) >= 0) return false;
		int id = getGroupId(name);
		if (id < 0) return false;
		try
		{
			DnC.db().get_table(UPDATE_GROUP_QUERY, 0, new String[] { newName, Integer.toString(id) });
		} catch (Exception e) { return false; }
		return true;
	}

	public static Account[] getAccounts()
	{
		accounts = null;
		try
		{
			TableResult tr = DnC.db().get_table(GET_ACCOUNTS_QUERY);
			int nrows = tr.nrows;
			if (nrows <= 0) return accounts;
			accounts = new Account[nrows];
			for (int i = 0; i < nrows; i++)
			{
				try
				{
					String[] as = (String[])tr.rows.get(i);
					Account acc = new Account();
					acc.login = as[1];
					acc.passwd = as[2];
					acc.accountId = Integer.parseInt(as[0]);
					acc.playerId = Integer.parseInt(as[3]);
					accounts[i] = acc;
				} catch (Exception e) { accounts[i] = null; }
			}
		} catch (Exception e) {}
		return accounts;
	}
	public static Account[] getAccountsByGroupId(int id)
	{
		Account[] accounts = null;
		return accounts;
	}
	public static Account[] getAccountsByGroupName(String name)
	{
		Account[] accounts = null;
		return accounts;
	}

	private static Account[] accounts = null;
	public static Account[] accounts() { return (accounts != null) ? accounts : getAccounts(); }
	private static JPanel groupsPane = null;
	private static JPanel accountsPane = null;
	public static int openAccountsDialog()
	{
		Frame frame = DnC.mainFrame();
		JDialog dialog = new JDialog(frame, "Accounts", true);
		dialog.setSize(640, 480);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(frame);
		dialog.setLayout(null);
		accountsPane = new AccountsPanel();
		dialog.add(accountsPane);
		groupsPane = new GroupsPanel((AccountsPanel)accountsPane);
		dialog.add(groupsPane);
		dialog.setVisible(true);
		dialog.dispose();
		return 0;
	}

	private Account()
	{
		//login = "Test";
		//passwd = "test";
		//playerId = -1;
	}

	@Override public String toString()
	{
		Player p = player();
		return login + " (" + ((p == null) ? "?" : p.name()) + ')';
	}

}
