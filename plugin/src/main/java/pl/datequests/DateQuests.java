package pl.datequests;

import org.bukkit.plugin.java.JavaPlugin;

public final class DateQuests extends JavaPlugin {

    private static DateQuests main;

    @Override
    public void onEnable() {
        main = this;
        getLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled.");
    }

    public static DateQuests getInstance() {
        return main;
    }

}
