package pl.datequests.util;

import org.bukkit.Location;
import org.bukkit.World;
import pl.datequests.DateQuests;

import javax.annotation.Nullable;

public class LocationUtil {

    @Nullable
    public static Location parseLocation(String location) {
        String[] split = location.split(" ");
        if(split.length != 4) {
            return null;
        }
        int x;
        int y;
        int z;
        try {
            x = Integer.parseInt(split[0]);
            y = Integer.parseInt(split[1]);
            z = Integer.parseInt(split[2]);
        } catch(NumberFormatException e) {
            return null;
        }
        World w = DateQuests.getInstance().getServer().getWorld(split[3]);
        if(w == null) {
            return null;
        }
        return new Location(w, x, y, z);
    }

}
