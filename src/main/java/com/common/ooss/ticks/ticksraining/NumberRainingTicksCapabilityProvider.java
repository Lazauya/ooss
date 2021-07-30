package com.common.ooss.ticks.ticksraining;

import net.minecraft.core.Direction;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NumberRainingTicksCapabilityProvider implements ICapabilitySerializable<Tag> {

  private final Direction NO_SPECIFIC_SIDE = null;

  private NumberRainingTicks numberRainingTicks = NumberRainingTicks.defaultInstance();

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
    if (NumberRainingTicksCapability.CAPABILITY_NUMBER_RAINING_TICKS == cap) {
      return (LazyOptional<T>) LazyOptional.of(() -> numberRainingTicks);
    }
    return LazyOptional.empty();
  }

  @Override
  public Tag serializeNBT() {
    LongTag longNBT = LongTag.valueOf(numberRainingTicks.get());
    return longNBT;
  }

  @Override
  public void deserializeNBT(Tag nbt) {
    long ticks = 0;
    if (nbt.getType() == LongTag.TYPE) {
      ticks = ((LongTag) nbt).getAsLong();
    }
    numberRainingTicks.set(ticks);
  }
}
