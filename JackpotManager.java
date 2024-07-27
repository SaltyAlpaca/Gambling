package de.salty.smp.gambling;


import de.salty.smp.Smp;
import de.salty.smp.utils.Maths;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.text.DecimalFormat;
import java.util.*;

public class JackpotManager {

	public static final ItemStack PLATZHALTER = new ItemCreator(Material.GRAY_STAINED_GLASS_PANE).name("§7").build();
	public static final int TIME = 120;
	public static final int CLOSED = 0;
	public static final int OPEN = 1;
	public static int timeout = 0;
	public static final int DRAWING = 2;
	public static final long MAX_ENTRY = 500000;

	public static int jackpotStatus = CLOSED;
	public static long totalMoney = 0;
	public static RandomAPI<JackpotEntry> entries = new RandomAPI<>();
	public static Inventory inv;
	public static BukkitTask task;

	public static void initJackpot() {
		inv = Bukkit.createInventory(null, 27, "§8Jackpot");
		fillInventoryWithPlaceholders(inv);
		updateInventory();
		jackpotStatus = OPEN;

		startBackgroundCountdown(TIME, time -> {
			updateInventory();
			if (shouldBroadcastCountdown(time)) {
				Bukkit.broadcastMessage("§6§lJACKPOT §8• §7The Jackpot starts in §6" + time + " §7seconds. §8(§7/jackpot§8)");
			}
			if (time == 0) {
				startDrawing();
			}
		});
	}

	private static void fillInventoryWithPlaceholders(Inventory inv) {
		for (int i = 0; i < inv.getSize(); i++) {
			inv.setItem(i, PLATZHALTER);
		}
	}

	private static boolean shouldBroadcastCountdown(int time) {
		return time == 120 || time == 60 || time == 30 || time == 15 || time == 10 || time <= 5;
	}

	private static void startDrawing() {
		jackpotStatus = DRAWING;
		if (entries.map.isEmpty()) {
			jackpotStatus = CLOSED;
			Bukkit.broadcastMessage("§6§lJACKPOT §8• §7§cThe jackpot has been cancelled because there weren't enough participants.");
			return;
		}
		playWinnerAnimation();

		startBackgroundCountdown(15, time -> {
			if (time == 0) {
				finishDrawing();
			}
		});
	}

	private static void finishDrawing() {
		jackpotStatus = CLOSED;
		ItemStack winnerItem = inv.getItem(13);
		if (winnerItem == null || !winnerItem.hasItemMeta()) {
			return;
		}

		String winnerName = ChatColor.stripColor(winnerItem.getItemMeta().getDisplayName());
		Player winner = Bukkit.getPlayer(winnerName);
		if (winner == null) {
			return;
		}

		long prizeMoney = totalMoney;
		double chance = getChanceDouble(getChance(winnerName));

		Smp.getEconomy().depositPlayer(winner, prizeMoney);
		Bukkit.broadcastMessage("§6§lJACKPOT §8• §7" + "§6" + winnerName + " §7has won the jackpot worth §6" + Maths.asString(prizeMoney) + " §7Coins. " + "§8(§7" + chance + "%§8)");

		entries.clear();
		totalMoney = 0;
		updateInventory();
	}

	public static void playWinnerAnimation() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getOpenInventory().getTitle().equals("§8Jackpot")) {
                Bukkit.getScheduler().runTask(Smp.getInstance(), () -> onlinePlayer.performCommand("jackpot"));
            }
        }
		final List<ItemStack> items = createWinningItems();
		Collections.shuffle(items);

		task = Bukkit.getScheduler().runTaskTimer(Smp.getInstance(), new Runnable() {
			int rolls = 70;

			int i = 6;

			@Override
			public void run() {
				if (timeout == 0) {
					playSoundForAllViewers(Sound.BLOCK_LEVER_CLICK);
					i++;
					setRollingItems(items, i);
					rolls--;
					adjustTimeoutBasedOnRolls(rolls);
					if (rolls == 0) {
						stopTask();
						Bukkit.getScheduler().runTaskLater(Smp.getInstance(), JackpotManager::zoomToWinner, 30L);
					}
				} else {
					timeout--;
				}
			}
		}, 0L, 2L);
	}

	private static List<ItemStack> createWinningItems() {
		List<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < entries.map.size() * 180; i++) {
			RandomAPI.WinningObject<JackpotEntry> e = entries.next();
			items.add(new ItemCreator(Material.PLAYER_HEAD).setSkullOwner(e.entry.name).name("§6" + e.entry.name)
					.lore("§7Bet§8: §6" + Maths.asString(e.entry.money)).build());
		}
		return items;
	}

	private static void setRollingItems(List<ItemStack> items, int i) {
		inv.setItem(2, PLATZHALTER);
		inv.setItem(3, PLATZHALTER);
		inv.setItem(4, new ItemCreator(Material.HOPPER).name("§8• §6Roll").lore(" §7Good luck!").build());
		inv.setItem(5, PLATZHALTER);
		inv.setItem(6, PLATZHALTER);
		inv.setItem(22, new ItemCreator(Material.HOPPER).name("§8• §6Roll").lore(" §7Good luck!").build());

		inv.setItem(10, items.get(i - 3));
		inv.setItem(11, items.get(i - 2));
		inv.setItem(12, items.get(i - 1));
		inv.setItem(13, items.get(i));
		inv.setItem(14, items.get(i + 1));
		inv.setItem(15, items.get(i + 2));
		inv.setItem(16, items.get(i + 3));
	}

	private static void adjustTimeoutBasedOnRolls(int rolls) {
		if (rolls <= 70 && rolls >= 20) {
			timeout = 0;
		} else if (rolls <= 19 && rolls >= 10) {
			timeout = 1;
		} else if (rolls <= 9 && rolls >= 5) {
			timeout = 2;
		} else if (rolls <= 4 && rolls >= 0) {
			timeout = 3;
		}
	}

	private static void playSoundForAllViewers(Sound sound) {
		for (HumanEntity ent : inv.getViewers()) {
			if (ent instanceof Player) {
				((Player) ent).playSound(ent.getEyeLocation(), sound, 5, 5);
			}
		}
	}

	private static void stopTask() {
		if (task != null) {
			task.cancel();
		}
	}

	private static void zoomToWinner() {
		task = Bukkit.getScheduler().runTaskTimer(Smp.getInstance(), new Runnable() {
			int i = 0;

			@Override
			public void run() {
				playSoundForAllViewers(Sound.BLOCK_LEVER_CLICK);

				if (i == 0) {
					inv.setItem(10, PLATZHALTER);
					inv.setItem(16, PLATZHALTER);
				} else if (i == 1) {
					inv.setItem(11, PLATZHALTER);
					inv.setItem(15, PLATZHALTER);
				} else if (i == 2) {
					inv.setItem(12, new ItemCreator(Material.GRAY_STAINED_GLASS_PANE).name("§c").build());
					inv.setItem(14, new ItemCreator(Material.GRAY_STAINED_GLASS_PANE).name("§c").build());
				} else {
					stopTask();
					Bukkit.getScheduler().runTaskLater(Smp.getInstance(), () -> {
						updateInventory();
						for (HumanEntity player : inv.getViewers()) {
							if (player instanceof Player) {
								((Player) player).performCommand("jackpot");
							}
						}
					}, 15 * 20);
				}
				i++;
			}
		}, 0L, 20L);
	}

	public static void updateInventory() {

		if (inv == null) {
			return;
		}

		inv.setItem(22, new ItemCreator(Material.TRIPWIRE_HOOK).name("§8• §6§lJackpot Statistics")
				.lore(Arrays.asList(" §7There are §6" + entries.map.size() + " §7players in the jackpot.",
						" §7The jackpot contains §6" + Maths.asString(totalMoney) + " §7Coins."))
				.build());
		inv.setItem(2, new ItemCreator(Material.MAP).name("§8• §6§l500 Coins")
				.lore(" §8(§7§oClick to bet 500 coins§8)").build());
		inv.setItem(3, new ItemCreator(Material.MAP).name("§8• §6§l5,000 Coins")
				.lore(" §8(§7§oClick to bet 5,000 coins§8)").build());
		inv.setItem(4, new ItemCreator(Material.MAP).name("§8• §6§l10,000 Coins")
				.lore(" §8(§7§oClick to bet 10,000 coins§8)").build());
		inv.setItem(5, new ItemCreator(Material.MAP).name("§8• §6§l25,000 Coins")
				.lore(" §8(§7§oClick to bet 25,000 coins§8)").build());
		inv.setItem(6, new ItemCreator(Material.MAP).name("§8• §6§l50,000 Coins")
				.lore(" §8(§7§oClick to bet 50,000 coins§8)").build());
		inv.setItem(12, new ItemCreator(Material.MAP).name("§8• §6§l100,000 Coins")
				.lore(" §8(§7§oClick to bet 100,000 coins§8)").build());
		inv.setItem(13, new ItemCreator(Material.MAP).name("§8• §6§l250,000 Coins")
				.lore(" §8(§7§oClick to bet 250,000 coins§8)").build());
		inv.setItem(14, new ItemCreator(Material.MAP).name("§8• §6§l500,000 Coins")
				.lore(" §8(§7§oClick to bet 500,000 coins§8)").build());
	}



	public static String getChance(String name) {
		JackpotEntry entry = getEntry(name);
		if (entry == null) {
			return "0";
		}
		long betAmount = entry.money;
		return new DecimalFormat("#.###").format((betAmount * 100.0) / totalMoney);
	}

	public static JackpotEntry getEntry(String name) {
		for (JackpotEntry entry : entries.map.values()) {
			if (entry.name.equalsIgnoreCase(name)) {
				return entry;
			}
		}
		return null;
	}

	public static double getChanceDouble(String chance) {
		return Double.parseDouble(chance.replace(",", "."));
	}

	public static class JackpotEntry {
		public UUID uuid;
		public String name;
		public long money;

		public JackpotEntry(UUID uuid, String name, long money) {
			this.uuid = uuid;
			this.name = name;
			this.money = money;
		}
	}

	public static void startBackgroundCountdown(int seconds, final CountdownTick tick) {
		for (int i = 0; i <= seconds; i++) {
			final int timeLeft = seconds - i;
			Smp.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(Smp.getInstance(), () -> tick.onTick(timeLeft), i * 20L);
		}
	}

	public interface CountdownTick {
		void onTick(int time);
	}

	public static boolean isBetween(int a, int b, int c) {
		return c > a && c < b;
	}
}
