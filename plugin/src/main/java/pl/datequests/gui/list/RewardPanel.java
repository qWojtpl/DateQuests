package pl.datequests.gui.list;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.beaverlib.gui.PluginGUI;
import pl.datequests.DateQuests;
import pl.datequests.data.MessagesManager;
import pl.datequests.quests.QuestsManager;

import java.util.ArrayList;
import java.util.List;

public class RewardPanel extends PluginGUI {

    private List<Integer> slots;
    private List<Integer> itemSlots;
    private int currentOffset;
    private int receiveTask = -1;
    private final int sourceIndex;

    public RewardPanel(Player owner, String inventoryName, int sourceIndex) {
        super(owner, inventoryName, 54);
        this.sourceIndex = sourceIndex;
    }

    @Override
    public void onOpen() {
        MessagesManager messagesManager = DateQuests.getInstance().getMessagesManager();
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
        setSlot(0, Material.DARK_OAK_DOOR, messagesManager.getMessage("backToCategory"),
                getLore(messagesManager.getMessage("backToCategoryLore")));
        setSlot(46, Material.ARROW, messagesManager.getMessage("previousPage"),
                getLore(messagesManager.getMessage("previousPageLore")));
        setSlot(49, Material.CHEST, messagesManager.getMessage("receiveAllRewards"),
                getLore(messagesManager.getMessage("receiveAllRewardsLore")));
        setSlot(52, Material.ARROW, messagesManager.getMessage("nextPage"),
                getLore(messagesManager.getMessage("nextPageLore")));
        loadRewards();
    }

    @Override
    public void onClick(int slot) {
        if(itemSlots.contains(slot)) {
            DateQuests.getInstance().getQuestsManager().receiveReward(getOwner().getName(), itemSlots.indexOf(slot) + currentOffset);
            loadRewards();
        } else if(slot == 0) {
            closeInventory();
            new QuestPanel(getOwner(), getInventoryName(), sourceIndex);
        } else if(slot == 46) {
            previousPage();
        } else if(slot == 49) {
            receiveAllRewards();
        } else if(slot == 52) {
            nextPage();
        }
    }

    @Override
    public void onClose() {
        if(receiveTask != -1) {
            DateQuests.getInstance().getServer().getScheduler().cancelTask(receiveTask);
        }
    }

    public void loadRewards() {
        MessagesManager messagesManager = DateQuests.getInstance().getMessagesManager();
        for(int slot : slots) {
            setSlot(slot, Material.WHITE_STAINED_GLASS_PANE, " ", getLore(""));
        }
        itemSlots = new ArrayList<>();
        List<ItemStack> rewards = DateQuests.getInstance().getQuestsManager().getPlayersRewards(getOwner().getName());
        while(currentOffset >= rewards.size()) {
            if(currentOffset == 0) {
                break;
            }
            previousPage();
        }
        int i = 0;
        int j = 0;
        for(ItemStack is : rewards) {
            if(currentOffset > j) {
                j++;
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
                lore.add(messagesManager.getMessage("receiveReward"));
                im.setLore(lore);
                itemStack.setItemMeta(im);
            }
            setSlot(slots.get(i), itemStack);
            itemSlots.add(slots.get(i));
            i++;
        }
        if(rewards.size() == 0) {
            setSlot(22, Material.BARRIER, messagesManager.getMessage("noRewards"),
                    getLore(messagesManager.getMessage("noRewardsLore")));
        }
    }

    private void previousPage() {
        if(currentOffset == 0) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset -= 28;
        loadRewards();
    }

    private void nextPage() {
        if(currentOffset + 28 > DateQuests.getInstance().getQuestsManager().getPlayersRewards(getOwner().getName()).size() - 1) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset += 28;
        loadRewards();
    }

    private void receiveAllRewards() {
        if(itemSlots.size() == 0) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        DateQuests plugin = DateQuests.getInstance();
        QuestsManager questsManager = plugin.getQuestsManager();
        receiveTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if(questsManager.getPlayersRewards(getOwner().getName()).size() == 0) {
                getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_YES, 1.0F, 0.75F);
                plugin.getServer().getScheduler().cancelTask(receiveTask);
                return;
            }
            questsManager.receiveReward(getOwner().getName(), 0);
            loadRewards();
        }, 0L, 1L);
    }

}
