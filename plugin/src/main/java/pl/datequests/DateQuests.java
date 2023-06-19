package pl.datequests;

import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pl.datequests.commands.CommandHelper;
import pl.datequests.commands.Commands;
import pl.datequests.data.DataHandler;
import pl.datequests.events.Events;
import pl.datequests.gui.GUIManager;
import pl.datequests.quests.QuestsManager;

@Getter
public final class DateQuests extends JavaPlugin {

    private static DateQuests main;
    private QuestsManager questsManager;
    private GUIManager guiManager;
    private DataHandler dataHandler;
    private Commands commands;
    private CommandHelper commandHelper;
    private Events events;

    @Override
    public void onEnable() {
        main = this;
        this.questsManager = new QuestsManager();
        this.guiManager = new GUIManager();
        this.dataHandler = new DataHandler();
        this.commands = new Commands();
        this.commandHelper = new CommandHelper();
        this.events = new Events();
        PluginCommand command = getCommand("datequests");
        if(command != null) {
            command.setExecutor(commands);
            command.setTabCompleter(commandHelper);
        }
        getServer().getPluginManager().registerEvents(events, this);
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
