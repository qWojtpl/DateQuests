package pl.datequests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.datequests.DateQuests;
import pl.datequests.data.DataHandler;
import pl.datequests.data.MessagesManager;
import pl.datequests.gui.list.QuestList;
import pl.datequests.permissions.PermissionManager;

import java.text.MessageFormat;

public class Commands implements CommandExecutor {

    private final DateQuests plugin = DateQuests.getInstance();
    private final DataHandler dataHandler = plugin.getDataHandler();
    private final MessagesManager messages = plugin.getMessagesManager();
    private final PermissionManager permissionManager = plugin.getPermissionManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("reload")) {
                reload(sender, args);
            } else if(args[0].equalsIgnoreCase("load")) {
                loadPlayer(sender, args);
            } else if(args[0].equalsIgnoreCase("lookup")) {
                lookup(sender, args);
            } else if(args[0].equalsIgnoreCase("save")) {
                save(sender);
            } else {
                help(sender);
            }
        } else {
            if(sender instanceof Player) {
                openGUI((Player) sender);
            } else {
                help(sender);
            }
        }
        return true;
    }

    public void help(CommandSender sender) {

    }

    public void reload(CommandSender sender, String[] args) {
        if(!hasPermission(sender, "reload")) {
            return;
        }
        boolean save = true;
        if(args.length > 1) {
            if(args[1].equalsIgnoreCase("--skipsave")) {
                save = false;
            }
        }
        if(save) {
            dataHandler.save();
        }
        dataHandler.loadAll();
        sender.sendMessage(messages.getMessage("reloaded") + " "
                + (save ? messages.getMessage("savedData") : messages.getMessage("skippedSave")));
    }

    public void loadPlayer(CommandSender sender, String[] args) {
        if(!hasPermission(sender, "loadPlayer")) {
            return;
        }
        if(args.length > 1) {
            if(dataHandler.getPlayerLoaded().contains(args[1])) {
                sender.sendMessage(messages.getMessage("playerAlreadyLoaded"));
            } else {
                dataHandler.loadPlayer(args[1]);
                sender.sendMessage(messages.getMessage("loadedPlayer") + args[0]);
            }
        } else {
            correctUsage(sender, "/dq load <player>");
        }
    }

    public void lookup(CommandSender sender, String[] args) {

    }

    public void save(CommandSender sender) {
        if(!hasPermission(sender, "save")) {
            return;
        }
        sender.sendMessage(messages.getMessage("saved"));
    }

    public void openGUI(Player sender) {
        new QuestList(sender, messages.getMessage("guiTitle"));
    }

    private void correctUsage(CommandSender sender, String usage) {
        sender.sendMessage(MessageFormat.format(messages.getMessage("correctUsage"), usage));
    }

    private boolean hasPermission(CommandSender sender, String permissionKey) {
        if(sender.hasPermission(permissionManager.getPermission(permissionKey))) {
            return true;
        }
        sender.sendMessage(messages.getMessage("noPermission"));
        return false;
    }

}
