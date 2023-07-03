package pl.datequests.data;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.datequests.DateQuests;

import java.util.ArrayList;
import java.util.List;

public class ItemLoader {

    public static ItemStack getItemStack(YamlConfiguration yml, String path) {
        path += ".";
        Material m = Material.getMaterial(yml.getString(path + "material", "DIRT").toUpperCase());
        if(m == null) {
            m = Material.DIRT;
        }
        ItemStack is = new ItemStack(m);
        is.setAmount(yml.getInt(path + "amount"));
        ItemMeta im = is.getItemMeta();
        if(im != null) {
            String name = yml.getString(path + "name");
            if(name != null) {
                im.setDisplayName(name.replace("&", "§"));
            }
            List<String> lore = yml.getStringList(path + "lore");
            if(lore.size() > 0) {
                List<String> newLore = new ArrayList<>();
                for(String line : lore) {
                    newLore.add(line.replace("&", "§"));
                }
                im.setLore(newLore);
            }
            List<String> enchantments = yml.getStringList(path + "enchantments");
            for(String enchant : enchantments) {
                String[] split = enchant.split(":");
                if(split.length != 2) {
                    DateQuests.getInstance().getLogger().severe("Error while parsing ItemStack: "
                            + enchant + " is not a correct enchantment format!");
                    continue;
                }
                Enchantment realEnchantment = Enchantment.getByName(split[0]);
                if(realEnchantment == null) {
                    DateQuests.getInstance().getLogger().severe("Error while parsing ItemStack: "
                            + split[0] + " is not a real enchantment!");
                    continue;
                }
                int level;
                try {
                    level = Integer.parseInt(split[1]);
                } catch(NumberFormatException e) {
                    DateQuests.getInstance().getLogger().severe("Error while parsing ItemStack: "
                            + split[1] + " is not a correct level!");
                    continue;
                }
                im.addEnchant(realEnchantment, level, true);
            }
            is.setItemMeta(im);
        }
        return is;
    }

    public static List<ItemStack> getItemStackList(YamlConfiguration yml, String path) {
        List<ItemStack> list = new ArrayList<>();
        int i = 0;
        while(yml.getString(path + "." + i + ".material") != null) {
            list.add(getItemStack(yml, path + "." + i));
            i++;
        }
        return list;
    }

    public static void parseList(YamlConfiguration yml, String path, List<ItemStack> list) {
        yml.set(path, null);
        int i = 0;
        for(ItemStack is : list) {
            String currentPath = path + "." + i + ".";
            yml.set(currentPath + "material", is.getType().name());
            yml.set(currentPath + "amount", is.getAmount());
            ItemMeta im = is.getItemMeta();
            if(im == null) {
                i++;
                continue;
            }
            if(!im.getDisplayName().equals("")) {
                yml.set(currentPath + "name", im.getDisplayName());
            }
            yml.set(currentPath + "lore", im.getLore());
            List<String> enchantments = new ArrayList<>();
            for(Enchantment enchant : im.getEnchants().keySet()) {
                enchantments.add(enchant + ":" + im.getEnchants().get(enchant));
            }
            yml.set(currentPath + "enchantments", enchantments);
            i++;
        }
    }

}
