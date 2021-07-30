package com.common.ooss.ticks.tickoflastrandomtick;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class TickOfLastRandomTickCapability {
  @CapabilityInject(TickOfLastRandomTick.class)
  public static Capability<TickOfLastRandomTick> CAPABILITY_TICKS_SINCE_RANDOM_TICK = null;

  public static void register() {
    CapabilityManager.INSTANCE.register(TickOfLastRandomTick.class);
  }
}
