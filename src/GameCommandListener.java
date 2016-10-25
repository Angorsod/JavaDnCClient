
/**
 * @author Angor
 *
 */
public interface GameCommandListener
{
	public void currentUrl(String url);
	public void skymapLoaded(int x, int y);
	public void planetOpened(int x, int y);
	public void skymapUpdated(int x, int y);
	public void currentPlayerId(int id);
	public void playerOpened(int id);
	public void playerInfo(Player p);
	public void raceOpened(int id);
	public void raceInfo(Race r);
	public void overviewOpened();
	public void communicatorOpened();
	public void designerOpened();
	public void settingsOpened();
	public void politicsOpened();
	public void currentTurn(int turn);
	public void homeWorld(int x, int y);
	public void status(int stat);
	public void entered();
	public void ownerUpdated(int x, int y);
}
