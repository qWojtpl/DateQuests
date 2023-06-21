package pl.datequests.quests;

import lombok.Getter;
import pl.datequests.DateQuests;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class QuestsManager {

    private final DateQuests plugin = DateQuests.getInstance();
    private final List<QuestSchema> questSchemas = new ArrayList<>();
    private final List<QuestAssign> questAssigns = new ArrayList<>();
    private int dateCheckTask;

    public QuestsManager() {
        registerDateCheckTask();
    }

    public void addQuestSchema(QuestSchema schema) {
        questSchemas.add(schema);
    }

    public void assignQuest(QuestAssign questAssign) {
        questAssigns.add(questAssign);
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

    private void registerDateCheckTask() {
        dateCheckTask = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {

        }, 0L, 20L);
    }

}
