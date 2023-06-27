package pl.datequests.data;

import java.util.HashMap;

public class MessagesManager {

    private final HashMap<String, String> messages = new HashMap<>();

    public String getMessage(String key) {
        if(messages.containsKey(key)) {
            return messages.get(key).replace("%prefix%", getRawMessage("prefix")).replace("&", "§");
        }
        return key;
    }

    public String getRawMessage(String key) {
        return messages.getOrDefault(key, key);
    }

    public void addMessage(String key, String message) {
        messages.put(key, message);
    }

}
