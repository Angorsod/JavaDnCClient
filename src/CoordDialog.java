import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * @author Angor
 *
 */
@SuppressWarnings("serial")
public class CoordDialog extends JDialog implements ActionListener
{

	private JTextField xField;
	private JTextField yField;
	private JButton okBtn;
	private JButton cancelBtn;

	/**
	 * @param frame
	 * @param x
	 * @param y
	 */
	public CoordDialog(Frame frame, String title, int x, int y)
	{
		super(frame, true);
		setTitle(title);
		setResizable(false);
		setLayout(null);
		setSize(240, 130);

		JLabel lbl = new JLabel("Enter planet coordinate");
		lbl.setBounds(35, 10, 170, 20);
		lbl.setHorizontalAlignment(JLabel.CENTER);
		add(lbl);

		xField = new JTextField(4);
		xField.setBounds(35, 35, 80, 21);
		add(xField);
		yField = new JTextField(4);
		yField.setBounds(125, 35, 80, 21);
		add(yField);

		okBtn = new JButton("OK");
		okBtn.setBounds(35, 75, 80, 20);
		okBtn.addActionListener(this);
		add(okBtn);
		cancelBtn = new JButton("Cancel");
		cancelBtn.setBounds(125, 75, 80, 20);
		cancelBtn.addActionListener(this);
		add(cancelBtn);
		getRootPane().setDefaultButton(okBtn);

/*
		//Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent we)
				{
					setVisible(false);
				}
			});
*/

		//Ensure the text field always gets the first focus.
		addComponentListener(new ComponentAdapter()
			{
				public void componentShown(ComponentEvent ce)
				{
					xField.requestFocusInWindow();
				}
			});
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override public void actionPerformed(ActionEvent event)
	{
		if (event.getSource() == okBtn)
		{
			int x = -1, y = -1;
			try
			{
				x = Integer.parseInt(xField.getText());
				y = Integer.parseInt(yField.getText());
			} catch (Exception e) {}
			if (x > 0 && x <= 2000 && y > 0 && y <= 2000)
			{
				DnC.mapPanel().center(x, y);
			}
		}
		setTitle(null);
		setVisible(false);
	}

}
