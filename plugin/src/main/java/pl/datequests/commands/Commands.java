package pl.datequests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.datequests.DateQuests;
import pl.datequests.data.DataHandler;
import pl.datequests.data.MessagesManager;
import pl.datequests.gui.list.QuestList;

public class Commands implements CommandExecutor {

    private final DateQuests plugin = DateQuests.getInstance();
    private final DataHandler dataHandler = plugin.getDataHandler();
    private final MessagesManager messages = plugin.getMessagesManager();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("reload")) {
                if(args.length > 1) {
                    if(args[1].equalsIgnoreCase("--skipsave")) {
                        c_Reload(sender, false);
                        return true;
                    }
                }
                c_Reload(sender, true);
                return true;
            }
        }
        if(sender instanceof Player) {
            openGUI((Player) sender);
        }
        return true;
    }

    public void c_Reload(CommandSender sender, boolean save) {
        if(save) {
            dataHandler.save();
        }
        dataHandler.loadAll();
        sender.sendMessage(messages.getMessage("reloaded") + (save ? " (Saved all data)" : " (Skipped saving)"));
    }

    public void openGUI(Player sender) {
        new QuestList(sender, messages.getMessage("guiTitle"));
    }

}
