package escort.plugins.server.game;

import org.bukkit.entity.Player;

public class GamePlayer {

	private Player p;
	private PlayerState state = PlayerState.LOBBY;
	private String pClass = "warrior";
	private boolean editing = false;

	public GamePlayer(Player player) {
		p = player;
	}

	public Player getPlayer() { return p; }

	public String getName() { return p.getName(); }

	public PlayerState getState() { return state; }

	public String getPClass() { return pClass; }

	public boolean isEditing() { return editing; }

	public void setState(PlayerState s) { state = s; }

	public void setPClass(String s) { pClass = s; }

	public void setEditing(boolean yesorno) { editing = yesorno; }
}
