/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.handlers;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.swornpermissions.types.Permission;
import net.dmulloy2.swornpermissions.types.Reloadable;
import net.dmulloy2.swornpermissions.util.FormatUtil;

import org.bukkit.entity.Player;

/**
 * @author dmulloy2
 */

public class ChatHandler implements Reloadable
{
	private boolean formatChat;
	private String chatFormat;

	private final SwornPermissions plugin;
	public ChatHandler(SwornPermissions plugin)
	{
		this.plugin = plugin;
		this.reload();
	}

	/**
	 * Parses a given chat message, replacing defined variables.
	 *
	 * @param player
	 *        - Player chatting
	 * @param message
	 *        - Message sent
	 * @return Formatted chat message
	 */
	public final String parseChatMessage(Player player, String message)
	{
		if (! formatChat)
			return message;

		User user = plugin.getPermissionHandler().getUser(player);

		// Characters that mess with chat
		message = message.replace("%", "%%");

		// Define Available Variables
		String prefix = user.getPrefix();
		String suffix = user.getSuffix();
		String name = "&f" + user.getDisplayName();

		// Replace Variables
		String format = getChatFormat(user);
		format = format.replace("{prefix}", prefix);
		format = format.replace("{name}", name);
		format = format.replace("{suffix}", suffix);

		// Permissions
		if (! plugin.getPermissionHandler().hasPermission(player, Permission.CHAT_COLOR))
			message = message.replaceAll("(&([a-fA-F0-9]))", "");
		if (! plugin.getPermissionHandler().hasPermission(player, Permission.CHAT_FORMATTING))
			message = message.replaceAll("(&([kKl-oL-OrR]))", "");
		if (! plugin.getPermissionHandler().hasPermission(player, Permission.CHAT_RAINBOW))
			message = message.replaceAll("(&([zZ]))", "");

		// Replace message variable
		format = format.replace("{message}", message);

		// Format colors
		format = FormatUtil.replaceColors(format);
		return format;
	}

	// Personal chat format #EasterEgg
	private String getChatFormat(User user)
	{
		if (user.hasOption("chatFormat"))
			return (String) user.getOption("chatFormat");

		return chatFormat;
	}

	@Override
	public void reload()
	{
		this.formatChat = plugin.getConfig().getBoolean("formatChat", true);
		this.chatFormat = plugin.getConfig().getString("chatFormat");
	}
}