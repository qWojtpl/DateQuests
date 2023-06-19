package pl.datequests;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import pl.datequests.data.DataHandler;
import pl.datequests.gui.GUIManager;
import pl.datequests.quests.QuestsManager;

@Getter
public final class DateQuests extends JavaPlugin {

    private static DateQuests main;
    private QuestsManager questsManager;
    private GUIManager guiManager;
    private DataHandler dataHandler;

    @Override
    public void onEnable() {
        main = this;
        this.questsManager = new QuestsManager();
        this.guiManager = new GUIManager();
        this.dataHandler = new DataHandler();
        dataHandler.loadConfig();
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        guiManager.closeAllInventories();
        getLogger().info("Disabled.");
    }

    public static DateQuests getInstance() {
        return main;
    }

}
