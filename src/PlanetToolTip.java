import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JToolTip;

/**
 * @author Angor
 *
 */
@SuppressWarnings("serial")
public class PlanetToolTip extends JToolTip
{

	private StringBuffer sb = new StringBuffer();
	private String defaultName = null;
	private String name = null;
	private String geology = null;
	private String natality = null;
	private String owner = null;

	public PlanetToolTip()
	{
		setLayout(null);
	}

	public void setPlanet(Planet p)
	{
		natality = null;
		this.owner = null;
		defaultName = p.getDefaultName();
		name = p.name();
		Dimension d = new Dimension();
		FontMetrics m = getFontMetrics(getFont());
		d.width = m.stringWidth(defaultName) + 6;
		int cnt = 2;
		if (defaultName.equals(name)) name = null;
		if (name != null)
		{
			cnt++;
			d.width = Math.max(d.width, m.stringWidth(name) + 6);
		}
		sb.setLength(0);
		if (!p.isExplored())
		{
			sb.append("S: ");
			int surf = p.img_surf() * 10;
			if (surf <= 0) surf = 1; 
			sb.append(surf);
			sb.append('-');
			sb.append(surf + ((surf == 1) ? 8 : 9));
			geology = sb.toString();
		}
		else
		{
			sb.append("O:");
			sb.append(p.organics());
			sb.append("; E:");
			sb.append(p.energy());
			sb.append("; M:");
			sb.append(p.minerals());
			sb.append("; T:");
			sb.append(p.temperature());
			sb.append("; S:");
			sb.append(p.surface());
			geology = sb.toString();
			sb.setLength(0);
			sb.append("Natality: ");
			sb.append(Math.round(p.natality() * 100.0F) / 100.0F);
			cnt++;
			natality = sb.toString();
			d.width = Math.max(d.width, m.stringWidth(natality) + 6);
		}
		d.width = Math.max(d.width, m.stringWidth(geology) + 6);
		Player owner = Player.getPlayer(p.ownerId());
		if (owner != null)
		{
			sb.setLength(0);
			sb.append("Owner: ");
			sb.append(owner.name());
			this.owner = sb.toString();
			cnt++;
			d.width = Math.max(d.width, m.stringWidth(this.owner) + 6);
		}
		d.height = cnt * (m.getHeight()) + 5;
		setPreferredSize(d);
	}

	public void paint(Graphics g)
	{
		if (defaultName == null && name == null && geology == null && natality == null && owner == null) return;
		super.paint(g);
		int h = getFontMetrics(getFont()).getHeight();
		int y = h;
		g.drawString(defaultName, 3, y); y += h;
		if (name != null) { g.drawString(name, 3, y); y += h; }
		g.drawString(geology, 3, y); y += h;
		if (natality != null) { g.drawString(natality, 3, y); y += h; }
		if (owner != null) g.drawString(owner, 3, y);
	}

}
