/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.swornpermissions.handlers;

import java.util.Map.Entry;

import net.dmulloy2.swornpermissions.SwornPermissions;
import net.dmulloy2.swornpermissions.permissions.User;
import net.dmulloy2.types.MyMaterial;
import net.dmulloy2.types.Reloadable;
import net.dmulloy2.util.FormatUtil;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

	private final SwornPermissions plugin;
	public AntiItemHandler(SwornPermissions plugin)
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
								regulatedEnchantment(player, ench, level, item.getType(), "high");
							}
							else if (level < 0)
							{
								item.removeEnchantment(ench);
								regulatedEnchantment(player, ench, level, item.getType(), "low");
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

	@EventHandler(priority = EventPriority.LOW)
	public void onInventoryClose(InventoryCloseEvent event)
	{
		if (event.getPlayer() instanceof Player)
		{
			Player player = (Player) event.getPlayer();
			PlayerInventory inv = player.getInventory();

			User user = plugin.getPermissionHandler().getUser(player);

			if (enabled)
			{
				ItemStack helmet = inv.getHelmet();
				if (helmet != null && ! user.canUse("-antiitem.item.", new MyMaterial(helmet.getType())))
				{
					blockedItem(player, helmet.getType());
					inv.setHelmet(null);
				}
	
				ItemStack chest = inv.getChestplate();
				if (chest != null && ! user.canUse("-antiitem.item.", new MyMaterial(chest.getType())))
				{
					blockedItem(player, chest.getType());
					inv.setChestplate(null);
				}
	
				ItemStack legs = inv.getLeggings();
				if (legs != null && ! user.canUse("-antiitem.item.", new MyMaterial(legs.getType())))
				{
					blockedItem(player, legs.getType());
					inv.setLeggings(null);
				}
	
				ItemStack boots = inv.getBoots();
				if (boots != null && ! user.canUse("-antiitem.item.", new MyMaterial(boots.getType())))
				{
					blockedItem(player, boots.getType());
					inv.setBoots(null);
				}
			}

			for (ItemStack item : inv.getContents())
			{
				if (item != null && item.getType() != Material.AIR)
				{
					if (enabled && ! user.canUse("-antiitem.item.", new MyMaterial(item.getType(), item.getData())))
					{
						blockedItem(player, item.getType());
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
								regulatedEnchantment(player, ench, level, item.getType(), "high");
							}
							else if (level <= 0)
							{
								item.removeEnchantment(ench);
								regulatedEnchantment(player, ench, level, item.getType(), "low");
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
								regulatedEnchantment(player, ench, level, armor.getType(), "high");
							}
							else if (level <= 0)
							{
								armor.removeEnchantment(ench);
								regulatedEnchantment(player, ench, level, armor.getType(), "low");
							}
						}
					}
				}
			}
		}
	}

	private final void regulatedEnchantment(Player player, Enchantment ench, int level, Material mat, String why)
	{
		player.sendMessage(FormatUtil.format("&4Enchantment &f{0}&4:&f{1} &4has been removed from &f{2} &4because it was too &f{3}&4.",
				FormatUtil.getFriendlyName(ench), level, FormatUtil.getFriendlyName(mat), why));
	}

	private final void blockedItem(Player player, Material mat)
	{
		player.sendMessage(FormatUtil.format("&4Item &f{0} &4is not allowed and has been removed from your inventory.",
				FormatUtil.getFriendlyName(mat)));
	}

	@Override
	public void reload()
	{
		this.enabled = plugin.getConfig().getBoolean("antiItem.enabled", false);
		this.maxEnchantmentLevel = plugin.getConfig().getInt("antiItem.maxEnchantmentLevel", 25);
		this.regulateEnchantments = plugin.getConfig().getBoolean("antiItem.regulateEnchantments", false);
	}
}