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
import pl.datequests.nbtapi.NBTAPIController;
import pl.datequests.permissions.PermissionManager;
import pl.datequests.placeholders.PlaceholderController;
import pl.datequests.quests.QuestsManager;

@Getter
public final class DateQuests extends JavaPlugin {

    private static DateQuests main;
    private MessagesManager messagesManager;
    private QuestsManager questsManager;
    private PermissionManager permissionManager;
    private CitizensController citizensController;
    private PlaceholderController placeholderController;
    private NBTAPIController nbtAPIController;
    private DataHandler dataHandler;
    private Commands commands;
    private CommandHelper commandHelper;
    private Events events;

    @Override
    public void onEnable() {
        main = this;
        this.messagesManager = new MessagesManager();
        this.questsManager = new QuestsManager();
        this.permissionManager = new PermissionManager();
        if(getServer().getPluginManager().getPlugin("Citizens") == null) {
            getLogger().info("Not found Citizens plugin!");
        } else {
            getLogger().info("Found Citizens!");
            this.citizensController = new CitizensController();
            getServer().getPluginManager().registerEvents(citizensController, this);
        }
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("Not found PlaceholderAPI plugin!");
        } else {
            getLogger().info("Found PlaceholderAPI!");
            this.placeholderController = new PlaceholderController();
            placeholderController.register();
        }
        if(getServer().getPluginManager().getPlugin("NBTAPI") == null) {
            getLogger().info("Not found NBTAPI!");
        } else {
            getLogger().info("Found NBTAPI!");
            this.nbtAPIController = new NBTAPIController();
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
        dataHandler.save();
        getLogger().info("Disabled.");
    }

    public static DateQuests getInstance() {
        return main;
    }

    public boolean isUsingCitizens() {
        return (citizensController != null);
    }

    public boolean isUsingPlaceholders() {
        return (placeholderController != null);
    }

    public boolean isUsingNBTAPI() {
        return (nbtAPIController != null);
    }

}
