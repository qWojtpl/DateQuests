package pl.datequests.permissions;

import lombok.Getter;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import pl.datequests.DateQuests;

import java.util.HashMap;

@Getter
public class PermissionManager {

    private final DateQuests plugin = DateQuests.getInstance();
    private final HashMap<String, Permission> permissions = new HashMap<>();

    public void registerPermission(String key, String name, String description) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        if(pluginManager.getPermission(name) != null) {
            pluginManager.removePermission(name);
        }
        Permission perm = new Permission(name, description);
        pluginManager.addPermission(perm);
        permissions.put(key, perm);
    }

    public Permission getPermission(String key) {
        return permissions.getOrDefault(key, new Permission(""));
    }

}
