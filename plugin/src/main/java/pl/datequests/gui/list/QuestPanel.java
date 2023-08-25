package pl.datequests.gui.list;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pl.datequests.DateQuests;
import pl.datequests.gui.PluginGUI;
import pl.datequests.gui.SortType;
import pl.datequests.quests.*;
import pl.datequests.util.RandomNumber;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class QuestPanel extends PluginGUI {

    private QuestSchema questSchema;
    private SortType currentSortType;
    private int currentOffset;
    private List<Integer> slots;
    private Material lastRandomizedMaterial;
    private final int questIndex;

    public QuestPanel(Player owner, String inventoryName, int questIndex) {
        super(owner, inventoryName, 54);
        this.questIndex = questIndex;
        onLoad();
    }

    public void onLoad() {
        questSchema = getQuestsManager().getSchemaFromIndex(questIndex);
        setGUIProtected(true);
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
        currentOffset = 0;
        int completed = 0;
        int allCompleted = 0;
        for(Quest q : getQuestsManager().getPlayersQuestsBySchema(getOwner().getName(), questSchema)) {
            if(q.getQuestState().equals(QuestState.COMPLETED)) {
                completed++;
            }
        }
        for(Quest q : getQuestsManager().getPlayersQuests(getOwner().getName())) {
            if(q.getQuestState().equals(QuestState.COMPLETED)) {
                allCompleted++;
            }
        }
        String assign = getMessages().getMessage("everyDay");
        if(questSchema.getQuestInterval().equals(QuestInterval.MONTH)) {
            assign = getMessages().getMessage("everyMonth");
        } else if(!questSchema.getQuestInterval().equals(QuestInterval.DAY)) {
            assign = getMessages().getMessage("day" + questSchema.getQuestInterval().name());
        }
        double completedQuests = 0;
        double allQuests = 0;
        for(QuestSchema schema : getQuestsManager().getQuestSchemas()) {
            String month = DateQuests.getInstance().getDateManager().getFormattedDate("%Y/%M");
            if(!schema.getMonthTags().containsKey(month)) {
                continue;
            }
            List<Integer> monthTags = schema.getMonthTags().get(month);
            allQuests += monthTags.size();
            List<Quest> playerQuests = getQuestsManager().getPlayersQuestsBySchema(getOwner().getName(), schema);
            for(int tag : monthTags) {
                for(Quest q : playerQuests) {
                    if(q.getTagID() == tag && q.getQuestState().equals(QuestState.COMPLETED)) {
                        completedQuests++;
                        break;
                    }
                }
            }
        }
        if(allQuests == 0) {
            allQuests = 1;
        }
        int percent = (int) (completedQuests / allQuests * 100);
        setSlot(0, Material.BOOK, getMessages().getMessage("questAssign"),
                getLore(getMessages().getMessage("questAssignLore") + assign));
        setSlot(9, Material.OAK_SIGN, getMessages().getMessage("statsName"), getLore(
                getMessages().getMessage("statsPercentCompleted") + percent + "%",
                getMessages().getMessage("statsCompleted") + completed,
                getMessages().getMessage("statsAllCompleted") + allCompleted,
                getMessages().getMessage("statsAssigned") +
                        getQuestsManager().getPlayersQuestsBySchema(getOwner().getName(), questSchema).size(),
                getMessages().getMessage("statsAllAssigned") +
                        getQuestsManager().getPlayersQuests(getOwner().getName()).size()));
        setSlot(18, Material.CHEST, getMessages().getMessage("rewards"),
                getLore(getMessages().getMessage("rewardsLore")));
        ItemStack changeItem = questSchema.getChangeQuestItem();
        if(questSchema.isChangeable()) {
            String name = changeItem.getType().name();
            if(changeItem.getItemMeta() != null) {
                if(!changeItem.getItemMeta().getDisplayName().equals("")) {
                    name = changeItem.getItemMeta().getDisplayName() + " (" + changeItem.getType().name() + ")";
                }
            }
            setSlot(27, Material.ENDER_EYE, getMessages().getMessage("changeQuest"), getLore(
                    MessageFormat.format(getMessages().getMessage("changeQuestLore"), name, changeItem.getAmount())
            ));
        }
        setSlot(36, Material.DARK_OAK_DOOR, getMessages().getMessage("backToCategory"),
                getLore(getMessages().getMessage("backToCategoryLore")));
        setSlot(47, Material.ARROW, getMessages().getMessage("previousPage"),
                getLore(getMessages().getMessage("previousPageLore")));
        setSlot(53, Material.ARROW, getMessages().getMessage("nextPage"),
                getLore(getMessages().getMessage("nextPageLore")));
        changeSortType();
    }

    @Override
    public void onClick(int slot) {
        if(slot == slots.get(0)) {
            if(currentOffset == 0) {
                if(getQuestsManager().takeQuest(getOwner().getName(), questSchema)) {
                    closeInventory();
                }
            }
        } else if(slot == 18) {
            closeInventory();
            new RewardPanel(getOwner(), getInventoryName(), questIndex);
        } else if(slot == 27) {
            changeEvent();
        } else if(slot == 36) {
            closeInventory();
            new QuestList(getOwner(), getInventoryName());
        } else if(slot == 47) {
            previousPage();
        } else if(slot == 50) {
            changeSortType();
        } else if(slot == 53) {
            nextPage();
        }
    }

    @Override
    public void onUpdate() {
        List<Material> materials = new ArrayList<>();
        for(QuestGroup group : questSchema.getQuestGroups()) {
            for(String event : group.getEvents()) {
                materials.add(getQuestsManager().getEventMaterial(event));
            }
        }
        Material randomizedMaterial = materials.get(RandomNumber.randomInt(0, materials.size() - 1));
        if(lastRandomizedMaterial != null) {
            int c = 0;
            while(lastRandomizedMaterial.equals(randomizedMaterial)) {
                if(c >= 15) {
                    break;
                }
                randomizedMaterial = materials.get(RandomNumber.randomInt(0, materials.size() - 1));
                c++;
            }
        }
        lastRandomizedMaterial = randomizedMaterial;
        setSlot(slots.get(0), randomizedMaterial,
                getMessages().getMessage("newQuest"), getLore(getMessages().getMessage("clickToTakeNewQuest")));
        setSlotEnchanted(slots.get(0), true);
    }

    private void loadQuests() {
        setGUIUpdating(false);
        slots = new ArrayList<>();
        int[] protectedSlots = new int[]{0, 9, 18, 27, 36, 45};
        for(int i = 2; i <= 45; i++) {
            if(isProtectedSlot(protectedSlots, i)) {
                continue;
            }
            setSlot(i, Material.WHITE_STAINED_GLASS_PANE, " ", getLore(""));
            slots.add(i);
        }
        int i = 0;
        if(currentOffset == 0) {
            if(getQuestsManager().isPlayerCanTakeQuest(getOwner().getName(), questSchema)) {
                setUpdateInterval(15);
                setGUIUpdating(true);
                i++;
            }
        }
        List<Quest> questList = getQuestsManager().getSortedQuests(
                getQuestsManager().getPlayersQuestsBySchema(getOwner().getName(), questSchema),
                currentSortType);
        int j = 0;
        for(Quest quest : questList) {
            if(currentOffset > j) {
                j++;
                continue;
            }
            if(i > slots.size() - 1) {
                break;
            }
            boolean enchant = false;
            Material m = getQuestsManager().getEventMaterial(quest.getEvent());
            String status = getMessages().getMessage("questCompleted");
            if(quest.getQuestState().equals(QuestState.NOT_ACTIVE)) {
                status = getMessages().getMessage("questNotActive");
                m = Material.RED_CONCRETE;
            } else if(quest.getQuestState().equals(QuestState.NOT_COMPLETED)) {
                status = getMessages().getMessage("questNotCompleted");
                enchant = true;
            }
            setSlot(slots.get(i), m, "§6" + quest.getDateTag(),
                    getLore("§2" + quest.getTranslatedEvent(),
                            MessageFormat.format(getMessages().getMessage("progress"), quest.getProgress(), quest.getRequiredProgress()),
                            status));
            setSlotEnchanted(slots.get(i), enchant);
            updateLoreForNBT(slots.get(i), quest.getTranslatedEvent(),0);
            i++;

        }
    }

    private void nextPage() {
        if(getQuestsManager().getPlayersQuestsBySchema(getOwner().getName(), questSchema).size() - 1 < currentOffset + 36) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset += 35;
        if(getQuestsManager().isPlayerCanTakeQuest(getOwner().getName(), questSchema)) {
            currentOffset -= 1;
        }
        loadQuests();
    }

    private void previousPage() {
        if(currentOffset == 0) {
            getOwner().playSound(getOwner(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.5F);
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset -= 35;
        if(getQuestsManager().isPlayerCanTakeQuest(getOwner().getName(), questSchema)) {
            currentOffset += 1;
        }
        loadQuests();
    }

    private void changeSortType() {
        if(currentSortType == null) {
            currentSortType = SortType.NEWEST;
        } else {
            getOwner().playSound(getOwner(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
            currentSortType = SortType.getNext(currentSortType);
        }
        int index = SortType.getIndex(currentSortType);
        setSlot(50, Material.HOPPER, getMessages().getMessage("sort"), getLore(
                getMessages().getMessage("nowSorted"),
                (index == 0 ? "§a§l" : "§6") + getMessages().getMessage("newestAssignedSort"),
                (index == 1 ? "§a§l" : "§6") + getMessages().getMessage("oldestAssignedSort"),
                (index == 2 ? "§a§l" : "§6") + getMessages().getMessage("completedSort"),
                (index == 3 ? "§a§l" : "§6") + getMessages().getMessage("notCompletedSort")));
        currentOffset = 0;
        loadQuests();
    }

    private void changeEvent() {
        if(!questSchema.isChangeable()) {
            return;
        }
        getQuestsManager().changeActiveQuestEvent(getOwner().getName(), questSchema);
        closeInventory();
    }

    private boolean isProtectedSlot(int[] array, int value) {
        for(int i : array) {
            if(i == value || i + 1 == value) {
                return true;
            }
        }
        return false;
    }

}