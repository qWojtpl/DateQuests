package pl.datequests.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.datequests.DateQuests;
import pl.datequests.quests.Quest;
import pl.datequests.quests.QuestInterval;
import pl.datequests.quests.QuestSchema;
import pl.datequests.quests.QuestsManager;

import java.io.File;
import java.io.IOException;

public class DataHandler {

    private final DateQuests plugin = DateQuests.getInstance();
    private final QuestsManager questsManager = plugin.getQuestsManager();
    private YamlConfiguration data;

    public void loadAll() {
        loadConfig();
        loadData();
        loadMessages();
    }

    public void loadConfig() {
        questsManager.getQuestSchemas().clear();
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
                schema.setIcon(ItemLoader.getItemStack(yml, path + "icon"));
                questsManager.addQuestSchema(schema);
            }
        }
    }

    public void loadData() {
        data = YamlConfiguration.loadConfiguration(getDataFile());
        ConfigurationSection schemaSection = data.getConfigurationSection("schema");
        if(schemaSection != null) {
            for(String schemaName : schemaSection.getKeys(false)) {
                QuestSchema schema = questsManager.getQuestSchema(schemaName);
                if(schema != null) {
                    String path = "schema." + schemaName + ".";
                    schema.setDateTag(data.getString(path + "dateTag"));
                    schema.setTagID(data.getInt(path + "tagID"));
                }
            }
        }
        ConfigurationSection playerSection = data.getConfigurationSection("players");
        if(playerSection != null) {
            for(String nickname : playerSection.getKeys(false)) {
                ConfigurationSection schemaNameSection = data.getConfigurationSection("players." + nickname);
                if(schemaNameSection == null) {
                    continue;
                }
                for(String schemaName : schemaNameSection.getKeys(false)) {
                    QuestSchema schema = questsManager.getQuestSchema(schemaName);
                    if(schema == null) {
                        continue;
                    }
                    ConfigurationSection questsSection = data.getConfigurationSection("players." + nickname + "." + schemaName);
                    if(questsSection == null) {
                        continue;
                    }
                    for(String dateTag : questsSection.getKeys(false)) {
                        Quest q = new Quest();
                        q.setOwner(nickname);
                        q.setQuestSchema(schema);
                        q.setDateTag(dateTag);
                        q.setTagID(data.getInt("players." + nickname + "." + schemaName + "." + dateTag));
                        questsManager.assignQuest(nickname, q);
                    }
                }
            }
        }
    }

    public void loadMessages() {

    }

    public void save() {
        try {
            data.save(getDataFile());
        } catch(IOException e) {
            plugin.getLogger().severe("Cannot save data.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveSchemaTags(QuestSchema schema) {
        String path = "schema." + schema.getSchemaName() + ".";
        data.set(path + "dateTag", schema.getDateTag());
        data.set(path + "tagID", schema.getTagID());
    }

    public void saveQuest(Quest quest) {
        String path = "players." + quest.getOwner() + "." + quest.getQuestSchema().getSchemaName() + "." + quest.getDateTag() + ".";
        data.set(path + "tagID", quest.getTagID());
    }

    public File getConfigFile() {
        return getFile("config.yml");
    }

    public File getDataFile() {
        return getFile("data.yml");
    }

    public File getFile(String resourceName) {
        File file = new File(plugin.getDataFolder(), resourceName);
        if(!file.exists()) {
            plugin.saveResource(resourceName, false);
        }
        return file;
    }

}
