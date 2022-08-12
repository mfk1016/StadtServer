package me.mfk1016.stadtserver.util;

import me.mfk1016.stadtserver.StadtServer;
import net.kyori.adventure.text.format.NamedTextColor;

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

    public static NamedTextColor getBossColor(int level) {
        return switch (level) {
            case 4 -> NamedTextColor.LIGHT_PURPLE;
            case 3 -> NamedTextColor.BLUE;
            case 2 -> NamedTextColor.YELLOW;
            default -> NamedTextColor.WHITE;
        };
    }

    public static String randomName() {
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
        return name + split + subname;
    }
}
