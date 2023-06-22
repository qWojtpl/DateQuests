package pl.datequests.quests;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class QuestGroup {

    private final List<String> events = new ArrayList<>();
    private final List<String> ranges = new ArrayList<>();

}
