package net.slipcor.pvparena.modules.bettergears;

import java.util.Random;

import net.minecraft.server.v1_4_5.ItemStack;
import net.minecraft.server.v1_4_5.NBTTagCompound;
import net.slipcor.pvparena.arena.ArenaClass;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Config.CFG;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;

public class HandlerV145 extends HandlerAbstract {

	public HandlerV145(BetterGears bg) {
		super(bg);
	}

	@Override
	public void equip(ArenaPlayer ap) {
		int teamColor = bg.getArena().isFreeForAll() ? ((int) ((new Random())
				.nextLong() % 16777216)) : bg.calculateTeamColor(bg.colors
				.get(ap.getArenaTeam()));

		CraftItemStack[] craftArmor = new CraftItemStack[4];
		craftArmor[0] = new CraftItemStack(Material.LEATHER_HELMET, 1);
		craftArmor[1] = new CraftItemStack(Material.LEATHER_CHESTPLATE, 1);
		craftArmor[2] = new CraftItemStack(Material.LEATHER_LEGGINGS, 1);
		craftArmor[3] = new CraftItemStack(Material.LEATHER_BOOTS, 1);

		ItemStack[] nmsArmor = new ItemStack[4];
		for (int i = 0; i < 4; i++) {
			nmsArmor[i] = craftArmor[i].getHandle();
			NBTTagCompound tag = nmsArmor[i].getTag();
			if (tag == null) {
				tag = new NBTTagCompound();
				tag.setCompound("display", new NBTTagCompound());
				nmsArmor[i].tag = tag;
			}
			tag = nmsArmor[i].tag.getCompound("display");
			tag.setInt("color", teamColor);
			nmsArmor[i].tag.setCompound("display", tag);
		}

		Short s = bg.levels.get(ap.getArenaClass());
		
		if (s == null) {
			String autoClass = bg.getArena().getArenaConfig().getString(CFG.READY_AUTOCLASS);
			ArenaClass ac = bg.getArena().getClass(autoClass);
			s = bg.levels.get(ac);
		}
		

		craftArmor[0].addUnsafeEnchantment(
				Enchantment.PROTECTION_ENVIRONMENTAL, s);
		craftArmor[0]
				.addUnsafeEnchantment(Enchantment.PROTECTION_EXPLOSIONS, s);
		craftArmor[0].addUnsafeEnchantment(Enchantment.PROTECTION_FALL, s);
		craftArmor[0].addUnsafeEnchantment(Enchantment.PROTECTION_FIRE, s);
		craftArmor[0]
				.addUnsafeEnchantment(Enchantment.PROTECTION_PROJECTILE, s);

		ap.get().getInventory().setHelmet(craftArmor[0]);
		ap.get().getInventory().setChestplate(craftArmor[1]);
		ap.get().getInventory().setLeggings(craftArmor[2]);
		ap.get().getInventory().setBoots(craftArmor[3]);
	}
}
