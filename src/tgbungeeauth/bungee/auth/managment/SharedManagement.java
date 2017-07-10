package tgbungeeauth.bungee.auth.managment;

import java.io.IOException;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import tgbungeeauth.bungee.TGBungeeAuthBungee;
import tgbungeeauth.bungee.auth.db.PlayerAuth;
import tgbungeeauth.bungee.config.Messages;

public class SharedManagement {

	public static void finish(ProxiedPlayer player, String hash, boolean isRegister) throws IOException {
		TGBungeeAuthBungee plugin = TGBungeeAuthBungee.getInstance();

		PlayerAuth auth = new PlayerAuth(player.getName().toLowerCase(), player.getName(), hash, player.getAddress().getHostString(), System.currentTimeMillis());
		if (isRegister) {
			plugin.getAuthDatabase().saveAuth(auth);
		} else {
			plugin.getAuthDatabase().updateSession(auth);
		}

		player.sendMessage(new TextComponent(isRegister ? Messages.registerSuccess : Messages.loginSuccess));

		plugin.finishAuth(player);
	}

}
