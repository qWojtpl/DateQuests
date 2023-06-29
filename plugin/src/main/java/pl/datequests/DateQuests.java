package pl.datequests;

import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pl.datequests.citizens.CitizensController;
import pl.datequests.commands.CommandHelper;
import pl.datequests.commands.Commands;
import pl.datequests.data.DataHandler;
import pl.datequests.data.MessagesManager;
import pl.datequests.events.Events;
import pl.datequests.gui.GUIManager;
import pl.datequests.permissions.PermissionManager;
import pl.datequests.quests.QuestsManager;
import pl.datequests.util.DateManager;

@Getter
public final class DateQuests extends JavaPlugin {

    private static DateQuests main;
    private DateManager dateManager;
    private MessagesManager messagesManager;
    private QuestsManager questsManager;
    private GUIManager guiManager;
    private PermissionManager permissionManager;
    private CitizensController citizensController;
    private DataHandler dataHandler;
    private Commands commands;
    private CommandHelper commandHelper;
    private Events events;

    @Override
    public void onEnable() {
        main = this;
        this.dateManager = new DateManager();
        this.messagesManager = new MessagesManager();
        this.questsManager = new QuestsManager();
        this.guiManager = new GUIManager();
        this.permissionManager = new PermissionManager();
        if(getServer().getPluginManager().getPlugin("Citizens") == null) {
            getLogger().info("Not found Citizens plugin!");
        } else {
            getLogger().info("Found Citizens!");
            this.citizensController = new CitizensController();
            getServer().getPluginManager().registerEvents(citizensController, this);
        }
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
        dataHandler.loadAll();
        questsManager.registerDateCheckTask();
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        if(guiManager != null) {
            guiManager.closeAllInventories();
        }
        dataHandler.save();
        getLogger().info("Disabled.");
    }

    public static DateQuests getInstance() {
        return main;
    }

    public boolean isUsingCitizens() {
        return (citizensController != null);
    }

}
