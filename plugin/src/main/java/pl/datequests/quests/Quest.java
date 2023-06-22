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
    private QuestState questState = QuestState.NOT_COMPLETED;
    @Nullable
    private String event;
    private int progress = 0;
    private int requiredProgress = 1;
    private String dateTag;
    private int tagID;

    public void randomizeEvent() {
        event = questSchema.getRandomEvent(owner);
        progress = 0;
        loadRequiredProgress();
    }

    public void loadRequiredProgress() {
        if(event == null) {
            return;
        }
        String[] split = event.split(" ");
        if(split.length != 3) {
            DateQuests.getInstance().getLogger().severe(event + " is not a correct event!");
            requiredProgress = 1;
            return;
        }
        try {
            requiredProgress = Integer.parseInt(split[1]);
        } catch(NumberFormatException e) {
            DateQuests.getInstance().getLogger().severe(event + " is not a correct event!");
            requiredProgress = 1;
        }
    }

    public void save() {
        DateQuests.getInstance().getDataHandler().saveQuest(this);
    }

}
