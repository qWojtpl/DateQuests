package pl.datequests.placeholders;

import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.datequests.DateQuests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class PlaceholderController extends PlaceholderExpansion {

    private final DateQuests main = DateQuests.getInstance();
    private final HashMap<String, Integer> playerScore = new HashMap<>();
    private List<String> leaderboard = new ArrayList<>();
    private int maxRecords;
    private int loadInterval;
    private int loadTask;

    @Override
    public @NotNull String getIdentifier() {
        return "datequests";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Assasin98980";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if(params.toLowerCase().startsWith("top_")) {
            try {
                String num = params.replace("top_", "");
                int id = Integer.parseInt(num);
                if(id > leaderboard.size() - 1) {
                    return "&c-";
                }
                return leaderboard.get(id);
            } catch(NumberFormatException ignored) {
                return "&cNOT VALID NUMBER";
            }
        }
        return null;
    }

    public void setScore(String player, int score) {
        playerScore.put(player, score);
    }

    public void addScore(String player, int score) {
        int currentScore = playerScore.getOrDefault(player, 0);
        currentScore += score;
        playerScore.put(player, currentScore);
        DateQuests.getInstance().getDataHandler().saveLeaderboard(player, currentScore);
    }

    public void createLoadTask() {
        if(loadTask != -1) {
            main.getServer().getScheduler().cancelTask(loadTask);
        }
        loadTask = main.getServer().getScheduler().scheduleSyncRepeatingTask(main, this::loadLeaderboard, 0L, 20L * loadInterval);
    }

    public void loadLeaderboard() {
        leaderboard = new ArrayList<>();
        List<String> skipPlayers = new ArrayList<>();
        int index = 0;
        for(int i = 0; i < playerScore.size() && (maxRecords == -1 || i < maxRecords); i++) {
            String maxPlayer = "";
            for(String player : playerScore.keySet()) {
                boolean found = false;
                for(String skipPlayer : skipPlayers) {
                    if(skipPlayer.equals(player)) {
                        found = true;
                        break;
                    }
                }
                if(found) {
                    continue;
                }
                if(playerScore.get(player) > playerScore.getOrDefault(maxPlayer, -1)) {
                    maxPlayer = player;
                }
            }
            skipPlayers.add(maxPlayer);
            leaderboard.add("§f" + ++index + ". " + maxPlayer + " §c- §9" + playerScore.get(maxPlayer));
        }
    }


}
