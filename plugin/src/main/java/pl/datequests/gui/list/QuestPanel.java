package pl.datequests.gui.list;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.datequests.gui.PluginGUI;
import pl.datequests.gui.SortType;

public class QuestPanel extends PluginGUI {

    private final int questIndex;
    private SortType currentSortType;
    private int currentOffset;

    public QuestPanel(Player owner, String inventoryName, int questIndex) {
        super(owner, inventoryName, 54);
        this.questIndex = questIndex;
    }

    @Override
    public void onOpen() {
        setGUIProtected(true);
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
        currentOffset = 0;
        setSlot(0, Material.BOOK, "Quest assign", getLore("New quests will assign every: DAY"));
        setSlot(9, Material.OAK_SIGN, "Category stats", getLore("Completed quests: 0", "Quests assigned: 12"));
        setSlot(18, Material.CHEST, "Rewards", getLore("Get your rewards!"));
        setSlot(47, Material.ARROW, "§f§lPrevious page", getLore("Go to previous page"));
        setSlot(50, Material.HOPPER, "§f§lSort", getLore("Now sorted by:", " -> §a§lNEWEST ASSIGNED", " §aOLDEST ASSIGNED", " §aCOMPLETED", " §aNOT COMPLETED"));
        setSlot(53, Material.ARROW, "§f§lNext page", getLore("Go to next page"));
        changeSortType();
    }

    @Override
    public void onClick(int slot) {
        if(slot == 47) {
            previousPage();
        } else if(slot == 50) {
            changeSortType();
        } else if(slot == 53) {
            nextPage();
        }
    }

    private void loadQuests() {
        int[] protectedSlots = new int[]{0, 9, 18, 27, 36, 45};
        for(int i = 2; i <= 45; i++) {
            if(isProtectedSlot(protectedSlots, i)) {
                continue;
            }
            setSlot(i, Material.WHITE_STAINED_GLASS_PANE, "", getLore(""));
        }
    }

    private void nextPage() {
        currentOffset += 36;
        loadQuests();
    }

    private void previousPage() {
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
