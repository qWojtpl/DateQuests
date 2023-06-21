package pl.datequests.gui.list;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import pl.datequests.DateQuests;
import pl.datequests.gui.PluginGUI;
import pl.datequests.gui.SortType;
import pl.datequests.quests.Quest;
import pl.datequests.quests.QuestSchema;

import java.util.ArrayList;
import java.util.List;

public class QuestPanel extends PluginGUI {

    private QuestSchema questSchema;
    private SortType currentSortType;
    private int currentOffset;
    private List<Integer> slots;
    private final int questIndex;

    public QuestPanel(Player owner, String inventoryName, int questIndex) {
        super(owner, inventoryName, 54);
        this.questIndex = questIndex;
    }

    @Override
    public void onOpen() {
        questSchema = getQuestsManager().getSchemaFromIndex(questIndex);
        setGUIProtected(true);
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
        currentOffset = 0;
        setSlot(0, Material.BOOK, "Quest assign", getLore("New quests will assign every: " + questSchema.getQuestInterval().name()));
        setSlot(9, Material.OAK_SIGN, "Category stats", getLore("Completed quests: 0", "Quests assigned: 12"));
        setSlot(18, Material.CHEST, "Rewards", getLore("Get your rewards!"));
        setSlot(47, Material.ARROW, "§f§lPrevious page", getLore("Go to previous page"));
        setSlot(50, Material.HOPPER, "§f§lSort", getLore("Now sorted by:", " -> §a§lNEWEST ASSIGNED", " §aOLDEST ASSIGNED", " §aCOMPLETED", " §aNOT COMPLETED"));
        setSlot(53, Material.ARROW, "§f§lNext page", getLore("Go to next page"));
        changeSortType();
    }

    @Override
    public void onClick(int slot) {
        if(slot == slots.get(0)) {
            if(currentOffset == 0) {
                if(getQuestsManager().isPlayerCanTakeQuest(getOwner().getName(), questSchema)) {
                    Quest q = new Quest();
                    q.setOwner(getOwner().getName());
                    q.setQuestSchema(questSchema);
                    q.setDateTag(questSchema.getDateTag());
                    q.setTagID(questSchema.getTagID());
                    getQuestsManager().assignQuest(getOwner().getName(), q);
                    closeInventory();
                    getOwner().sendMessage("Accepted quest!");
                }
            }
        } else if(slot == 47) {
            previousPage();
        } else if(slot == 50) {
            changeSortType();
        } else if(slot == 53) {
            nextPage();
        }
    }

    private void loadQuests() {
        slots = new ArrayList<>();
        int[] protectedSlots = new int[]{0, 9, 18, 27, 36, 45};
        for(int i = 2; i <= 45; i++) {
            if(isProtectedSlot(protectedSlots, i)) {
                continue;
            }
            setSlot(i, Material.WHITE_STAINED_GLASS_PANE, "", getLore(""));
            slots.add(i);
        }
        int i = 0;
        if(currentOffset == 0) {
            if(getQuestsManager().isPlayerCanTakeQuest(getOwner().getName(), questSchema)) {
                setSlot(slots.get(i), Material.GRASS_BLOCK, "New quest", getLore("Click to accept"));
                setSlotEnchanted(slots.get(i), true);
                i++;
            }
        }
        for(Quest quest : getQuestsManager().getPlayersQuests(getOwner().getName())) {
            if(currentOffset > i) {
                i++;
                continue;
            }
            if(i > slots.size() - 1) {
                break;
            }
            setSlot(slots.get(i), Material.GRASS_BLOCK, quest.getDateTag(), getLore(""));
            i++;
        }
    }

    private void nextPage() {
        if(getQuestsManager().getPlayersQuests(getOwner().getName()).size() < currentOffset + 36) {
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset += 36;
        loadQuests();
    }

    private void previousPage() {
        if(currentOffset == 0) {
            return;
        }
        getOwner().playSound(getOwner(), Sound.ITEM_BOOK_PAGE_TURN, 1.0F, 1.0F);
        currentOffset -= 36;
        loadQuests();
    }

    private void changeSortType() {
        if(currentSortType == null) {
            currentSortType = SortType.NEWEST;
        } else {
            currentSortType = SortType.getNext(currentSortType);
        }
        currentOffset = 0;
        loadQuests();
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
