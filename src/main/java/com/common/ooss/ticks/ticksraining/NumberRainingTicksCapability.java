package com.common.ooss.ticks.ticksraining;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class NumberRainingTicksCapability {
  @CapabilityInject(NumberRainingTicks.class)
  public static Capability<NumberRainingTicks> CAPABILITY_NUMBER_RAINING_TICKS = null;

  public static void register() {
    CapabilityManager.INSTANCE.register(NumberRainingTicks.class);
  }
}
