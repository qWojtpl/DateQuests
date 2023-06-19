package pl.datequests.gui;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final List<PluginGUI> guiList = new ArrayList<>();

    public void registerInventory(PluginGUI gui) {
        guiList.add(gui);
    }

    public void removeInventory(PluginGUI gui) {
        guiList.remove(gui);
    }

    public void closeAllInventories() {
        for(PluginGUI gui : guiList) {
            gui.getOwner().closeInventory();
        }
    }

}
