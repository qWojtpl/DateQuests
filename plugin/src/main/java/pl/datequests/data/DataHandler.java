package pl.datequests.data;

import pl.datequests.DateQuests;

import java.io.File;

public class DataHandler {

    private final DateQuests plugin = DateQuests.getInstance();

    public void loadConfig() {

    }

    public File getConfigFile() {
        return getFile("config.yml");
    }

    public File getFile(String resourceName) {
        File file = new File(plugin.getDataFolder(), resourceName);
        if(!file.exists()) {
            plugin.saveResource(resourceName, false);
        }
        return file;
    }

}
