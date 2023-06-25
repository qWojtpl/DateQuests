package pl.datequests.data;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
            i++;
        }
    }

}
