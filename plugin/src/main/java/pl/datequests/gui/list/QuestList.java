package pl.datequests.gui.list;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import pl.datequests.DateQuests;
import pl.datequests.gui.PluginGUI;
import pl.datequests.quests.QuestSchema;

import java.util.ArrayList;
import java.util.List;

public class QuestList extends PluginGUI {

    private List<Integer> slots;

    public QuestList(Player owner, String inventoryName) {
        super(owner, inventoryName, 54);
    }

    @Override
    public void onOpen() {
        setGUIProtected(true);
        fillWith(Material.GRAY_STAINED_GLASS_PANE);
        List<QuestSchema> schemas = new ArrayList<>();
        for(QuestSchema schema : getQuestsManager().getQuestSchemas()) {
            if(getOwner().hasPermission(schema.getPermission())) {
                schemas.add(schema);
            }
        }
        this.slots = new ArrayList<>();
        int numberOfSchemas = schemas.size();
        if(numberOfSchemas == 0) {
            setSlot(22, Material.BARRIER, getMessages().getMessage("noAvailableQuests"),
                    getLore(getMessages().getMessage("noAvailableQuestsLore")));
            return;
        } if(numberOfSchemas == 1) {
            slots.add(22);
        } else if(numberOfSchemas == 2) {
            slots.add(21);
            slots.add(23);
        } else if(numberOfSchemas == 3) {
            slots.add(20);
            slots.add(22);
            slots.add(24);
        } else if(numberOfSchemas == 4) {
            slots.add(19);
            slots.add(21);
            slots.add(23);
            slots.add(25);
        } else {
            for(int i = 0; i < numberOfSchemas; i++) {
                slots.add(i);
            }
        }
        int i = 0;
        for(int slot : slots) {
            setSlot(slot, schemas.get(i).getIcon());
            i++;
        }
    }

    @Override
    public void onClick(int slot) {
        if(slots.contains(slot)) {
            closeInventory();
            int index = -1;
            for(int i = 0; i < slots.size(); i++) {
                if(slots.get(i).equals(slot)) {
                    index = i;
                }
            }
            if(index == -1) {
                return;
            }
            new QuestPanel(getOwner(), getInventoryName(), index);
        }
    }

}
