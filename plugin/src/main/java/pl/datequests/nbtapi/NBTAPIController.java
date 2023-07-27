package pl.datequests.nbtapi;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Material;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.datequests.DateQuests;
import pl.datequests.data.MessagesManager;

import javax.annotation.Nullable;

public class NBTAPIController {

    private final DateQuests plugin = DateQuests.getInstance();

    @Nullable
    public ItemStack translateLore(ItemStack is, String event, int index) {
        if(is == null) {
            return null;
        }
        if(is.getItemMeta() == null) {
            return is;
        }
        if(is.getItemMeta().getLore() == null) {
            return is;
        }
        String[] split = is.getItemMeta().getLore().get(index).split(" ");
        if(split.length != 3) {
            return is;
        }
        NBTItem nbt = new NBTItem(is);
        NBTCompound comp = nbt.getOrCreateCompound("display");
        Material m = plugin.getQuestsManager().getEventMaterial(event);
        String translationKey = m.getTranslationKey();
        if(m.name().contains("_SPAWN_EGG")) {
            translationKey = "entity.minecraft." + m.name().replace("_SPAWN_EGG", "").toLowerCase();
        }
        comp.getStringList("Lore").set(index, "{\"extra\":[{\"translate\":\"" + translationKey + "\"}],\"text\":\"" + split[0] + " " + split[1] + " \",\"bold\":false,\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false,\"color\":\"dark_green\"}");
        nbt.applyNBT(is);
        return is;
    }

    public void sendTranslatedEvent(Player player, String prefix, String event, String suffix) {
        String[] split = event.split(" ");
        if(split.length != 3) {
            return;
        }
        Material m = plugin.getQuestsManager().getEventMaterial(event);
        String translationKey = m.getTranslationKey();
        if(m.name().contains("_SPAWN_EGG")) {
            translationKey = "entity.minecraft." + m.name().replace("_SPAWN_EGG", "").toLowerCase();
        }
        BaseComponent[] components = new BaseComponent[]{
                new TextComponent(prefix + split[0] + " " + split[1] + " "),
                new TranslatableComponent(translationKey),
                new TextComponent(suffix)
        };
        for(BaseComponent component : components) {
            component.setColor(ChatColor.DARK_GREEN);
        }
        player.spigot().sendMessage(components);
    }

    public void sendTranslatedItem(Player player, Material material, int amount) {
        String translationKey = material.getTranslationKey();
        BaseComponent[] components = new BaseComponent[]{
                new TextComponent(" " + plugin.getMessagesManager().getMessage("deliveredItem")),
                new TranslatableComponent(translationKey),
                new TextComponent(" §ax§2" + amount)
        };
        for(BaseComponent component : components) {
            component.setColor(ChatColor.DARK_GREEN);
        }
        player.spigot().sendMessage(components);
    }

    public void sendTranslatedSubtitle(Player player, String event) {
        String[] split = event.split(" ");
        if(split.length != 3) {
            return;
        }
        Material m = plugin.getQuestsManager().getEventMaterial(event);
        String translationKey = m.getTranslationKey();
        if(m.name().contains("_SPAWN_EGG")) {
            translationKey = "entity.minecraft." + m.name().replace("_SPAWN_EGG", "").toLowerCase();
        }
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "title " + player.getName() + " subtitle {\"text\":\"" + split[0] + " " + split[1] + " \",\"color\":\"gold\",\"extra\":[{\"translate\":\"" + translationKey + "\"}]}");
    }

}
