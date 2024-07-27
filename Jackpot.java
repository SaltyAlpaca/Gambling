package de.salty.smp.gambling;


import de.salty.smp.Smp;
import de.salty.smp.utils.Maths;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

import static de.salty.smp.gambling.JackpotManager.inv;


public class Jackpot implements CommandExecutor, Listener {



	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1) {
			if (sender.hasPermission("cb.jackpot.start")) {
				if (args[0].equalsIgnoreCase("start")) {
					JackpotManager.initJackpot();
					return true;
				}
			}
		}
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("teilnehmen") || args[0].equalsIgnoreCase("join")) {
					if (isLong(args[1])) {
						for (JackpotManager.JackpotEntry et : JackpotManager.entries.map.values()) {
							if (et.name.equalsIgnoreCase(p.getName())) {
								p.sendMessage("§6§lJACKPOT §8• §7You have already joined the jackpot.");
								return true;
							}
						}
						long coins = Long.parseLong(args[1]);
						if (JackpotManager.MAX_ENTRY < coins) {
							p.sendMessage("§6§lJACKPOT §8• §7You can join with a maximum of §6"
									+ Maths.asString(JackpotManager.MAX_ENTRY) + " §7Coins.");
							return true;
						}
						if (coins < 500) {
							p.sendMessage("§6§lJACKPOT §8• §7You must join with at least §6500 §7Coins.");
							return true;
						}
						double money = Smp.getEconomy().getBalance(p);
						if (money >= coins) {
							Smp.getEconomy().withdrawPlayer(p, coins);
							JackpotManager.totalMoney += coins;
							JackpotManager.entries.add((double) coins / 10,
									new JackpotManager.JackpotEntry(p.getUniqueId(), p.getName(), coins));
							p.sendMessage("§6§lJACKPOT §8• §7You have joined the jackpot with §6" + Maths.asString(coins) + " Coins§7.");
							JackpotManager.updateInventory();
							if (JackpotManager.entries.map.size() >= 2) {
								JackpotManager.initJackpot();
							}
							for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
								if (onlinePlayer.getOpenInventory().getTitle().equals("§8Jackpot")) {
									Bukkit.getScheduler().runTask(Smp.getInstance(), () -> onlinePlayer.performCommand("jackpot"));
								}
							}

							return true;
						}
						p.closeInventory();
						p.sendMessage("§6§lJACKPOT §8• §7You don't have enough coins!");
						return true;
					}
					p.sendMessage("§6§lJACKPOT §8• §7You must enter a valid number.");
					return true;
				}
			}
			if (args.length == 0) {
				if (JackpotManager.jackpotStatus != JackpotManager.DRAWING) {
					Inventory inv = Bukkit.createInventory(p, 27, "§8Jackpot");

					ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
					i.setAmount(1);
					ItemMeta im = i.getItemMeta();
					im.setDisplayName("§8§8§8§8");
					i.setItemMeta(im);
					for (int o1 = 0; o1 != 27; o1++) {
						inv.setItem(o1, i);
					}
					inv.setItem(22,
							new ItemCreator(Material.TRIPWIRE_HOOK).name("§8• §6§lJackpot Statistics")
									.lore(Arrays.asList(" §7There are §6" + JackpotManager.entries.map.size() + " §7players in the jackpot.",
											" §7The jackpot contains §6" + Maths.asString(JackpotManager.totalMoney) + " §7Coins."))
									.build());
					inv.setItem(2,
							new ItemCreator(Material.MAP).name("§8• §6§l500 Coins")
									.lore(" §8(§7§oClick to bet 500 coins§8)").build());
					inv.setItem(3,
							new ItemCreator(Material.MAP).name("§8• §6§l5,000 Coins")
									.lore(" §8(§7§oClick to bet 5,000 coins§8)").build());
					inv.setItem(4,
							new ItemCreator(Material.MAP).name("§8• §6§l10,000 Coins")
									.lore(" §8(§7§oClick to bet 10,000 coins§8)").build());
					inv.setItem(5,
							new ItemCreator(Material.MAP).name("§8• §6§l25,000 Coins")
									.lore(" §8(§7§oClick to bet 25,000 coins§8)").build());
					inv.setItem(6,
							new ItemCreator(Material.MAP).name("§8• §6§l50,000 Coins")
									.lore(" §8(§7§oClick to bet 50,000 coins§8)").build());
					inv.setItem(12,
							new ItemCreator(Material.MAP).name("§8• §6§l100,000 Coins")
									.lore(" §8(§7§oClick to bet 100,000 coins§8)").build());
					inv.setItem(13,
							new ItemCreator(Material.MAP).name("§8• §6§l250,000 Coins")
									.lore(" §8(§7§oClick to bet 250,000 coins§8)").build());
					inv.setItem(14,
							new ItemCreator(Material.MAP).name("§8• §6§l500,000 Coins")
									.lore(" §8(§7§oClick to bet 500,000 coins§8)").build());
					p.openInventory(inv);
					return true;
				} else {
					p.openInventory(inv);
					return true;
				}
			}
		}
		return true;
	}

	public static boolean isLong(String s) {
		try {
			Long.parseLong(s);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return Long.parseLong(s) >= 1L;
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (e.getView().getTitle().equals("§8Jackpot")) {
			e.setCancelled(true);
			Player p = (Player) e.getWhoClicked();

			if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
				return;
			}

			if (JackpotManager.jackpotStatus == JackpotManager.DRAWING) {
				return;
			}

			String displayName = e.getCurrentItem().getItemMeta().getDisplayName();

			if (displayName.equals("§8• §6§l500 Coins")) {
				p.performCommand("jackpot teilnehmen 500");
			} else if (displayName.equals("§8• §6§l5,000 Coins")) {
				p.performCommand("jackpot teilnehmen 5000");
			} else if (displayName.equals("§8• §6§l10,000 Coins")) {
				p.performCommand("jackpot teilnehmen 10000");
			} else if (displayName.equals("§8• §6§l25,000 Coins")) {
				p.performCommand("jackpot teilnehmen 25000");
			} else if (displayName.equals("§8• §6§l50,000 Coins")) {
				p.performCommand("jackpot teilnehmen 50000");
			} else if (displayName.equals("§8• §6§l100,000 Coins")) {
				p.performCommand("jackpot teilnehmen 100000");
			} else if (displayName.equals("§8• §6§l250,000 Coins")) {
				p.performCommand("jackpot teilnehmen 250000");
			} else if (displayName.equals("§8• §6§l500,000 Coins")) {
				p.performCommand("jackpot teilnehmen 500000");
			} else {
				return;
			}

			p.playSound(p.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
		}
	}





	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (e.getView().getTitle().equals("§8Jackpot") && JackpotManager.jackpotStatus == JackpotManager.DRAWING) {
			Bukkit.getScheduler().runTaskLater(Smp.getInstance(), () -> {
				((Player) e.getPlayer()).openInventory(inv);
			}, 1L);
		}
	}

}
