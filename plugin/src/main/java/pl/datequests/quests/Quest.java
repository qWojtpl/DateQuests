package pl.datequests.quests;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import pl.datequests.DateQuests;
import pl.datequests.data.MessagesManager;

@Getter
@Setter
public class Quest {

    private String owner;
    private QuestSchema questSchema;
    private QuestState questState = QuestState.NOT_COMPLETED;
    private String event;
    private int progress = 0;
    private int requiredProgress = 1;
    private boolean changed;
    private String dateTag;
    private int tagID;

    public void randomizeEvent() {
        if(changed) {
            return;
        }
        setEvent(questSchema.getRandomEvent(owner));
        progress = 0;
    }

    public String getTranslatedEvent() {
        MessagesManager messages = DateQuests.getInstance().getMessagesManager();
        return event
                .replaceFirst("kill", messages.getMessage("eventKill"))
                .replaceFirst("break", messages.getMessage("eventBreak"))
                .replaceFirst("deliver", messages.getMessage("eventDeliver"));
    }

    public void setEvent(String event) {
        this.event = event;
        String[] split = event.split(" ");
        if(split.length != 3) {
            DateQuests.getInstance().getLogger().severe(event + " is not a correct event!");
            requiredProgress = 1;
            this.event = "break 1 bedrock";
            return;
        }
        try {
            requiredProgress = Integer.parseInt(split[1]);
        } catch(NumberFormatException e) {
            DateQuests.getInstance().getLogger().severe(event + " is not a correct event!");
            requiredProgress = 1;
            this.event = "break 1 bedrock";
        }
        if(split[0].equalsIgnoreCase("deliver")) {
            if(!DateQuests.getInstance().isUsingCitizens()) {
                DateQuests.getInstance().getLogger().warning(
                        "Event deliver is not possible, since you're not using Citizens!");
            }
        }
    }

    public void setTagID(int id) {
        this.tagID = id;
        if(this.questState.equals(QuestState.COMPLETED)) {
            return;
        }
        updateTagID();
    }

    public void updateTagID() {
        if(this.tagID < questSchema.getTagID()) {
            this.questState = QuestState.NOT_ACTIVE;
        }
    }

    /**
        Returns true if quest is completed
        @param progress Progress to add
        @return True or false
     */
    public boolean setProgress(int progress) {
        this.progress = progress;
        if(progress >= requiredProgress) {
            questState = QuestState.COMPLETED;
            return true;
        }
        return false;
    }

    public void save() {
        DateQuests.getInstance().getDataHandler().saveQuest(this);
    }

    public void saveProgress() {
        DateQuests.getInstance().getDataHandler().saveQuestProgress(this);
    }

}
