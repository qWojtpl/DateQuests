package pl.datequests.gui.list;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import pl.beaverlib.gui.PluginGUI;
import pl.datequests.DateQuests;
import pl.datequests.data.MessagesManager;
import pl.datequests.permissions.PermissionManager;
import pl.datequests.quests.Quest;
import pl.datequests.quests.QuestState;
import pl.datequests.quests.QuestsManager;

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
        MessagesManager messagesManager = DateQuests.getInstance().getMessagesManager();
        setGUIProtected(true);
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
        pageID = 0;
        setSlot(21, Material.OAK_SIGN, messagesManager.getMessage("allPlayersQuests"),
                getLore(messagesManager.getMessage("allPlayersQuestsLore")));
        setSlot(23, Material.CHEST, messagesManager.getMessage("playerRewards"),
                getLore(messagesManager.getMessage("playerRewardsLore")));
    }

    @Override
    public void onClick(int slot, boolean rightClick) {
        PermissionManager permissionManager = DateQuests.getInstance().getPermissionManager();
        MessagesManager messagesManager = DateQuests.getInstance().getMessagesManager();
        QuestsManager questsManager = DateQuests.getInstance().getQuestsManager();
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
                    if(getOwner().hasPermission(permissionManager.getPermission("lookupChangeEvent"))) {
                        boolean changed = q.isChanged();
                        if(changed) {
                            q.setChanged(false);
                        }
                        q.randomizeEvent();
                        q.setQuestState(QuestState.NOT_COMPLETED);
                        q.updateTagID();
                        q.setChanged(changed);
                        q.save();
                        getOwner().sendMessage(messagesManager.getMessage("changedQuestEvent") + q.getTranslatedEvent());
                        getOwner().playSound(getOwner(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    }
                } else {
                    if(getOwner().hasPermission(permissionManager.getPermission("lookupSwitchComplete"))) {
                        if(q.getQuestState().equals(QuestState.COMPLETED)) {
                            q.setQuestState(QuestState.NOT_COMPLETED);
                            q.setProgress(0);
                            q.updateTagID();
                        } else {
                            q.setQuestState(QuestState.COMPLETED);
                            q.setProgress(q.getRequiredProgress());
                        }
                        q.save();
                        getOwner().sendMessage(messagesManager.getMessage("changedQuestProgress") + q.getProgress());
                        getOwner().playSound(getOwner(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    }
                }
                loadQuests();
            }
        } else if(pageID == 2) {
            if(rewardSlots.containsKey(slot)) {
                if(getOwner().hasPermission(permissionManager.getPermission("lookupRemoveReward"))) {
                    questsManager.removeReward(lookupPlayer, rewardSlots.get(slot));
                    getOwner().playSound(getOwner(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    loadRewardsPage();
                }
            }
        }
    }

    public void loadQuestsPage() {
        currentOffset = 0;
        pageID = 1;
        loadFrame();
        loadQuests();
    }

    public void loadQuests() {
        PermissionManager permissionManager = DateQuests.getInstance().getPermissionManager();
        MessagesManager messagesManager = DateQuests.getInstance().getMessagesManager();
        QuestsManager questsManager = DateQuests.getInstance().getQuestsManager();
        loadFrame();
        List<Quest> playerQuests = questsManager.reverseList(questsManager.getPlayersQuests(lookupPlayer));
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
            if(getOwner().hasPermission(permissionManager.getPermission("lookupSwitchComplete"))) {
                additionalMessages += messagesManager.getMessage("resetQuestClick");
            }
            if(getOwner().hasPermission(permissionManager.getPermission("lookupChangeEvent"))) {
                additionalMessages += messagesManager.getMessage("changeQuestClick");
            }
            boolean enchant = false;
            Material m = questsManager.getEventMaterial(q.getEvent());
            String status = messagesManager.getMessage("questCompleted");
            if(q.getQuestState().equals(QuestState.NOT_ACTIVE)) {
                status = messagesManager.getMessage("questNotActive");
                m = Material.RED_CONCRETE;
            } else if(q.getQuestState().equals(QuestState.NOT_COMPLETED)) {
                status = messagesManager.getMessage("questNotCompleted");
                enchant = true;
            }
            setSlot(slots.get(i),
                    m,
                    "§6" + q.getQuestSchema().getSchemaName() + "§2:§6" + q.getDateTag(),
                    getLore(
                            "§2" + q.getTranslatedEvent(),
                            messagesManager.getMessage("type") + q.getQuestSchema().getSchemaName(),
                            MessageFormat.format(messagesManager.getMessage("progress"), q.getProgress(), q.getRequiredProgress()),
                            status,
                            additionalMessages));
            questSlots.put(slots.get(i), q);
            setSlotEnchanted(slots.get(i), enchant);
            if(DateQuests.getInstance().isUsingNBTAPI()) {
                setSlot(slots.get(i), DateQuests.getInstance().getNbtAPIController().translateLore(getInventory().getItem(slots.get(i)), q.getTranslatedEvent(), 0));
            }
            i++;
        }
    }

    public void loadRewardsPage() {
        currentOffset = 0;
        pageID = 2;
        loadFrame();
        loadRewards();
    }

    public void loadRewards() {
        loadFrame();
        List<ItemStack> playerRewards = DateQuests.getInstance().getQuestsManager().getPlayersRewards(lookupPlayer);
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
            if(getOwner().hasPermission(DateQuests.getInstance().getPermissionManager().getPermission("lookupRemoveReward"))) {
                ItemMeta im = itemStack.getItemMeta();
                if(im != null) {
                    List<String> lore = im.getLore();
                    if(lore == null) {
                        lore = new ArrayList<>();
                    }
                    lore.add(" ");
                    lore.add(DateQuests.getInstance().getMessagesManager().getMessage("removeRewardClick"));
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
        setSlot(46, Material.ARROW, DateQuests.getInstance().getMessagesManager().getMessage("previousPage"),
                getLore(DateQuests.getInstance().getMessagesManager().getMessage("previousPageLore")));
        setSlot(52, Material.ARROW, DateQuests.getInstance().getMessagesManager().getMessage("nextPage"),
                getLore(DateQuests.getInstance().getMessagesManager().getMessage("nextPageLore")));
    }

    private void nextPage() {
        if((pageID == 1 && DateQuests.getInstance().getQuestsManager().getPlayersQuests(getOwner().getName()).size() - 1 < currentOffset + 28)
                || (pageID == 2 && DateQuests.getInstance().getQuestsManager().getPlayersRewards(getOwner().getName()).size() - 1 < currentOffset + 28)) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset += 28;
        if(pageID == 1) {
            loadQuests();
        } else {
            loadRewards();
        }
    }

    private void previousPage() {
        if(currentOffset == 0) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset -= 28;
        if(pageID == 1) {
            loadQuests();
        } else {
            loadRewards();
        }
    }

}
