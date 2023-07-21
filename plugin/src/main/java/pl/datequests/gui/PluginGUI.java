package pl.datequests.gui;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.datequests.DateQuests;
import pl.datequests.data.MessagesManager;
import pl.datequests.nbtapi.NBTAPIController;
import pl.datequests.permissions.PermissionManager;
import pl.datequests.quests.QuestsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public abstract class PluginGUI {

    private final QuestsManager questsManager = DateQuests.getInstance().getQuestsManager();
    private final MessagesManager messages = DateQuests.getInstance().getMessagesManager();
    private final PermissionManager permissionManager = DateQuests.getInstance().getPermissionManager();
    private final Player owner;
    private final String inventoryName;
    private final int inventorySize;
    private final Inventory inventory;
    private boolean guiProtected;
    private boolean updating;
    private int updateInterval = 5;
    private int updateTask = -1;

    public PluginGUI(Player owner, String inventoryName, int inventorySize) {
        DateQuests plugin = DateQuests.getInstance();
        plugin.getGuiManager().registerInventory(this);
        this.owner = owner;
        this.inventoryName = inventoryName;
        this.inventorySize = inventorySize;
        inventory = plugin.getServer().createInventory(owner, inventorySize, inventoryName);
        owner.openInventory(inventory);
        onOpen();
    }

    public void setSlot(int slot, Material material, String name, List<String> lore) {
        ItemStack is = new ItemStack(material);
        ItemMeta im = is.getItemMeta();
        if(im != null) {
            im.setDisplayName(name);
            im.setLore(lore);
        }
        is.setItemMeta(im);
        setSlot(slot, is);
    }

    public void setSlot(int slot, int count, Material material, String name, List<String> lore) {
        ItemStack is = new ItemStack(material);
        is.setAmount(count);
        ItemMeta im = is.getItemMeta();
        if(im != null) {
            im.setDisplayName(name);
            im.setLore(lore);
        }
        is.setItemMeta(im);
        setSlot(slot, is);
    }

    public void updateLoreForNBT(int slot, String event, int index) {
        if(!DateQuests.getInstance().isUsingNBTAPI()) {
            return;
        }
        setSlot(slot, DateQuests.getInstance().getNbtAPIController().translateLore(getInventory().getItem(slot), event, index));
    }

    public void setSlot(int slot, ItemStack is) {
        inventory.setItem(slot, is);
    }

    public List<String> getLore(String... loreLine) {
        List<String> lore = new ArrayList<>(Arrays.asList(loreLine));
        List<String> parsedLore = new ArrayList<>();
        for(String line : lore) {
            String[] split = line.split("%nl%");
            parsedLore.addAll(Arrays.asList(split));
        }
        return parsedLore;
    }

    public void setSlotEnchanted(int slot, boolean enchanted) {
        ItemStack is = inventory.getItem(slot);
        if(is == null) {
            return;
        }
        ItemMeta im = is.getItemMeta();
        if(im == null) {
            return;
        }
        if(enchanted) {
            im.addEnchant(Enchantment.DURABILITY, 1, true);
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            im.removeEnchant(Enchantment.DURABILITY);
            im.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        is.setItemMeta(im);
        setSlot(slot, is);
    }

    public void setGUIProtected(boolean protect) {
        guiProtected = protect;
    }

    public void closeInventory() {
        owner.closeInventory();
    }

    public void fillWith(Material material) {
        for(int i = 0; i < inventorySize; i++) {
            setSlot(i, material, " ", new ArrayList<>());
        }
    }

    public void setGUIUpdating(boolean updating) {
        this.updating = updating;
        if(updating) {
            updateTask = DateQuests.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(
                    DateQuests.getInstance(), this::onUpdate, 0L, updateInterval);
        } else {
            if(updateTask == -1) {
                return;
            }
            DateQuests.getInstance().getServer().getScheduler().cancelTask(updateTask);
        }
    }

    public void onOpen() {

    }

    public void onClose() {
        setGUIUpdating(false);
    }

    public void onClick(int slot) {

    }

    public void onClick(int slot, boolean rightClick) {

    }

    public void onUpdate() {

    }

}
