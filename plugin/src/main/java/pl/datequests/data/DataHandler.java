package pl.datequests.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.datequests.DateQuests;
import pl.datequests.quests.*;

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
        questsManager.getQuests().clear();
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
                ConfigurationSection groupSection = yml.getConfigurationSection(path + "questGroups");
                if(groupSection != null) {
                    for(String id : groupSection.getKeys(false)) {
                        ConfigurationSection groupQuestSection = yml.getConfigurationSection(path + "questGroups." + id);
                        if(groupQuestSection == null) {
                            continue;
                        }
                        for(String questID : groupQuestSection.getKeys(false)) {
                            QuestGroup questGroup = new QuestGroup();
                            String questPath = path + "questGroups." + id + "." + questID + ".";
                            questGroup.getEvents().add(yml.getString(questPath + "event"));
                            questGroup.getRanges().add(yml.getString(questPath + "range"));
                            schema.getQuestGroups().add(questGroup);
                        }
                    }
                }
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
                        String insidePath = "players." + nickname + "." + schemaName + "." + dateTag + ".";
                        Quest q = new Quest();
                        q.setOwner(nickname);
                        q.setQuestSchema(schema);
                        q.setDateTag(dateTag);
                        q.setTagID(data.getInt(insidePath + "tagID"));
                        q.setEvent(data.getString(insidePath + "event", ""));
                        q.setProgress(data.getInt(insidePath + "progress"));
                        questsManager.assignQuest(nickname, q);
                    }
                }
            }
        }
    }

    public void loadMessages() {
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(getMessagesFile());
        ConfigurationSection messagesSection = yml.getConfigurationSection("messages");
        if(messagesSection == null) {
            return;
        }
        MessagesManager messagesManager = plugin.getMessagesManager();
        for(String key : messagesSection.getKeys(false)) {
            messagesManager.addMessage(key, yml.getString("messages." + key));
        }
    }

    public void save() {
        for(QuestSchema schema : questsManager.getQuestSchemas()) {
            saveSchemaTags(schema);
        }
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
        data.set(path + "event", quest.getEvent());
        saveQuestProgress(quest);
    }

    public void saveQuestProgress(Quest quest) {
        String path = "players." + quest.getOwner() + "." + quest.getQuestSchema().getSchemaName() + "." + quest.getDateTag() + ".";
        data.set(path + "progress", quest.getProgress());
    }

    public File getConfigFile() {
        return getFile("config.yml");
    }

    public File getDataFile() {
        return getFile("data.yml");
    }

    public File getMessagesFile() {
        return getFile("messages.yml");
    }

    public File getFile(String resourceName) {
        File file = new File(plugin.getDataFolder(), resourceName);
        if(!file.exists()) {
            plugin.saveResource(resourceName, false);
        }
        return file;
    }

}
