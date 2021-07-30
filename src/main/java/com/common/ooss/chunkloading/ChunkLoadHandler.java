package com.common.ooss.chunkloading;

import cern.jet.random.Binomial;
import cern.jet.random.engine.MersenneTwister64;
import com.common.ooss.ticks.tickoflastrandomtick.TickOfLastRandomTick;
import com.common.ooss.ticks.tickoflastrandomtick.TickOfLastRandomTickCapability;
import com.common.ooss.ticks.ticksraining.NumberRainingTicks;
import com.common.ooss.ticks.ticksraining.NumberRainingTicksCapability;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Mod.EventBusSubscriber
public class ChunkLoadHandler {
  private static final Logger LOGGER = LogManager.getLogger();

  private static final double randomTickProb = 1.0 / 16.0;
  private static final Binomial binGen = new Binomial(1, randomTickProb, new MersenneTwister64());
  private static final int[] iceIterationOrder = new int[256];

  public static void initializeIceIterationOrder() {
    for (int i = 0; i < iceIterationOrder.length; i++) {
      iceIterationOrder[i] = i;
    }
  }

  @SubscribeEvent
  public static void onWorldTick(TickEvent.WorldTickEvent tickEvent) {
    if (!tickEvent.world.isClientSide
        && tickEvent.type == TickEvent.Type.WORLD
        && tickEvent.phase == TickEvent.Phase.START) {
      ServerLevel world = ((ServerLevel) tickEvent.world);
      final NumberRainingTicks totalNumberRainingTicks =
          world
              .getCapability(NumberRainingTicksCapability.CAPABILITY_NUMBER_RAINING_TICKS)
              .orElse(null);
      if (world.isRaining()) {
        // first we need to update the world's raining ticks if necessary
        if (totalNumberRainingTicks != null) {
          totalNumberRainingTicks.increment();
        }
      }

      ServerChunkCache chunkCache = world.getChunkSource();
      List<ChunkHolder> chunkList = Lists.newArrayList(chunkCache.chunkMap.getChunks());

      // This was from the original code, but we shouldn't need this
      // Collections.shuffle(chunkList);
      chunkList.forEach(
          (chunkHolder) -> {
            Optional<LevelChunk> optional =
                chunkHolder.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();

            if (optional.isPresent()) {
              LevelChunk levelchunk = optional.get();
              ChunkPos chunkpos = levelchunk.getPos();

              if ((chunkCache.level.isPositionEntityTicking(chunkpos)
                      && !chunkCache.chunkMap.noPlayersCloseForSpawning(chunkpos))
                  || chunkCache.distanceManager.shouldForceTicks(chunkpos.toLong())) {

                // this means the chunk is random ticking
                // since it's random ticking, reset the ticksSinceRandomTick
                TickOfLastRandomTick tick =
                    levelchunk
                        .getCapability(
                            TickOfLastRandomTickCapability.CAPABILITY_TICKS_SINCE_RANDOM_TICK)
                        .orElse(null);

                //                LOGGER.info(tick);
                //                LOGGER.info(totalNumberRainingTicks.get());

                if (tick != null) {
                  if (tick.getTickOfLastRandomTick() != -1) {
                    // first find the difference between the current tick and then
                    // if it's greater than 1, then that means that this chunk was out of random
                    // tick range for some time
                    long ticksOutOfRandomTickRange =
                        chunkCache.level.getGameTime() - tick.getTickOfLastRandomTick();
                    long ticksRaining =
                        totalNumberRainingTicks.get() - tick.getLastNumberOfRainingTicks();

                    if (ticksOutOfRandomTickRange > 1) {
                      //                      LOGGER.info(
                      //                          chunkCache.level.getGameTime()
                      //                              + ", "
                      //                              + tick.getTickOfLastRandomTick()
                      //                              + ", tick difference is "
                      //                              + ticksOutOfRandomTickRange
                      //                              + ", "
                      //                              + ticksRaining
                      //                              + ", "
                      //                              + tick.getLastNumberOfRainingTicks()
                      //                              + ", "
                      //                              + totalNumberRainingTicks.get());
                      // we need to simulate snowfall
                      try {
                        simulateSnowfallAndFreezing(
                            world, levelchunk, ticksOutOfRandomTickRange, ticksRaining);
                      } catch (Error error) {
                        LOGGER.error(error.getMessage());
                      }
                    }
                  }
                  // finally update the ticks
                  tick.setTickOfLastRandomTick(chunkCache.level.getGameTime());
                  // update the chunks last number of raining ticks
                  tick.setLastNumberOfRainingTicks(totalNumberRainingTicks.get());
                  // TODO: make sure this is the correct mapping
                  levelchunk.markUnsaved();
                }
              } else {
                // do nothing, because this tick is not randomly ticking? not 100% sure
              }
            }
          });
    }
  }

  static boolean canPlaceSnowOn(BlockState on, ChunkAccess chunk, BlockPos pos) {
    try {
      if (!on.is(Blocks.ICE) && !on.is(Blocks.PACKED_ICE) && !on.is(Blocks.BARRIER)) {
        if (!on.is(Blocks.HONEY_BLOCK) && !on.is(Blocks.SOUL_SAND)) {
          return Block.isFaceFull(on.getCollisionShape(chunk, pos), Direction.UP)
              || on.is(Blocks.SNOW) && on.getValue(SnowLayerBlock.LAYERS) == 8;
        } else {
          return true;
        }
      } else {
        return false;
      }
    } catch (Error error) {
      LOGGER.info("There was an error: " + error.toString());
      return false;
    }
  }

  // credit to https://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
  private static void shuffleArray(int[] array, Random random) {
    int index;
    for (int i = array.length - 1; i > 0; i--) {
      index = random.nextInt(i + 1);
      if (index != i) {
        array[index] ^= array[i];
        array[i] ^= array[index];
        array[index] ^= array[i];
      }
    }
  }

  /**
   * Generate an array representing a valid arrangement for snow after a certain number of ticks
   * (trials)
   *
   * @param numTrials the number of ticks that have passed, aka the number of times that the game
   *     would have "tried" to place snow
   * @param random a random object, preferable the one tied to the world
   * @return an array that represents each x-z coordinate in the chunk
   */
  static int[] generateIceAndSnowMap(int numTrials, Random random) {
    if (numTrials == 0) {
      return new int[256];
    }
    // TODO: optimize for small and large tick values

    // first we generate the amount of successes that we may have had random ticking
    // this can exactly be represented by a binomial distribution with parameters
    // n = # of samples = # of rainingTicks
    // p = probability of success = probability that we choose a block to try icing = 1/16

    // apparently, generating a binomial distribution is hard, but colt
    // (https://dst.lbl.gov/ACSSoftware/colt/api/overview-summary.html)
    // has a good, fast implementation
    // for very large tick values, we won't use a generator, and instead just choose a random
    // uniform value between 2*256 and 5*256
    // and for extremely large tick values we can just use a value of 256*16, which should pretty
    // much max every column
    // this is so we don't have to worry about accidentally using too much cpu on this when the
    // chunk has been unloaded for a very long time

    int numberOfPercips;

    // TODO: see if there's a better way to do the numberOfPercips
    if (20 * 60 * 10 < numTrials && numTrials <= 20 * 60 * 100) { // 10 minutes
      numberOfPercips = random.nextInt(3 * 256) + 512;
    } else if (20 * 60 * 100 <= numTrials) {
      numberOfPercips = 256 * 16;
    } else {
      //      LOGGER.info(numTrials);
      //      LOGGER.info(randomTickProb);
      //      LOGGER.info(binGen);
      numberOfPercips = binGen.nextInt(numTrials, randomTickProb);
    }

    //    LOGGER.info("percips: " + numberOfPercips);

    // next we generate 256-1 integers in the range 0 through numberOfPercips
    // if we sort these, we can use the differences between them to find a set of
    // percipDifs for each block that all add up to numberOfPercips

    int numBlocksInChunk = 16 * 16;
    int[] percipDifs = new int[numBlocksInChunk - 1];
    for (int i = 0; i < numBlocksInChunk - 1; i++) {
      percipDifs[i] = random.nextInt(numberOfPercips + 1);
    }

    // next we need to sort the array
    Arrays.sort(percipDifs);

    // now we need to generate the percip array, or the array that contains info about how many
    // times
    // each block would have gotten snow

    int[] percips = new int[numBlocksInChunk];

    for (int i = 0; i < numBlocksInChunk; i++) {
      int lower = i - 1 < 0 ? 0 : percipDifs[i - 1];
      int upper = i >= ((numBlocksInChunk - 1) - 1) ? numberOfPercips : percipDifs[i];
      percips[i] = upper - lower;
    }

    // lastly we have to shuffle percips, otherwise certain orientations become significantly less
    // likely than others
    shuffleArray(percips, random);

    return percips;
  }

  /**
   * @param world the world
   * @param chunk the chunk
   * @param rainingTicks the number of ticks it was raining
   * @param ticksOutOfRandomTickRange the total number of ticks to simulate
   */
  public static void simulateSnowfallAndFreezing(
      ServerLevel world, LevelChunk chunk, long ticksOutOfRandomTickRange, long rainingTicks) {
    int[] freezes =
        generateIceAndSnowMap((int) (ticksOutOfRandomTickRange - rainingTicks), world.random);
    int[] percipsAndFreezes = generateIceAndSnowMap((int) rainingTicks, world.random);

    // we want to iterate over the blocks randomly to be more fair
    // this will help make ice generation look more natural
    // so get a new iteration order by shuffling our old one
    shuffleArray(iceIterationOrder, world.random);

    for (int idx : iceIterationOrder) {
      int x = idx / 16;
      int z = idx % 16;
      int numFreezes = freezes[idx];
      int numPercipsAndFreezes = percipsAndFreezes[idx];

      int worldX = chunk.getPos().getMinBlockX() + x;
      int worldZ = chunk.getPos().getMinBlockZ() + z;
      BlockPos blockPos =
          world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(worldX, 0, worldZ));
      BlockPos belowPos = blockPos.below();
      Biome biome = world.getBiome(blockPos);

      // the logic for freezing and snowing was basically copied from the ServerLevel
      // tickChunk method
      if (numFreezes > 0 || numPercipsAndFreezes > 0) {
        if (biome.shouldFreeze(world, belowPos)) {
          world.setBlockAndUpdate(belowPos, Blocks.ICE.defaultBlockState());
        }
      }

      // TODO: implement snow piling up maybe? would be neat!
      if (numPercipsAndFreezes > 0) {
        if (biome.shouldSnow(world, blockPos)) {
          world.setBlockAndUpdate(blockPos, Blocks.SNOW.defaultBlockState());
        }

        BlockState belowBlockState = world.getBlockState(belowPos);
        Biome.Precipitation biomePrecipitation = biome.getPrecipitation();
        if (biomePrecipitation == Biome.Precipitation.RAIN && biome.isColdEnoughToSnow(belowPos)) {
          biomePrecipitation = Biome.Precipitation.SNOW;
        }

        // repeat multiple times for cauldrons
        for (int numPercips = 0;
            numPercips < numPercipsAndFreezes && numPercips < 8;
            numPercips++) {
          belowBlockState
              .getBlock()
              .handlePrecipitation(belowBlockState, world, belowPos, biomePrecipitation);
        }
      }

      //    for (int x = 0; x < 16; x++) {
      //      for (int z = 0; z < 16; z++) {
      //        // get our number of snows in pericps
      //        int idx = 16 * x + z;
      //        int numFreezes = freezes[idx];
      //        int numPercipsAndFreezes = percipsAndFreezes[idx];
      //
      //        int worldX = chunk.getPos().getMinBlockX() + x;
      //        int worldZ = chunk.getPos().getMinBlockZ() + z;
      //        BlockPos blockPos =
      //            world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(worldX, 0,
      // worldZ));
      //        BlockPos belowPos = blockPos.below();
      //        Biome biome = world.getBiome(blockPos);
      //
      //        // the logic for freezing and snowing was basically copied from the ServerLevel
      //        // tickChunk method
      //        if (numFreezes > 0 || numPercipsAndFreezes > 0) {
      //          if (biome.shouldFreeze(world, belowPos)) {
      //            world.setBlockAndUpdate(belowPos, Blocks.ICE.defaultBlockState());
      //          }
      //        }
      //
      //        // TODO: implement snow piling up maybe? would be neat!
      //        if (numPercipsAndFreezes > 0) {
      //          if (biome.shouldSnow(world, blockPos)) {
      //            world.setBlockAndUpdate(blockPos, Blocks.SNOW.defaultBlockState());
      //          }
      //
      //          BlockState belowBlockState = world.getBlockState(belowPos);
      //          Biome.Precipitation biomePrecipitation = biome.getPrecipitation();
      //          if (biomePrecipitation == Biome.Precipitation.RAIN
      //              && biome.isColdEnoughToSnow(belowPos)) {
      //            biomePrecipitation = Biome.Precipitation.SNOW;
      //          }
      //
      //          // repeat multiple times for cauldrons
      //          for (int numPercips = 0;
      //              numPercips < numPercipsAndFreezes && numPercips < 8;
      //              numPercips++) {
      //            belowBlockState
      //                .getBlock()
      //                .handlePrecipitation(belowBlockState, world, belowPos, biomePrecipitation);
      //          }
      //        }
      //      }
    }
  }
}
