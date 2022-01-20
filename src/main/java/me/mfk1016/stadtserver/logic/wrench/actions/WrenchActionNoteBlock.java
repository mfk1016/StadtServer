package me.mfk1016.stadtserver.logic.wrench.actions;

import me.mfk1016.stadtserver.logic.wrench.WrenchAction;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.entity.Player;

public class WrenchActionNoteBlock extends WrenchAction {

    @Override
    protected Result wrenchBlock(Player player, Block target) {
        if (!(target.getBlockData() instanceof NoteBlock noteBlockData))
            return Result.FALSE;
        noteBlockData.setNote(Note.sharp(0, Note.Tone.F));
        target.setBlockData(noteBlockData);
        player.playNote(target.getLocation(), noteBlockData.getInstrument(), noteBlockData.getNote());
        return Result.TRUE;
    }

    @Override
    protected String wrenchMessage(Result result) {
        return null;
    }

    @Override
    protected boolean isEventCancelled(Result result) {
        return result == Result.TRUE;
    }
}
