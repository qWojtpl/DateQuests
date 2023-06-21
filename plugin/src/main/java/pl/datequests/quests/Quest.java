package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Quest {

    private String owner;
    private QuestSchema questSchema;
    private String dateTag;
    private int tagID;

}
