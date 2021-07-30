package com.common.ooss.ticks.ticksraining;

/** This class is for storing the total amount of time it's been raining in the world */
public class NumberRainingTicks {
  private long numberOfRainingTicks;

  public NumberRainingTicks(int initCharge) {
    numberOfRainingTicks = initCharge;
  }

  public static NumberRainingTicks defaultInstance() {
    return new NumberRainingTicks(-1);
  }

  public long get() {
    return numberOfRainingTicks;
  }

  public void set(long ticks) {
    numberOfRainingTicks = ticks;
  }

  public void increment() {
    numberOfRainingTicks++;
  }
}
