/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.shadowperms.handlers;

import java.util.Map.Entry;

import net.dmulloy2.shadowperms.ShadowPerms;
import net.dmulloy2.shadowperms.types.User;
import net.dmulloy2.types.MyMaterial;
import net.dmulloy2.types.Reloadable;
import net.dmulloy2.util.FormatUtil;
import net.dmulloy2.util.MaterialUtil;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author dmulloy2
 */

public class AntiItemHandler implements Listener, Reloadable
{
	private boolean enabled;
	private int maxEnchantmentLevel;
	private boolean regulateEnchantments;

	private final ShadowPerms plugin;
	public AntiItemHandler(ShadowPerms plugin)
	{
		this.plugin = plugin;
		this.reload();

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		User user = plugin.getPermissionHandler().getUser(player);
		if (user == null)
			return;

		switch (event.getAction())
		{
			case RIGHT_CLICK_BLOCK:
			case RIGHT_CLICK_AIR:
			{
				ItemStack item = event.getItem();
				if (item != null)
				{
					MyMaterial mat = new MyMaterial(item.getType());
					if (enabled && ! user.canUse("-antiitem.item.", mat))
					{
						event.setCancelled(true);
					}

					if (regulateEnchantments && ! item.getEnchantments().isEmpty())
					{
						for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet())
						{
							Enchantment ench = entry.getKey();
							int level = entry.getValue();
							if (level > maxEnchantmentLevel)
							{
								item.removeEnchantment(ench);
								regulatedEnchantment(player, ench, level, item, "high");
							}
							else if (level < 0)
							{
								item.removeEnchantment(ench);
								regulatedEnchantment(player, ench, level, item, "low");
							}
						}
					}
				}

				Block clicked = event.getClickedBlock();
				if (clicked != null)
				{
					MyMaterial mat = new MyMaterial(clicked.getType(), clicked.getState().getData());
					if (enabled && ! user.canUse("-antiitem.item.", mat))
					{
						event.setCancelled(true);
					}
				}

				break;
			}
			case LEFT_CLICK_AIR:
			case LEFT_CLICK_BLOCK:
			{
				ItemStack item = event.getItem();
				if (item != null)
				{
					MyMaterial mat = new MyMaterial(item.getType());
					if (enabled && ! user.canUse("-antiitem.leftclick.", mat))
					{
						event.setCancelled(true);
					}
				}

				break;
			}
			default:
				break;
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		if (player == null)
			return;

		User user = plugin.getPermissionHandler().getUser(player);
		if (user == null)
			return;

		Block placed = event.getBlockPlaced();
		if (placed != null)
		{
			MyMaterial mat = new MyMaterial(placed.getType(), placed.getState().getData());
			if (enabled && ! user.canUse("-antiitem.place.", mat))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if (player == null)
			return;

		User user = plugin.getPermissionHandler().getUser(player);
		if (user == null)
			return;

		Block placed = event.getBlock();
		if (placed != null)
		{
			MyMaterial mat = new MyMaterial(placed.getType(), placed.getState().getData());
			if (enabled && ! user.canUse("-antiitem.break.", mat))
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if (event.getPlayer() instanceof Player)
		{
			Player player = (Player) event.getPlayer();
			PlayerInventory inv = player.getInventory();

			User user = plugin.getPermissionHandler().getUser(player);

			if (enabled && user != null)
			{
				ItemStack helmet = inv.getHelmet();
				if (helmet != null && ! user.canUse("-antiitem.item.", new MyMaterial(helmet.getType())))
				{
					blockedItem(player, helmet);
					inv.setHelmet(null);
				}

				ItemStack chest = inv.getChestplate();
				if (chest != null && ! user.canUse("-antiitem.item.", new MyMaterial(chest.getType())))
				{
					blockedItem(player, chest);
					inv.setChestplate(null);
				}

				ItemStack legs = inv.getLeggings();
				if (legs != null && ! user.canUse("-antiitem.item.", new MyMaterial(legs.getType())))
				{
					blockedItem(player, legs);
					inv.setLeggings(null);
				}

				ItemStack boots = inv.getBoots();
				if (boots != null && ! user.canUse("-antiitem.item.", new MyMaterial(boots.getType())))
				{
					blockedItem(player, boots);
					inv.setBoots(null);
				}
			}

			for (ItemStack item : inv.getContents())
			{
				if (item != null && item.getType() != Material.AIR)
				{
					if (enabled && user != null && ! user.canUse("-antiitem.item.", new MyMaterial(item.getType(), item.getData())))
					{
						blockedItem(player, item);
						inv.remove(item);
						continue;
					}

					if (regulateEnchantments && ! item.getEnchantments().isEmpty())
					{
						for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet())
						{
							Enchantment ench = entry.getKey();
							int level = entry.getValue();
							if (level > maxEnchantmentLevel)
							{
								item.removeEnchantment(ench);
								regulatedEnchantment(player, ench, level, item, "high");
							}
							else if (level <= 0)
							{
								item.removeEnchantment(ench);
								regulatedEnchantment(player, ench, level, item, "low");
							}
						}
					}
				}
			}

			for (ItemStack armor : inv.getArmorContents())
			{
				if (armor != null && armor.getType() != Material.AIR)
				{
					if (regulateEnchantments && ! armor.getEnchantments().isEmpty())
					{
						for (Entry<Enchantment, Integer> entry : armor.getEnchantments().entrySet())
						{
							Enchantment ench = entry.getKey();
							int level = entry.getValue();
							if (level > maxEnchantmentLevel)
							{
								armor.removeEnchantment(ench);
								regulatedEnchantment(player, ench, level, armor, "high");
							}
							else if (level <= 0)
							{
								armor.removeEnchantment(ench);
								regulatedEnchantment(player, ench, level, armor, "low");
							}
						}
					}
				}
			}
		}
	}

	private final void regulatedEnchantment(Player player, Enchantment ench, int level, ItemStack item, String reason)
	{
		player.sendMessage(plugin.getPrefix() + FormatUtil.format(
				"&4Enchantment &c{0}&4:&c{1} &4has been removed from your &c{2} &4because it was too &c{3}&4.",
				FormatUtil.getFriendlyName(ench.getName()), level, MaterialUtil.getName(item), reason));
	}

	private final void blockedItem(Player player, ItemStack item)
	{
		player.sendMessage(plugin.getPrefix() + FormatUtil.format(
				"&4Item &c{0} &4is not allowed and has been removed from your inventory.",
				MaterialUtil.getName(item)));
	}

	@Override
	public void reload()
	{
		this.enabled = plugin.getConfig().getBoolean("antiItem.enabled", false);
		this.maxEnchantmentLevel = plugin.getConfig().getInt("antiItem.maxEnchantmentLevel", 25);
		this.regulateEnchantments = plugin.getConfig().getBoolean("antiItem.regulateEnchantments", false);
	}
}