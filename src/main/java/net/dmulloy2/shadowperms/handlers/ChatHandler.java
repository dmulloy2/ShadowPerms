/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.handlers;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.Permission;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.swornapi.types.Reloadable;
import net.dmulloy2.swornapi.util.FormatUtil;

import org.bukkit.entity.Player;

import lombok.Getter;

/**
 * @author dmulloy2
 */

public class ChatHandler implements Reloadable
{
	private @Getter boolean setDisplay;
	private boolean formatChat;

	private String chatFormat;

	private final ShadowPerms plugin;
	public ChatHandler(ShadowPerms plugin)
	{
		this.plugin = plugin;
		this.reload();
	}

	/**
	 * Parses a given chat message, replacing defined variables.
	 *
	 * @param player Player chatting
	 * @param message Message sent
	 * @return Formatted chat message
	 */
	public final String formatChat(Player player, String message)
	{
		if (! formatChat)
			return null;

		User user = plugin.getPermissionHandler().getUser(player);
		if (user == null)
			return null;

		// Replace Variables
		String format = getChatFormat(user)
				.replace("{prefix}", user.getPrefix())
				.replace("{name}", user.getDisplayName())
				.replace("{suffix}", user.getSuffix())
				.replace("{world}", player.getWorld().getName());

		// Chat color
		if (user.hasOption("chatColor"))
			format = format.replace(":", user.getOption("chatColor") + ":");

		// Escape pesky % characters
		message = message.replace("%", "%%");

		// Disallow fancy chat formatting if they don't have permission
		if (! plugin.getPermissionHandler().hasPermission(player, Permission.CHAT_COLOR))
			message = message.replaceAll("(&([a-fA-F0-9]))", "");
		if (! plugin.getPermissionHandler().hasPermission(player, Permission.CHAT_FORMATTING))
			message = message.replaceAll("(&([kKl-oL-OrR]))", "");
		if (! plugin.getPermissionHandler().hasPermission(player, Permission.CHAT_RAINBOW))
			message = message.replaceAll("(&([zZ]))", "");

		// Insert message, format colors
		return FormatUtil.replaceColors(format.replace("{message}", message));
	}

	// Personal chat format #EasterEgg
	private String getChatFormat(User user)
	{
		if (user.hasOption("chatFormat"))
			return user.getOption("chatFormat").toString();

		return chatFormat;
	}

	@Override
	public void reload()
	{
		this.setDisplay = plugin.getConfig().getBoolean("setDisplay", true);
		this.formatChat = plugin.getConfig().getBoolean("formatChat", true);
		this.chatFormat = plugin.getConfig().getString("chatFormat");
	}
}