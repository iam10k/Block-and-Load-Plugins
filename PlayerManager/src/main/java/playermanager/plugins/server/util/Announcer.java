package playermanager.plugins.server.util;

import playermanager.plugins.server.main.PlayerManager;

public class Announcer extends Thread
{
    private int msgToShow = 0;
    private PlayerManager plugin;

    public Announcer(PlayerManager pl) {
        plugin = pl;
    }

    public void run()
    {
        if (msgToShow >= plugin.getConfig().getStringList("Announcements.list").size()) {
			msgToShow = 0;
		}
        plugin.Announce(msgToShow);
		msgToShow++;
    }
}