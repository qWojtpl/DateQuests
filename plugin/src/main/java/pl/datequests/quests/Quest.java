package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;
import pl.datequests.DateQuests;

@Getter
@Setter
public class Quest {

    private String owner;
    private QuestSchema questSchema;
    private String dateTag;
    private int tagID;

    public void save() {
        DateQuests.getInstance().getDataHandler().saveQuest(this);
    }

}
