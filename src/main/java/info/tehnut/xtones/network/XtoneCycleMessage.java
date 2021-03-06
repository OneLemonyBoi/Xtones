package info.tehnut.xtones.network;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public final class XtoneCycleMessage implements IMessage {
    private static final int SIZE_BYTES = Byte.BYTES + Byte.BYTES + Short.BYTES;
    private static final EnumHand[] HANDS = EnumHand.values();

    private static final int ABSENT = 0;
    private static final int NEXT = 1;
    private static final int PREV = -1;

    private static final int INVALID_SLOT = -2;
    private static final int OFF_HAND_SLOT = -1;

    private @MonotonicNonNull EnumHand hand;
    private int offset = ABSENT;
    private int slot = INVALID_SLOT;

    @Deprecated
    public XtoneCycleMessage() {
    }

    XtoneCycleMessage(final EntityPlayer player, final EnumHand hand, final int scroll) {
        this.hand = hand;
        this.offset = (scroll >= 0) ? NEXT : PREV;
        this.slot = isMain(hand) ? checkSlot(hand, player.inventory.currentItem) : OFF_HAND_SLOT;
    }

    private static boolean isMain(final EnumHand hand) {
        return hand == EnumHand.MAIN_HAND;
    }

    private static int checkSlot(final EnumHand hand, final int slot) {
        final boolean hotbar = isMain(hand) && InventoryPlayer.isHotbar(slot);
        Preconditions.checkArgument(hotbar || (slot == OFF_HAND_SLOT), slot);
        return slot;
    }

    EnumHand getHand() {
        Preconditions.checkState(this.hand != null);
        return this.hand;
    }

    int getOffset() {
        Preconditions.checkState(this.offset != ABSENT);
        return this.offset;
    }

    int getExpectedSlot() {
        Preconditions.checkState(this.slot != INVALID_SLOT);
        return this.slot;
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        Preconditions.checkArgument(buf.isReadable(SIZE_BYTES), buf);
        this.offset = buf.readBoolean() ? NEXT : PREV;
        this.hand = HANDS[buf.readByte()];
        this.slot = checkSlot(this.hand, buf.readShort());
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        Preconditions.checkArgument(buf.isWritable(SIZE_BYTES), buf);
        buf.writeBoolean(this.offset == NEXT);
        buf.writeByte(this.hand.ordinal());
        buf.writeShort(this.slot);
    }
}
