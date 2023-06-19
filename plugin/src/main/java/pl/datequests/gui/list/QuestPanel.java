package pl.datequests.gui.list;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.datequests.gui.PluginGUI;

public class QuestPanel extends PluginGUI {

    private final int questIndex;

    public QuestPanel(Player owner, String inventoryName, int inventorySize, int questIndex) {
        super(owner, inventoryName, inventorySize);
        this.questIndex = questIndex;
    }

    @Override
    public void onOpen() {
        setGUIProtected(true);
        fillWith(Material.BLACK_STAINED_GLASS_PANE);
        int[] protectedSlots = new int[]{0, 9, 18, 27, 36, 48};
        for(int i = 2; i <= 45; i++) {
            if(isProtectedSlot(protectedSlots, i)) {
                continue;
            }
            setSlot(i, Material.WHITE_STAINED_GLASS_PANE, "", getLore(""));
        }
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
