package pl.datequests.quests;

import lombok.Getter;
import pl.datequests.DateQuests;

@Getter
public class QuestAssign {

    private final String player;
    private final Quest quest;

    public QuestAssign(String player, Quest quest) {
        this.player = player;
        this.quest = quest;
        DateQuests.getInstance().getQuestsManager().assignQuest(this);
    }

}
