package pl.datequests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.datequests.gui.list.QuestList;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0) {
            if(args[0].equalsIgnoreCase("reload")) {
                if(args.length > 1) {
                    if(args[1].equalsIgnoreCase("--skipsave")) {
                        c_Reload(false);
                        return true;
                    }
                }
                c_Reload(true);
            }
        }
        if(sender instanceof Player) {
            new QuestList((Player) sender, "test");
        }
        return true;
    }

    public void c_Reload(boolean save) {

    }

}
