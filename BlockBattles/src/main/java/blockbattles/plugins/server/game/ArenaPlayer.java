package blockbattles.plugins.server.game;

import org.bukkit.entity.Player;

public class ArenaPlayer {

	private Player p;
	private PlayerStage stage = PlayerStage.LOBBY;
	private String pClass = "warrior";
	private Arena a = null;
	private boolean editing = false;

	public ArenaPlayer(Player player) {
		p = player;
	}

	public Player getPlayer() { return p; }

	public PlayerStage getStage() { return stage; }

	public String getPClass() { return pClass; }

	public Arena getArena() { return a; }

	public boolean isEditing() { return editing; }

	public void setStage(PlayerStage s) { stage = s; }

	public void setPClass(String s) { pClass = s; }

	public void setArena(Arena arena) { a = arena; }

	public void setEditing(boolean yesorno) { editing = yesorno; }
}
