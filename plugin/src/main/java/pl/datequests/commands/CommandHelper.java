package pl.datequests.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import pl.datequests.DateQuests;
import pl.datequests.permissions.PermissionManager;

import java.util.ArrayList;
import java.util.List;

public class CommandHelper implements TabCompleter {

    private final DateQuests plugin = DateQuests.getInstance();
    private final PermissionManager permissionManager = plugin.getPermissionManager();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length == 1) {
            if(hasPermission(sender, "reload")) {
                completions.add("reload");
            }
            if(hasPermission(sender, "loadPlayer")) {
                completions.add("load");
            }
            if(hasPermission(sender, "lookup")) {
                completions.add("lookup");
            }
            if(hasPermission(sender, "save")) {
                completions.add("save");
            }
        } else if(args.length == 2) {
            if(args[0].equalsIgnoreCase("reload") && hasPermission(sender, "reload")) {
                completions.add("--skipsave");
            } else if((args[0].equalsIgnoreCase("load") && hasPermission(sender, "loadPlayer")) ||
                    (args[0].equalsIgnoreCase("lookup") && hasPermission(sender, "lookup"))) {
                for(Player p : plugin.getServer().getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        }
        return StringUtil.copyPartialMatches(args[args.length-1], completions, new ArrayList<>());
    }

    private boolean hasPermission(CommandSender sender, String permissionKey) {
        return sender.hasPermission(permissionManager.getPermission(permissionKey));
    }

}
