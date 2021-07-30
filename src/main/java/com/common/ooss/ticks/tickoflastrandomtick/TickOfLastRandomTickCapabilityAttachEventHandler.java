package com.common.ooss.ticks.tickoflastrandomtick;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TickOfLastRandomTickCapabilityAttachEventHandler {
  /*
  For attaching capabilities to chunks
  */
  @SubscribeEvent
  public static void attachCapabilityToChunkHandler(
      AttachCapabilitiesEvent<LevelChunk> capabilitiesEvent) {
    TickOfLastRandomTick tickOfLastRandomTick =
        ((LevelChunk) capabilitiesEvent.getObject())
            .getCapability(TickOfLastRandomTickCapability.CAPABILITY_TICKS_SINCE_RANDOM_TICK)
            .orElse(null);
    if (tickOfLastRandomTick == null) {
      capabilitiesEvent.addCapability(
          new ResourceLocation("ooss:tick_of_last_random_tick"),
          new TickOfLastRandomTickCapabilityProvider());
    }
  }
}
