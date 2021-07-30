package com.common.ooss.ticks.tickoflastrandomtick;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TickOfLastRandomTickCapabilityProvider implements ICapabilitySerializable<Tag> {

  private final Direction NO_SPECIFIC_SIDE = null;

  private TickOfLastRandomTick tickOfLastRandomTick = TickOfLastRandomTick.defaultInstance();

  /**
   * Retrieves the Optional handler for the capability requested on the specific side. The return
   * value <strong>CAN</strong> be the same for multiple faces. Modders are encouraged to cache this
   * value, using the listener capabilities of the Optional to be notified if the requested
   * capability get lost.
   *
   * @param cap The capability on which the request is being made.
   * @param side Optionally, the side on which the request is being made.
   * @return An optional holding the requested capability.
   */
  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
    return getCapability(cap);
  }

  @Nonnull
  @Override
  public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
    if (TickOfLastRandomTickCapability.CAPABILITY_TICKS_SINCE_RANDOM_TICK == cap) {
      return (LazyOptional<T>) LazyOptional.of(() -> tickOfLastRandomTick);
    }
    return LazyOptional.empty();
  }

  @Override
  public Tag serializeNBT() {
    LongTag tickOfLastRandomTickNBT =
        LongTag.valueOf(tickOfLastRandomTick.getTickOfLastRandomTick());
    LongTag lastNumberOfRainingTicks =
        LongTag.valueOf(tickOfLastRandomTick.getLastNumberOfRainingTicks());
    CompoundTag tag = new CompoundTag();
    tag.put("tolr", tickOfLastRandomTickNBT);
    tag.put("lnor", lastNumberOfRainingTicks);
    return tag;
  }

  @Override
  public void deserializeNBT(Tag nbt) {
    long tick = -1; // tick of last random
    long ticks = -1; // last number of raining
    if (nbt.getType() == CompoundTag.TYPE) {
      tick = ((CompoundTag) nbt).getLong("tolr");
      ticks = ((CompoundTag) nbt).getLong("lnor");
    }
    tickOfLastRandomTick.setTickOfLastRandomTick(tick);
    tickOfLastRandomTick.setLastNumberOfRainingTicks(ticks);
  }
}
