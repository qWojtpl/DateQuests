package pl.datequests.gui.list;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.datequests.gui.PluginGUI;
import pl.datequests.quests.Quest;
import pl.datequests.quests.QuestState;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminPanel extends PluginGUI {

    private final String lookupPlayer;
    private int pageID = 0;
    private int currentOffset = 0;
    private final List<Integer> slots = new ArrayList<>();
    private final HashMap<Integer, Quest> questSlots = new HashMap<>();
    private final HashMap<Integer, Integer> rewardSlots = new HashMap<>();

    public AdminPanel(Player owner, String inventoryName, String lookupPlayer) {
        super(owner, inventoryName, 54);
        this.lookupPlayer = lookupPlayer;
        onLoad();
    }

    public void onLoad() {
        setGUIProtected(true);
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
        pageID = 0;
        setSlot(21, Material.OAK_SIGN, getMessages().getMessage("allPlayersQuests"),
                getLore(getMessages().getMessage("allPlayersQuestsLore")));
        setSlot(23, Material.CHEST, getMessages().getMessage("playerRewards"),
                getLore(getMessages().getMessage("playerRewardsLore")));
    }

    @Override
    public void onClick(int slot, boolean rightClick) {
        if(pageID == 0) {
            if (slot == 21) {
                loadQuestsPage();
            } else if (slot == 23) {
                loadRewardsPage();
            }
        } else if(slot == 46) {
            previousPage();
        } else if(slot == 52) {
            nextPage();
        } else if(pageID == 1) {
            if(questSlots.containsKey(slot)) {
                Quest q = questSlots.get(slot);
                if(rightClick) {
                    if(getOwner().hasPermission(getPermissionManager().getPermission("lookupChangeEvent"))) {
                        boolean changed = q.isChanged();
                        if(changed) {
                            q.setChanged(false);
                        }
                        q.randomizeEvent();
                        q.setQuestState(QuestState.NOT_COMPLETED);
                        q.updateTagID();
                        q.setChanged(changed);
                        q.save();
                        getOwner().sendMessage(getMessages().getMessage("changedQuestEvent") + q.getTranslatedEvent());
                        getOwner().playSound(getOwner(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    }
                } else {
                    if(getOwner().hasPermission(getPermissionManager().getPermission("lookupSwitchComplete"))) {
                        if(q.getQuestState().equals(QuestState.COMPLETED)) {
                            q.setQuestState(QuestState.NOT_COMPLETED);
                            q.setProgress(0);
                            q.updateTagID();
                        } else {
                            q.setQuestState(QuestState.COMPLETED);
                            q.setProgress(q.getRequiredProgress());
                        }
                        q.save();
                        getOwner().sendMessage(getMessages().getMessage("changedQuestProgress") + q.getProgress());
                        getOwner().playSound(getOwner(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    }
                }
                loadQuests();
            }
        } else if(pageID == 2) {
            if(rewardSlots.containsKey(slot)) {
                if(getOwner().hasPermission(getPermissionManager().getPermission("lookupRemoveReward"))) {
                    getQuestsManager().removeReward(lookupPlayer, rewardSlots.get(slot));
                    getOwner().playSound(getOwner(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    loadRewardsPage();
                }
            }
        }
    }

    public void loadQuestsPage() {
        pageID = 1;
        loadFrame();
        loadQuests();
    }

    public void loadQuests() {
        List<Quest> playerQuests = getQuestsManager().reverseList(getQuestsManager().getPlayersQuests(lookupPlayer));
        int i = 0;
        int j = 0;
        for(Quest q : playerQuests) {
            if(currentOffset > j) {
                j++;
                continue;
            }
            if(i > slots.size() - 1) {
                break;
            }
            String additionalMessages = "";
            if(getOwner().hasPermission(getPermissionManager().getPermission("lookupSwitchComplete"))) {
                additionalMessages += getMessages().getMessage("resetQuestClick");
            }
            if(getOwner().hasPermission(getPermissionManager().getPermission("lookupChangeEvent"))) {
                additionalMessages += getMessages().getMessage("changeQuestClick");
            }
            boolean enchant = false;
            Material m = getQuestsManager().getEventMaterial(q.getEvent());
            String status = getMessages().getMessage("questCompleted");
            if(q.getQuestState().equals(QuestState.NOT_ACTIVE)) {
                status = getMessages().getMessage("questNotActive");
                m = Material.RED_CONCRETE;
            } else if(q.getQuestState().equals(QuestState.NOT_COMPLETED)) {
                status = getMessages().getMessage("questNotCompleted");
                enchant = true;
            }
            setSlot(slots.get(i),
                    m,
                    "§6" + q.getQuestSchema().getSchemaName() + "§2:§6" + q.getDateTag(),
                    getLore(
                            "§2" + q.getTranslatedEvent(),
                            getMessages().getMessage("type") + q.getQuestSchema().getSchemaName(),
                            MessageFormat.format(getMessages().getMessage("progress"), q.getProgress(), q.getRequiredProgress()),
                            status,
                            additionalMessages));
            questSlots.put(slots.get(i), q);
            setSlotEnchanted(slots.get(i), enchant);
            i++;
        }
    }

    public void loadRewardsPage() {
        pageID = 2;
        loadFrame();
        loadRewards();
    }

    public void loadRewards() {
        List<ItemStack> playerRewards = getQuestsManager().getPlayersRewards(lookupPlayer);
        int i = 0;
        int j = 0;
        for(ItemStack is : playerRewards) {
            if(currentOffset > j) {
                j++;
                continue;
            }
            if(i > slots.size() - 1) {
                break;
            }
            ItemStack itemStack = is.clone();
            if(getOwner().hasPermission(getPermissionManager().getPermission("lookupRemoveReward"))) {
                ItemMeta im = itemStack.getItemMeta();
                if(im != null) {
                    List<String> lore = im.getLore();
                    if(lore == null) {
                        lore = new ArrayList<>();
                    }
                    lore.add(" ");
                    lore.add(getMessages().getMessage("removeRewardClick"));
                    im.setLore(lore);
                    itemStack.setItemMeta(im);
                }
            }
            setSlot(slots.get(i), itemStack);
            rewardSlots.put(slots.get(i), i);
            i++;
        }
    }

    private void loadFrame() {
        questSlots.clear();
        rewardSlots.clear();
        slots.clear();
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
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
                setSlot(i, Material.WHITE_STAINED_GLASS_PANE, " ", getLore(""));
            }
        }
        setSlot(46, Material.ARROW, getMessages().getMessage("previousPage"),
                getLore(getMessages().getMessage("previousPageLore")));
        setSlot(52, Material.ARROW, getMessages().getMessage("nextPage"),
                getLore(getMessages().getMessage("nextPageLore")));
    }

    private void nextPage() {
        if(getQuestsManager().getPlayersQuests(getOwner().getName()).size() - 1 < currentOffset + 36) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset += 36;
        loadQuests();
    }

    private void previousPage() {
        if(currentOffset == 0) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset -= 36;
        loadQuests();
    }

}
