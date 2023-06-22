package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;
import pl.datequests.DateQuests;

import javax.annotation.Nullable;

@Getter
@Setter
public class Quest {

    private String owner;
    private QuestSchema questSchema;
    private QuestState questState;
    @Nullable
    private String event;
    private int progress = 0;
    private int requiredProgress = 1;
    private String dateTag;
    private int tagID;

    public void save() {
        DateQuests.getInstance().getDataHandler().saveQuest(this);
    }

}
