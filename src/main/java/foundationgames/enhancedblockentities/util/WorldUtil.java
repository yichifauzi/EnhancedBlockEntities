package foundationgames.enhancedblockentities.util;

import foundationgames.enhancedblockentities.EnhancedBlockEntities;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum WorldUtil implements ClientTickEvents.EndWorldTick {
    EVENT_LISTENER;

    public static Set<ChunkSectionPos> FORCE_SYNCHRONOUS_CHUNK_REBUILD = new HashSet<>();
    private static Map<RegistryKey<World>, Long2ObjectMap<Runnable>> SCHEDULED_TASKS = new HashMap<>();

    public static void rebuildChunkSynchronously(World world, BlockPos pos, boolean forceSync) {
        var bState = world.getBlockState(pos);
        try {
            if (forceSync) {
                WorldUtil.FORCE_SYNCHRONOUS_CHUNK_REBUILD.add(ChunkSectionPos.from(pos));
            }
            MinecraftClient.getInstance().worldRenderer.updateBlock(world, pos, bState, bState, 8);
        } catch (NullPointerException ignored) {
            EnhancedBlockEntities.LOG.warn("Error rebuilding chunk at block pos "+pos);
        }
    }

    public static void schedule(World world, long time, Runnable action) {
        SCHEDULED_TASKS.computeIfAbsent(world.getRegistryKey(), k -> new Long2ObjectOpenHashMap<>()).put(time, action);
    }

    @Override
    public void onEndTick(ClientWorld world) {
        var key = world.getRegistryKey();

        if (SCHEDULED_TASKS.containsKey(key)) {
            SCHEDULED_TASKS.get(key).long2ObjectEntrySet().removeIf(entry -> {
                if (world.getTime() >= entry.getLongKey()) {
                    entry.getValue().run();
                    return true;
                }

                return false;
            });
        }
    }
}
