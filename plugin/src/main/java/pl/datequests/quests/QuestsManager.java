package pl.datequests.quests;

import lombok.Getter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter
public class QuestsManager {

    private final List<QuestSchema> questSchemas = new ArrayList<>();
    private final List<QuestAssign> questAssigns = new ArrayList<>();

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

}
