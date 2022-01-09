package me.mfk1016.stadtserver.util;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.ChatColor;

public class BossName {

    private static final String[] NAMES_M = {
            "Adalbert", "Friederich", "Diethelm", "Gunther", "Landolf", "Karl-Heinz", "Petrosilius",
            "Kasper", "Seppel", "Walter", "Eugen", "Olaf", "Arminius", "Dietrich", "Alois", "Ruben",
            "Patrick", "Balduin"
    };

    private static final String[] NAMES_W = {
            "Brunhilde", "Sieglinde", "Ludmilla", "Barbara", "Antonina", "Isabella", "Charlotte", "Bertha",
            "Gudrun", "Walburga", "Friede", "Philippa"
    };

    private static final String[] SUBNAMES = {
            "Verbuggte", "Verhinderte", "Ungetötete", "Lootlose", "Gerissene", "Berittene", "Ungeschorene",
            "Ungesäuberte", "Hungrige", "Unbeseitigte", "Kopflose", "Hirnlose", "Unbekleidete", "Befleckte",
            "Schwindende", "Verschlissene", "Unberittene", "Kleine", "Verblichene", "Lautlose",
            "Beruhigte", "Blutlose", "Lichtscheue", "Unredliche"
    };

    private static final String SPLIT_M = " der ";
    private static final String SPLIT_W = " die ";

    private static String colorPrefix(int level) {
        return switch (level) {
            case 4 -> ChatColor.LIGHT_PURPLE.toString();
            case 3 -> ChatColor.BLUE.toString();
            case 2 -> ChatColor.YELLOW.toString();
            default -> "";
        };
    }

    public static String randomName(int level) {
        boolean mw = StadtServer.RANDOM.nextInt(2) == 0;
        String name, split;
        if (mw) {
            name = NAMES_M[StadtServer.RANDOM.nextInt(NAMES_M.length)];
            split = SPLIT_M;
        } else {
            name = NAMES_W[StadtServer.RANDOM.nextInt(NAMES_W.length)];
            split = SPLIT_W;
        }
        String subname = SUBNAMES[StadtServer.RANDOM.nextInt(SUBNAMES.length)];
        return colorPrefix(level) + name + split + subname;
    }
}
