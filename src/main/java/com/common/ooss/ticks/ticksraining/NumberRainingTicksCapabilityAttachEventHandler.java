package com.common.ooss.ticks.ticksraining;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class NumberRainingTicksCapabilityAttachEventHandler {
  /*
  For attaching capabilities to chunks
  */
  @SubscribeEvent
  public static void attachCapabilityToChunkHandler(
      AttachCapabilitiesEvent<Level> capabilitiesEvent) {
    NumberRainingTicks numberRainingTicks =
        ((Level) capabilitiesEvent.getObject())
            .getCapability(NumberRainingTicksCapability.CAPABILITY_NUMBER_RAINING_TICKS)
            .orElse(null);
    if (numberRainingTicks == null) {
      capabilitiesEvent.addCapability(
          new ResourceLocation("ooss:number_raining_ticks"),
          new NumberRainingTicksCapabilityProvider());
    }
  }
}
