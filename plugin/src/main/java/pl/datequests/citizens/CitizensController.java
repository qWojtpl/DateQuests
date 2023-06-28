package pl.datequests.citizens;

import lombok.Getter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import pl.datequests.DateQuests;

@Getter
public class CitizensController implements Listener {

    private final DateQuests plugin = DateQuests.getInstance();
    private NPC npc;

    public CitizensController() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void createNPC(String skinName, Location location) {
        if(location == null) {
            plugin.getLogger().severe("Cannot create NPC - location is null");
            return;
        }
        npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "DateQuests NPC");
        npc.spawn(location);
        SkinTrait skinTrait = npc.getTrait(SkinTrait.class);
        skinTrait.setSkinName(skinName);
    }

    @EventHandler
    public void onClick(NPCRightClickEvent event) {
        if(event.isCancelled()) {
            return;
        }
        if(!event.getNPC().equals(npc)) {
            return;
        }
        event.getClicker().sendMessage("Clicked! ;D");
    }

}
