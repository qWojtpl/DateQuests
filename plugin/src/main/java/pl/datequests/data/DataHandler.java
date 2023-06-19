package pl.datequests.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.datequests.DateQuests;
import pl.datequests.quests.QuestInterval;
import pl.datequests.quests.QuestSchema;
import pl.datequests.quests.QuestsManager;

import java.io.File;

public class DataHandler {

    private final DateQuests plugin = DateQuests.getInstance();
    private final QuestsManager questsManager = plugin.getQuestsManager();

    public void loadConfig() {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(getConfigFile());
        ConfigurationSection questsSection = yml.getConfigurationSection("quests");
        if(questsSection != null) {
            for(String questName : questsSection.getKeys(false)) {
                String path = "quests." + questName + ".";
                QuestSchema schema = new QuestSchema();
                schema.setSchemaName(questName);
                QuestInterval interval;
                try {
                    interval = QuestInterval.valueOf(yml.getString(path + "interval"));
                } catch(IllegalArgumentException e) {
                    plugin.getLogger().severe(
                            "Cannot compare " + yml.getString(path + "interval") + " with a correct quest interval!");
                    continue;
                }
                schema.setQuestInterval(interval);
                schema.setChangeQuestItem(ItemLoader.getItemStack(yml, path + "changeQuestItem"));
                questsManager.addQuestSchema(schema);
            }
        }
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
