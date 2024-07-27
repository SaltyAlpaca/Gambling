package de.salty.smp.gambling;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.profile.PlayerProfile;

public class ItemCreator {
	private org.bukkit.inventory.ItemStack is;

	public ItemCreator(Material mat) {
		this.is = new org.bukkit.inventory.ItemStack(mat);
	}

	public ItemCreator(org.bukkit.inventory.ItemStack is) {
		this.is = is;
	}

	public ItemCreator amount(int amount) {
		this.is.setAmount(amount);
		return this;
	}

	public ItemCreator name(String name) {
		ItemMeta meta = this.is.getItemMeta();
		meta.setDisplayName(name);
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemCreator lore(String name) {
		ItemMeta meta = this.is.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.add(name);
		meta.setLore(lore);
		this.is.setItemMeta(meta);
		return this;
	}
	
	public ItemCreator lore2(String name, String string) {
		ItemMeta meta = this.is.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = new ArrayList<>();
		}
		lore.add(name);
		lore.add(string);
		meta.setLore(lore);
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemCreator lore(List<String> lore) {
		ItemMeta meta = this.is.getItemMeta();
		meta.setLore(lore);
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemCreator durability(int durability) {
		this.is.setDurability((short) durability);
		return this;
	}

	@SuppressWarnings("deprecation")
	public ItemCreator data(int data) {
		this.is.setData(new MaterialData(this.is.getType(), (byte) data));
		return this;
	}

	public ItemCreator enchantment(Enchantment enchantment, int level) {
		this.is.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	public ItemCreator enchantment(Enchantment enchantment) {
		this.is.addUnsafeEnchantment(enchantment, 1);
		return this;
	}

	public ItemCreator type(Material material) {
		this.is.setType(material);
		return this;
	}

	public ItemCreator clearLore() {
		ItemMeta meta = this.is.getItemMeta();
		meta.setLore(new ArrayList<>());
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemCreator clearEnchantments() {
		for (Enchantment e : this.is.getEnchantments().keySet()) {
			this.is.removeEnchantment(e);
		}
		return this;
	}

	public ItemCreator color(Color color) {
		if ((this.is.getType() == Material.LEATHER_BOOTS) || (this.is.getType() == Material.LEATHER_CHESTPLATE)
				|| (this.is.getType() == Material.LEATHER_HELMET) || (this.is.getType() == Material.LEATHER_LEGGINGS)) {
			LeatherArmorMeta meta = (LeatherArmorMeta) this.is.getItemMeta();
			meta.setColor(color);
			this.is.setItemMeta(meta);
			return this;
		}
		throw new IllegalArgumentException("color() only applicable for leather armor!");
	}

	public ItemCreator setSkullOwner(String owner) {
		if ((this.is.getType() == Material.PLAYER_HEAD) || (this.is.getType() == Material.PLAYER_WALL_HEAD)) {
			this.is.setDurability((short) SkullType.PLAYER.ordinal());
			SkullMeta meta = (SkullMeta) this.is.getItemMeta();
			meta.setOwner(owner);
			this.is.setItemMeta(meta);
			return this;
		}
		throw new IllegalArgumentException("skullOwner() only applicable for skulls!");
	}

	public org.bukkit.inventory.ItemStack build() {
		return this.is;
	}
	/*
	 * public org.bukkit.inventory.ItemStack addGlow() { ItemStack nmsStack =
	 * CraftItemStack.asNMSCopy(is); NBTTagCompound tag = null; if
	 * (!nmsStack.hasTag()) { tag = new NBTTagCompound(); nmsStack.setTag(tag);
	 * } if (tag == null) { tag = nmsStack.getTag(); } NBTTagList ench = new
	 * NBTTagList(); tag.set("ench", ench); nmsStack.setTag(tag); return
	 * CraftItemStack.asCraftMirror(nmsStack); }
	 */
}
