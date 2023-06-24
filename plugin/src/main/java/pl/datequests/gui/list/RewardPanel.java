package pl.datequests.gui.list;

import org.bukkit.entity.Player;
import pl.datequests.gui.PluginGUI;

public class RewardPanel extends PluginGUI {

    public RewardPanel(Player owner, String inventoryName) {
        super(owner, inventoryName, 54);
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClick(int slot) {
        setGUIProtected(true);
    }

}
