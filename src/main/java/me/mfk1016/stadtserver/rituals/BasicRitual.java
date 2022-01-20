package me.mfk1016.stadtserver.rituals;

import me.mfk1016.stadtserver.StadtServer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class BasicRitual {

    protected final Block focusBlock;
    private final BukkitRunnable ritualWorker;
    protected RitualState state;
    private int timer = 0;

    public BasicRitual(Block focusBlock) {
        this.focusBlock = focusBlock;
        this.state = RitualState.INIT;
        BasicRitual instance = this;
        ritualWorker = new BukkitRunnable() {
            @Override
            public void run() {
                onRitualCheck(timer);
                if (state == RitualState.SUCCESS) {
                    onRitualSuccess();
                    RitualManager.ritualStopped(instance);
                    cancel();
                } else if (state == RitualState.FAILURE) {
                    onRitualFail();
                    RitualManager.ritualStopped(instance);
                    cancel();
                } else if (state == RitualState.CANCEL) {
                    cancel();
                }
                timer++;
            }
        };
    }

    protected void performRitual() {
        ritualWorker.runTaskTimer(StadtServer.getInstance(), 10, 10);
    }

    public void cancelRitual() {
        state = RitualState.CANCEL;
        focusBlock.getWorld().playSound(focusBlock.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
        onRitualCancel();
    }

    public abstract boolean hasProperShape();

    public abstract void startRitual(Entity sacrifice);

    public abstract void onRitualCheck(int timer);

    public abstract void onRitualSuccess();

    public abstract void onRitualFail();

    protected abstract void onRitualCancel();
}
