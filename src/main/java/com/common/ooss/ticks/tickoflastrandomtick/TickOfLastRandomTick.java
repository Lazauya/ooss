package com.common.ooss.ticks.tickoflastrandomtick;

/**
 * This class is for storing the last time that the chunk was randomly ticking and the total number
 * of ticks that the world had been raining for. We use this to determine the amount of time a chunk
 * was out of random tick range so we can automatically populate it with the right amount of snow,
 * based on the time that it was snowing.
 */
public class TickOfLastRandomTick {
  private long tickOfLastRandomTick;
  private long lastNumberOfRainingTicks;

  public TickOfLastRandomTick(int initCharge, long lastNumberOfRainingTicks) {
    tickOfLastRandomTick = initCharge;
    this.lastNumberOfRainingTicks = lastNumberOfRainingTicks;
  }

  public static TickOfLastRandomTick defaultInstance() {
    return new TickOfLastRandomTick(-1, -1);
  }

  public long getTickOfLastRandomTick() {
    return tickOfLastRandomTick;
  }

  public void setTickOfLastRandomTick(long tick) {
    tickOfLastRandomTick = tick;
  }

  public long getLastNumberOfRainingTicks() {
    return lastNumberOfRainingTicks;
  }

  public void setLastNumberOfRainingTicks(long lastNumberOfRainingTicks) {
    this.lastNumberOfRainingTicks = lastNumberOfRainingTicks;
  }
}
