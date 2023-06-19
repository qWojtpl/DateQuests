package pl.datequests.gui.list;

import org.bukkit.entity.Player;
import pl.datequests.gui.PluginGUI;

public class QuestList extends PluginGUI {

    public QuestList(Player owner, String inventoryName, int inventorySize) {
        super(owner, inventoryName, inventorySize);
    }

    @Override
    public void onOpen() {
        setGUIProtected(true);

    }

    @Override
    public void onClose() {

    }

    @Override
    public void onClick(int slot) {

    }

}
