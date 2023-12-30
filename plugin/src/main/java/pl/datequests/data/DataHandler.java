package pl.datequests.data;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import pl.beaverlib.data.ItemLoader;
import pl.beaverlib.util.DateManager;
import pl.datequests.DateQuests;
import pl.datequests.permissions.PermissionManager;
import pl.datequests.quests.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class DataHandler {

    private final DateQuests plugin = DateQuests.getInstance();
    private final QuestsManager questsManager = plugin.getQuestsManager();
    private YamlConfiguration data;
    private boolean loadAllPlayers;
    private int saveTask = -1;
    private int saveInterval;
    private boolean saving;
    private boolean loadLeaderboard;
    private final List<String> playerLoaded = new ArrayList<>();

    public void loadAll() {
        loadConfig();
        loadData();
        loadMessages();
        registerSaveTask();
    }

    public void loadConfig() {
        questsManager.getQuestSchemas().clear();
        questsManager.getSpecialReward().clear();
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(getConfigFile());
        loadAllPlayers = yml.getBoolean("config.loadAllPlayers");
        saveInterval = yml.getInt("config.saveInterval", 10);
        if(plugin.isUsingCitizens()) {
            plugin.getCitizensController().setNpcName(yml.getString("npc.name", "DateQuests NPC"));
        }
        if(plugin.isUsingPlaceholders()) {
            loadLeaderboard = yml.getBoolean("config.loadLeaderboard");
            if(loadLeaderboard) {
                plugin.getPlaceholderController().setMaxRecords(yml.getInt("config.leaderboardMaxRecords"));
                plugin.getPlaceholderController().setLoadInterval(yml.getInt("config.leaderboardLoadInterval"));
                plugin.getPlaceholderController().createLoadTask();
            }
        }
        ConfigurationSection permissionSection = yml.getConfigurationSection("permissions");
        if(permissionSection != null) {
            PermissionManager permissionManager = plugin.getPermissionManager();
            for(String permissionName : permissionSection.getKeys(false)) {
                permissionManager.registerPermission(
                        permissionName,
                        yml.getString("permissions." + permissionName),
                        "Permission used by DateQuests plugin");
            }
        }
        ConfigurationSection questsSection = yml.getConfigurationSection("quests");
        if(questsSection != null) {
            for(String questName : questsSection.getKeys(false)) {
                String path = "quests." + questName + ".";
                QuestSchema schema = new QuestSchema();
                schema.setSchemaName(questName);
                QuestInterval interval;
                try {
                    interval = QuestInterval.valueOf(yml.getString(path + "interval", "DAY"));
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
                        QuestGroup questGroup = new QuestGroup();
                        for(String questID : groupQuestSection.getKeys(false)) {
                            String questPath = path + "questGroups." + id + "." + questID + ".";
                            questGroup.getEvents().add(yml.getString(questPath + "event"));
                            questGroup.getRanges().add(yml.getString(questPath + "range"));
                        }
                        schema.getQuestGroups().add(questGroup);
                    }
                }
                schema.setChangeable(yml.getBoolean(path + "changeable"));
                schema.setRewards(ItemLoader.getItemStackList(yml, path + "rewards.items"));
                RewardType rewardType = RewardType.ALL;
                String rewardTypeStr = yml.getString(path + "rewards.rewardType", "ALL");
                try {
                    rewardType = RewardType.valueOf(rewardTypeStr);
                } catch(IllegalArgumentException e) {
                    plugin.getLogger().severe(rewardTypeStr +
                            " is not correct RewardType! Correct RewardType is RANDOM or ALL. Replacing with ALL...");
                }
                schema.setRewardType(rewardType);
                questsManager.addQuestSchema(schema);
            }
        }
        RewardType rewardType = RewardType.ALL;
        String rewardTypeStr = yml.getString("specialReward.rewardType", "ALL");
        try {
            rewardType = RewardType.valueOf(rewardTypeStr);
        } catch(IllegalArgumentException e) {
            plugin.getLogger().severe(rewardTypeStr +
                    " is not correct RewardType! Correct RewardType is RANDOM or ALL. Replacing with ALL...");
        }
        questsManager.setSpecialRewardType(rewardType);
        questsManager.setSpecialReward(ItemLoader.getItemStackList(yml, "specialReward.items"));
        questsManager.setSpecialRewardPercentage(yml.getDouble("specialReward.minimumCompletedPercentage"));
    }

    public void loadData() {
        questsManager.getQuests().clear();
        questsManager.getRewards().clear();
        playerLoaded.clear();
        data = null;
        data = YamlConfiguration.loadConfiguration(getDataFile());
        ConfigurationSection schemaSection = data.getConfigurationSection("schema");
        if(schemaSection != null) {
            for(String schemaName : schemaSection.getKeys(false)) {
                QuestSchema schema = questsManager.getQuestSchema(schemaName);
                if(schema != null) {
                    String path = "schema." + schemaName + ".";
                    schema.setDateTag(data.getString(path + "dateTag"));
                    schema.setTagID(data.getInt(path + "tagID"));
                    ConfigurationSection tagsSection = data.getConfigurationSection("monthTags." + schemaName);
                    if(tagsSection != null) {
                        for(String date : tagsSection.getKeys(false)) {
                            schema.getMonthTags().put(date, data.getIntegerList("monthTags." + schemaName + "." + date));
                        }
                    }
                }
            }
        }
        questsManager.checkTags();
        if(!loadAllPlayers) {
            for(Player p : plugin.getServer().getOnlinePlayers()) {
                loadPlayer(p.getName());
            }
        } else {
            ConfigurationSection playerSection = data.getConfigurationSection("players");
            if(playerSection != null) {
                for(String nickname : playerSection.getKeys(false)) {
                    loadPlayer(nickname);
                }
            }
        }
        if(plugin.isUsingPlaceholders()) {
            plugin.getPlaceholderController().getLeaderboard().clear();
            plugin.getPlaceholderController().getPlayerScore().clear();
            if(loadLeaderboard) {
                ConfigurationSection leaderboardSection = data.getConfigurationSection("leaderboard");
                if(leaderboardSection == null) {
                    return;
                }
                for(String playerName : leaderboardSection.getKeys(false)) {
                    plugin.getPlaceholderController().setScore(playerName, data.getInt("leaderboard." + playerName));
                }
            }
        }
    }

    public void loadPlayer(String nickname) {
        if(playerLoaded.contains(nickname)) {
            return;
        }
        playerLoaded.add(nickname);
        ConfigurationSection schemaNameSection = data.getConfigurationSection("players." + nickname);
        if(schemaNameSection == null) {
            return;
        }
        for(String schemaName : schemaNameSection.getKeys(false)) {
            QuestSchema schema = questsManager.getQuestSchema(schemaName);
            if(schema == null) {
                continue;
            }
            String lastGroupPath = "players." + nickname + "." + schemaName + ".--lastGroup";
            if(data.contains(lastGroupPath)) {
                schema.getLastPlayerGroup().put(nickname, data.getInt(lastGroupPath));
            }
            ConfigurationSection questsSection = data.getConfigurationSection("players." + nickname + "." + schemaName);
            if(questsSection == null) {
                continue;
            }
            for(String dateTag : questsSection.getKeys(false)) {
                if(dateTag.startsWith("--")) {
                    continue;
                }
                String insidePath = "players." + nickname + "." + schemaName + "." + dateTag + ".";
                Quest q = new Quest();
                q.setOwner(nickname);
                q.setQuestSchema(schema);
                q.setDateTag(dateTag);
                q.setTagID(data.getInt(insidePath + "tagID"));
                q.setEvent(data.getString(insidePath + "event", ""));
                q.setProgress(data.getInt(insidePath + "progress"));
                q.setChanged(data.getBoolean(insidePath + "changed"));
                questsManager.assignQuest(nickname, q);
            }
        }
        questsManager.getSpecialRewardReceived().put(nickname, data.getIntegerList("players." + nickname + ".--specialRewards"));
        ConfigurationSection rewardSection = data.getConfigurationSection("players." + nickname + ".--rewards");
        if(rewardSection == null) {
            return;
        }
        for(String key : rewardSection.getKeys(false)) {
            questsManager.assignReward(nickname, ItemLoader.getItemStack(data, "players." + nickname + ".--rewards." + key));
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

    public void registerSaveTask() {
        if(saveTask != -1) {
            plugin.getServer().getScheduler().cancelTask(saveTask);
        }
        saveTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if(saving) {
                return;
            }
            saving = true;
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::save);
        }, 20L * saveInterval, 20L * saveInterval);
    }

    public void save() {
        try {
            data.save(getDataFile());
        } catch(IOException e) {
            plugin.getLogger().severe("Cannot save data.yml: " + e.getMessage());
            e.printStackTrace();
        } finally {
            saving = false;
        }
    }

    public void saveSchemaTags(QuestSchema schema) {
        String path = "schema." + schema.getSchemaName() + ".";
        data.set(path + "dateTag", schema.getDateTag());
        data.set(path + "tagID", schema.getTagID());
        String date = DateManager.getFormattedDate("%Y/%M");
        path = "monthTags." + schema.getSchemaName() + "." + date;
        List<Integer> list = data.getIntegerList(path);
        if(!list.contains(schema.getTagID())) {
            list.add(schema.getTagID());
        }
        schema.getMonthTags().put(date, list);
        data.set(path, list);
    }

    public void saveQuest(Quest quest) {
        String path = "players." + quest.getOwner() + "." + quest.getQuestSchema().getSchemaName() + "." + quest.getDateTag() + ".";
        data.set(path + "tagID", quest.getTagID());
        data.set(path + "event", quest.getEvent());
        if(quest.isChanged()) {
            data.set(path + "changed", quest.isChanged());
        }
        saveQuestProgress(quest);
    }

    public void saveQuestProgress(Quest quest) {
        String path = "players." + quest.getOwner() + "." + quest.getQuestSchema().getSchemaName() + "." + quest.getDateTag() + ".";
        data.set(path + "progress", quest.getProgress());
    }

    public void saveLastPlayerGroup(String player, QuestSchema schema, int group) {
        data.set("players." + player + "." + schema.getSchemaName() + ".--lastGroup", group);
    }

    public void saveReceivedSpecialReward(String player, int month) {
        String path = "players." + player + ".--specialRewards";
        List<Integer> list = data.getIntegerList(path);
        if(!list.contains(month)) {
            list.add(month);
        }
        data.set(path, list);
    }

    public void savePlayerRewards(String player) {
        ItemLoader.parseList(data, "players." + player + ".--rewards", questsManager.getPlayersRewards(player));
    }

    public void saveLeaderboard(String player, int score) {
        data.set("leaderboard." + player, score);
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
