package pl.datequests.quests;

import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import pl.datequests.DateQuests;
import pl.datequests.util.PlayerUtil;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class QuestsManager {

    private final DateQuests plugin = DateQuests.getInstance();
    private final List<QuestSchema> questSchemas = new ArrayList<>();
    private final HashMap<String, List<Quest>> quests = new HashMap<>();
    private int dateCheckTask = -1;

    public void addQuestSchema(QuestSchema schema) {
        questSchemas.add(schema);
    }

    public void addProgress(Quest quest, int progress) {
        if(!quest.getQuestState().equals(QuestState.NOT_COMPLETED)) {
            return;
        }
        quest.setProgress(quest.getProgress() + progress);
        Player p = PlayerUtil.getPlayer(quest.getOwner());
        if(p != null) {
            PlayerUtil.sendActionBarMessage(p, "§aYou scored in quest " + quest.getQuestSchema().getSchemaName());
        }
        if(quest.getProgress() >= quest.getRequiredProgress()) {
            quest.setQuestState(QuestState.COMPLETED);
        }
        quest.save();
    }

    public void assignQuest(String player, Quest quest) {
        List<Quest> playerQuests = getPlayersQuests(player);
        playerQuests.add(quest);
        quests.put(player, playerQuests);
    }

    @Nullable
    public QuestSchema getQuestSchema(String name) {
        for(QuestSchema schema : questSchemas) {
            if(schema.getSchemaName().equals(name)) {
                return schema;
            }
        }
        return null;
    }

    public boolean isPlayerCanTakeQuest(String player, QuestSchema schema) {
        for(Quest quest : getPlayersQuests(player)) {
            if(!quest.getQuestSchema().equals(schema)) {
                continue;
            }
            if(quest.getTagID() >= schema.getTagID()) {
                return false;
            } else {
                plugin.getLogger().info(quest.getTagID() + " tagid " + schema.getTagID());
                plugin.getLogger().info(quest.getDateTag() + " datetag " + schema.getDateTag());
            }
        }
        return true;
    }

    public boolean isQuestForEvent(Quest quest, String event, String subject) {
        if(quest.getEvent() == null) {
            return false;
        }
        String questEvent = quest.getEvent();
        String[] split = questEvent.split(" ");
        if(split.length != 3) {
            return false;
        }
        return split[0].equalsIgnoreCase(event) && split[2].equalsIgnoreCase(subject);
    }

    public List<Quest> getPlayersQuests(String player) {
        if(quests.containsKey(player)) {
            return quests.get(player);
        }
        return new ArrayList<>();
    }

    public List<Quest> getPlayersActiveQuests(String player) {
        List<Quest> playerQuests = getPlayersQuests(player);
        List<Quest> activeQuests = new ArrayList<>();
        for(Quest q : playerQuests) {
            if(q.getQuestState().equals(QuestState.NOT_COMPLETED)) {
                activeQuests.add(q);
            }
        }
        return activeQuests;
    }

    @Nullable
    public QuestSchema getSchemaFromIndex(int index) {
        if(questSchemas.size() - 1 > index) {
            return null;
        }
        return questSchemas.get(index);
    }

    public void registerDateCheckTask() {
        if(dateCheckTask != -1) {
            plugin.getServer().getScheduler().cancelTask(dateCheckTask);
        }
        dateCheckTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            String newTag = plugin.getDateManager().getFormattedDate("%Y/%M/%D");
            for(QuestSchema schema : questSchemas) {
                if(newTag.equals(schema.getDateTag())) {
                    continue;
                }
                if(QuestInterval.DAY.equals(schema.getQuestInterval())) {
                    schema.setDateTag(newTag);
                } else if(QuestInterval.MONTH.equals(schema.getQuestInterval())) {
                    if(newTag.equals(plugin.getDateManager().getFormattedDate("%Y/%M/1"))) {
                        schema.setDateTag(newTag);
                    }
                } else {
                    if(plugin.getDateManager().getDayName().equalsIgnoreCase(schema.getQuestInterval().name())) {
                        schema.setDateTag(newTag);
                    }
                }
            }
        }, 0L, 20L);
    }

}
