/**
 * SwornPermissions - comprehensive permission, chat, and world management system
 * Copyright (C) 2014 dmulloy2
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.dmulloy2.swornpermissions;

import lombok.Getter;
import net.dmulloy2.swornpermissions.commands.CmdHelp;
import net.dmulloy2.swornpermissions.commands.CmdReload;
import net.dmulloy2.swornpermissions.commands.CmdVersion;
import net.dmulloy2.swornpermissions.commands.group.CmdGroup;
import net.dmulloy2.swornpermissions.commands.user.CmdUser;
import net.dmulloy2.swornpermissions.conversion.ConversionHandler;
import net.dmulloy2.swornpermissions.handlers.ChatHandler;
import net.dmulloy2.swornpermissions.handlers.CommandHandler;
import net.dmulloy2.swornpermissions.handlers.DataHandler;
import net.dmulloy2.swornpermissions.handlers.LogHandler;
import net.dmulloy2.swornpermissions.handlers.PermissionHandler;
import net.dmulloy2.swornpermissions.listeners.ChatListener;
import net.dmulloy2.swornpermissions.listeners.PlayerListener;
import net.dmulloy2.swornpermissions.listeners.WorldListener;
import net.dmulloy2.swornpermissions.types.Reloadable;
import net.dmulloy2.swornpermissions.util.FormatUtil;
import net.dmulloy2.swornpermissions.vault.SwornChatVault;
import net.dmulloy2.swornpermissions.vault.SwornPermissionsVault;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author dmulloy2
 */

public class SwornPermissions extends JavaPlugin implements Reloadable
{
	private @Getter PermissionHandler permissionHandler;
	private @Getter CommandHandler commandHandler;
	private @Getter ChatHandler chatHandler;
	private @Getter DataHandler dataHandler;
	private @Getter LogHandler logHandler;

	private @Getter boolean disabling;
	
	private @Getter String prefix = FormatUtil.format("&3[&eSwornPerms&3]&e ");

	@Override
	public void onEnable()
	{
		long start = System.currentTimeMillis();

		disabling = false;

		/** Register Required Handlers **/
		logHandler = new LogHandler(this);

		/** Conversion **/
		if (! getDataFolder().exists())
		{
			// If the data folder doesn't exist, this is the first run.
			// Attempt to convert from other permission systems.
			new ConversionHandler(this).attemptConversion();
		}

		/** Configuration **/
		saveDefaultConfig();
		reloadConfig();

		/** Define Other Handlers **/
		dataHandler = new DataHandler(this);
		chatHandler = new ChatHandler(this);
		commandHandler = new CommandHandler(this);

		permissionHandler = new PermissionHandler(this);
		permissionHandler.reload(); // Load

		/** Register Commands **/
		commandHandler.setCommandPrefix("swornperms");
		commandHandler.registerPrefixedCommand(new CmdGroup(this));
		commandHandler.registerPrefixedCommand(new CmdHelp(this));
		commandHandler.registerPrefixedCommand(new CmdReload(this));
		commandHandler.registerPrefixedCommand(new CmdUser(this));
		commandHandler.registerPrefixedCommand(new CmdVersion(this));

		/** Register Listeners **/
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new WorldListener(this), this);

		/** Register Permissions and Chat for Vault **/
		if (pm.isPluginEnabled("Vault"))
		{
			SwornPermissionsVault perms = new SwornPermissionsVault(this);
			getServer().getServicesManager().register(Permission.class, perms, this, ServicePriority.Highest);

			SwornChatVault chat = new SwornChatVault(this, perms);
			getServer().getServicesManager().register(Chat.class, chat, this, ServicePriority.High);
		}

		logHandler.log("{0} has been enabled ({1}ms)", getDescription().getFullName(), System.currentTimeMillis() - start);
	}

	@Override
	public void onDisable()
	{
		long start = System.currentTimeMillis();

		disabling = true;

		getServer().getServicesManager().unregisterAll(this);
		getServer().getScheduler().cancelTasks(this);

		dataHandler.save();

		logHandler.log("{0} has been disabled ({1}ms)", getDescription().getFullName(), System.currentTimeMillis() - start);
	}

	@Override
	public void reload()
	{
		reloadConfig();

		chatHandler.reload();
		dataHandler.reload();
		permissionHandler.reload();
	}
}