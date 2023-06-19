package pl.datequests.data;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemLoader {

    public static ItemStack getItemStack(YamlConfiguration yml, String path) {
        return new ItemStack(Material.AIR);
    }

    public static List<ItemStack> getItemStackList(YamlConfiguration yml, String path) {
        return new ArrayList<>();
    }

}
