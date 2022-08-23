package me.mfk1016.stadtserver.candlestore;

import me.mfk1016.stadtserver.util.Keys;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Candle;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static me.mfk1016.stadtserver.util.Functions.*;

public class CandleStoreUtils {

    public static CandleMemberType getMemberType(Material subBlockMat) {
        return switch (subBlockMat) {
            case ENDER_CHEST -> CandleMemberType.CHEST;
            case HOPPER -> CandleMemberType.EXPORT;
            default -> CandleMemberType.NORMAL;
        };
    }

    public static boolean isValidStoreItem(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasEnchants())
            return false;
        if (item.getMaxStackSize() > 1)
            return true;
        return switch (item.getType()) {
            case CLOCK, COMPASS -> true;
            default -> false;
        };
    }

    public static ItemStack createViewItem(Material mat, long amount) {
        ItemStack elem = new ItemStack(mat);
        ItemMeta meta = elem.getItemMeta();
        meta.displayName(undecoratedText(largeAmountString(amount)));
        elem.setItemMeta(meta);
        return elem;
    }

    /* --- Candle Tool --- */

    public static ItemStack getCandleTool() {
        ItemStack tool = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta meta = tool.getItemMeta();
        meta.displayName(undecoratedText("Candle Tool").color(NamedTextColor.LIGHT_PURPLE));
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(Keys.CANDLE_STORE, PersistentDataType.INTEGER, 1);
        tool.setItemMeta(meta);
        return tool;
    }

    public static boolean isCandleTool(ItemStack item) {
        if (stackEmpty(item) || item.getType() != Material.GOLDEN_SWORD)
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(Keys.CANDLE_STORE);
    }

    /* --- Checks --- */

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean checkValidToCreateElement(PlayerInteractEvent event, Block target, Dispenser targetDispenser) {
        Optional<CandleStore> current = CandleStoreManager.getStore(targetDispenser);
        if (current.isPresent()) {
            event.setCancelled(true);
            Material candleMat = target.getRelative(BlockFace.UP).getType();
            playerMessage(event.getPlayer(),
                    getMaterialName(candleMat, false) + " store with " +
                            current.get().getMemberCount() + " members and " +
                            current.get().getUsedSlots() + "/" + current.get().getStorageSlots() + " slots used.");
            return false;
        }
        if (!targetDispenser.getInventory().isEmpty()) {
            playerMessage(event.getPlayer(), "The dispenser must be empty.");
            return false;
        }
        Directional dispenserDirection = (Directional) target.getBlockData();
        if (dispenserDirection.getFacing() != BlockFace.DOWN) {
            playerMessage(event.getPlayer(), "The dispenser must face down.");
            return false;
        }
        if (!(target.getRelative(BlockFace.UP).getBlockData() instanceof Candle)) {
            playerMessage(event.getPlayer(), "A candle must be placed on the dispenser.");
            return false;
        }
        return true;
    }

    public static List<Dispenser> getNearestPotentialCandleStoreElements(Block toSearch, Material candleMat, int range) {
        List<Dispenser> dispensers = new ArrayList<>();
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    if (toSearch.getRelative(x, y + 1, z).getType() != candleMat)
                        continue;
                    Block relative = toSearch.getRelative(x, y, z);
                    if (relative.getType() == Material.DISPENSER)
                        dispensers.add((Dispenser) relative.getState());
                }
            }
        }
        dispensers.sort(Comparator.comparingDouble(a -> toSearch.getLocation().distance(a.getLocation())));
        return dispensers;
    }

    public static void onDestroyCandleStoreMember(Block target, Player destroyer) {
        if (target.getType().name().endsWith("CANDLE")) {
            Block possibleTarget = target.getRelative(0, -1, 0);
            if (possibleTarget.getType() != Material.DISPENSER)
                return;
            Dispenser dispenser = (Dispenser) possibleTarget.getState();
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.deleteFromStore(dispenser);
                if (destroyer != null) {
                    Material candleMat = target.getType();
                    String candleName = getMaterialName(candleMat, false);
                    playerMessage(destroyer, candleName + " store member removed.");
                }
            }
        } else if (target.getRelative(BlockFace.UP).getType() == Material.DISPENSER) {
            Dispenser dispenser = (Dispenser) target.getRelative(BlockFace.UP).getState();
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.updateStoreMember(dispenser, CandleMemberType.NORMAL);
                if (destroyer != null) {
                    Material candleMat = target.getRelative(0, 2, 0).getType();
                    String candleName = getMaterialName(candleMat, false);
                    playerMessage(destroyer, candleName + " store member updated.");
                }
            }
        } else if (target.getType() == Material.DISPENSER) {
            Dispenser dispenser = (Dispenser) target.getState();
            if (CandleStoreManager.getStore(dispenser).isPresent()) {
                CandleStoreManager.deleteFromStore(dispenser);
                if (destroyer != null) {
                    Material candleMat = target.getRelative(0, 1, 0).getType();
                    String candleName = getMaterialName(candleMat, false);
                    playerMessage(destroyer, candleName + " store member removed.");
                }
            }
        }
    }
}
