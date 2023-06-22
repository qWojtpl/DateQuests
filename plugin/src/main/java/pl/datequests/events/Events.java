package pl.datequests.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import pl.datequests.DateQuests;
import pl.datequests.gui.GUIManager;
import pl.datequests.gui.PluginGUI;
import pl.datequests.quests.Quest;
import pl.datequests.quests.QuestsManager;

import java.util.ArrayList;
import java.util.List;

public class Events implements Listener {

    private final DateQuests plugin = DateQuests.getInstance();
    private final QuestsManager questsManager = plugin.getQuestsManager();
    private final GUIManager guiManager = plugin.getGuiManager();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if(event.isCancelled()) {
            return;
        }
        String player = event.getPlayer().getName();
        List<Quest> playersActiveQuests = questsManager.getPlayersActiveQuests(player);
        for(Quest q : playersActiveQuests) {
            if(questsManager.isQuestForEvent(q, "break", event.getBlock().getType().name())) {
                questsManager.addProgress(q, 1);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        PluginGUI gui = guiManager.getGUIByInventory(event.getClickedInventory());
        if(gui == null) {
            return;
        }
        gui.onClick(event.getSlot());
        if(gui.isGuiProtected()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        PluginGUI gui = guiManager.getGUIByInventory(event.getInventory());
        if(gui == null) {
            return;
        }
        if(gui.isGuiProtected()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        guiManager.removeInventory(guiManager.getGUIByInventory(event.getInventory()));
    }

}
