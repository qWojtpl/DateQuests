package pl.datequests.quests;

import lombok.Getter;
import pl.datequests.DateQuests;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class QuestsManager {

    private final DateQuests plugin = DateQuests.getInstance();
    private final List<QuestSchema> questSchemas = new ArrayList<>();
    private final HashMap<String, List<Quest>> quests = new HashMap<>();
    private int dateCheckTask;

    public QuestsManager() {
        registerDateCheckTask();
    }

    public void addQuestSchema(QuestSchema schema) {
        questSchemas.add(schema);
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
            }
        }
        return true;
    }

    public List<Quest> getPlayersQuests(String player) {
        if(quests.containsKey(player)) {
            return quests.get(player);
        }
        return new ArrayList<>();
    }

    @Nullable
    public QuestSchema getSchemaFromIndex(int index) {
        if(questSchemas.size() - 1 > index) {
            return null;
        }
        return questSchemas.get(index);
    }

    private void registerDateCheckTask() {
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
