/**
 * (c) 2015 dmulloy2
 */
package net.dmulloy2.swornpermissions.vault;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.ServicePriority;

/**
 * @author dmulloy2
 */

public class VaultHandler
{
	public static void setupIntegration(SwornPermissions plugin)
	{
		SwornPermissionsVault perms = new SwornPermissionsVault(plugin);
		plugin.getServer().getServicesManager().register(Permission.class, perms, plugin, ServicePriority.Highest);

		SwornChatVault chat = new SwornChatVault(plugin, perms);
		plugin.getServer().getServicesManager().register(Chat.class, chat, plugin, ServicePriority.Highest);
	}
}