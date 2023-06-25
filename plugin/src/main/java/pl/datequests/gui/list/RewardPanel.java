package pl.datequests.gui.list;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.datequests.gui.PluginGUI;

import java.util.ArrayList;
import java.util.List;

public class RewardPanel extends PluginGUI {

    private List<Integer> slots;
    private List<Integer> itemSlots;
    private int currentOffset;

    public RewardPanel(Player owner, String inventoryName) {
        super(owner, inventoryName, 54);
    }

    @Override
    public void onOpen() {
        setGUIProtected(true);
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
        slots = new ArrayList<>();
        currentOffset = 0;
        int[] protectedSlots = new int[]{17, 18, 26, 27, 35, 36, 44};
        for(int i = 10; i < 45; i++) {
            boolean isProtected = false;
            for(int slot : protectedSlots) {
                if(i == slot) {
                    isProtected = true;
                    break;
                }
            }
            if(!isProtected) {
                slots.add(i);
            }
        }
        loadRewards();
    }

    public void loadRewards() {
        for(int slot : slots) {
            setSlot(slot, Material.WHITE_STAINED_GLASS_PANE, "", getLore(""));
        }
        itemSlots = new ArrayList<>();
        List<ItemStack> rewards = getQuestsManager().getPlayersRewards(getOwner().getName());
        int i = 0;
        for(ItemStack is : rewards) {
            if(currentOffset > i) {
                i++;
                continue;
            }
            if(i > slots.size() - 1) {
                break;
            }
            ItemStack itemStack = is.clone();
            ItemMeta im = itemStack.getItemMeta();
            if(im != null) {
                List<String> lore = im.getLore();
                if(lore == null) {
                    lore = new ArrayList<>();
                }
                lore.add(" ");
                lore.add("§a§lCLICK TO RECEIVE");
                im.setLore(lore);
                itemStack.setItemMeta(im);
            }
            setSlot(slots.get(i), itemStack);
            itemSlots.add(slots.get(i));
            i++;
        }
    }

    @Override
    public void onClick(int slot) {
        if(itemSlots.contains(slot)) {
            getQuestsManager().receiveReward(getOwner().getName(), itemSlots.indexOf(slot));
            loadRewards();
        }
    }

}
