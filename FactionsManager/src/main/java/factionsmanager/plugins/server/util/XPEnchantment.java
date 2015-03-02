package factionsmanager.plugins.server.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

public class XPEnchantment extends EnchantmentWrapper {

	public XPEnchantment(int id) {
		super(id);
	}

	@Override
	public String getName() {
		return "Experience";
	}

	@Override
	public int getMaxLevel() {
		return 10;
	}

	@Override
	public int getStartLevel() {
		return 0;
	}

	@Override
	public EnchantmentTarget getItemTarget() {
		return EnchantmentTarget.ALL;
	}

	@Override
	public boolean conflictsWith(Enchantment enchantment) {
		return false;
	}

	@Override
	public boolean canEnchantItem(ItemStack itemStack) {
		return true;
	}
}
