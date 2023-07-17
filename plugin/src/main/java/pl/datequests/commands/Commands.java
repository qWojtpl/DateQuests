package pl.datequests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import pl.datequests.DateQuests;
import pl.datequests.data.DataHandler;
import pl.datequests.data.MessagesManager;
import pl.datequests.gui.list.AdminPanel;
import pl.datequests.gui.list.QuestList;
import pl.datequests.permissions.PermissionManager;

import java.text.MessageFormat;

public class Commands implements CommandExecutor {

    private final DateQuests plugin = DateQuests.getInstance();
    private final DataHandler dataHandler = plugin.getDataHandler();
    private final MessagesManager messages = plugin.getMessagesManager();
    private final PermissionManager permissionManager = plugin.getPermissionManager();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("reload")) {
                reload(sender, args);
            } else if(args[0].equalsIgnoreCase("load")) {
                loadPlayer(sender, args);
            } else if(args[0].equalsIgnoreCase("lookup")) {
                lookup(sender, args);
            } else if(args[0].equalsIgnoreCase("save")) {
                save(sender);
            } else if(args[0].equalsIgnoreCase("serialize")) {
                serialize(sender);
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
        sender.sendMessage("§6{========================}");
        sender.sendMessage(" ");
        boolean haveAnyCommand = false;
        if(sender.hasPermission(permissionManager.getPermission("reload"))) {
            haveAnyCommand = true;
            sender.sendMessage(" §6/§edq reload §6- §2Reloads configuration and saves data.");
            sender.sendMessage(" §6/§edq reload §f--skipsave §6- §2Reloads configuration but loading data from file.");
        }
        if(sender.hasPermission(permissionManager.getPermission("load"))) {
            haveAnyCommand = true;
            sender.sendMessage(" §6/§edq load §f<player> §6- §2Loads player's data.");
        }
        if(sender.hasPermission(permissionManager.getPermission("lookup"))) {
            haveAnyCommand = true;
            sender.sendMessage(" §6/§edq lookup §f<player> §6- §2Lookups about player's data (rewards, quests).");
        }
        if(sender.hasPermission(permissionManager.getPermission("save"))) {
            haveAnyCommand = true;
            sender.sendMessage(" §6/§edq save §6- §2Saves data into file, not reloading configuration.");
        }
        if(sender.hasPermission(permissionManager.getPermission("serialize"))) {
            haveAnyCommand = true;
            sender.sendMessage(" §6/§edq serialize §6- §2Serialize item in your hand");
        }
        if(!haveAnyCommand) {
            sender.sendMessage(" " + messages.getMessage("noAccessToSubcommands"));
        }
        sender.sendMessage(" ");
        sender.sendMessage("§6{========================}");
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
                sender.sendMessage(messages.getMessage("loadedPlayer") + args[1]);
            }
        } else {
            correctUsage(sender, "/dq load <player>");
        }
    }

    public void lookup(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(messages.getMessage("mustBePlayer"));
            return;
        }
        if(!hasPermission(sender, "lookup")) {
            return;
        }
        if(args.length > 1) {
            if(!dataHandler.getPlayerLoaded().contains(args[1])) {
                sender.sendMessage(messages.getMessage("playerNotLoaded"));
            } else {
                new AdminPanel((Player) sender, messages.getMessage("lookupGUI") + args[1], args[1]);
            }
        } else {
            correctUsage(sender, "/dq lookup <player>");
        }
    }

    public void save(CommandSender sender) {
        if(!hasPermission(sender, "save")) {
            return;
        }
        sender.sendMessage(messages.getMessage("saved"));
        dataHandler.save();
    }

    public void serialize(CommandSender sender) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(messages.getMessage("mustBePlayer"));
            return;
        }
        if(!hasPermission(sender, "serialize")) {
            return;
        }
        Player p = (Player) sender;
        ItemStack is = p.getInventory().getItemInMainHand();
        if(is.getItemMeta() != null) {
            sender.sendMessage("§2Meta: §6" + is.getItemMeta());
            dataHandler.getData().set("lastSerialization", is.getItemMeta());
        } else {
            sender.sendMessage("§c-");
        }
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
