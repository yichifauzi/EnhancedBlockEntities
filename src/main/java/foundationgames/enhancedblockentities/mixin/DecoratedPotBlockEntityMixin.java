package foundationgames.enhancedblockentities.mixin;

import foundationgames.enhancedblockentities.util.WorldUtil;
import foundationgames.enhancedblockentities.util.duck.ModelStateHolder;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotBlockEntity.class)
public class DecoratedPotBlockEntityMixin implements ModelStateHolder {
    @Unique private int enhanced_bes$modelState = 0;

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void enhanced_bes$updateChunkOnPatternsLoaded(NbtCompound nbt, CallbackInfo ci) {
        var self = (DecoratedPotBlockEntity)(Object)this;

        if (self.getWorld() != null && self.getWorld().isClient()) {
            WorldUtil.rebuildChunkSynchronously(self.getWorld(), self.getPos(), false);
        }
    }

    @Inject(method = "onSyncedBlockEvent", at = @At(value = "RETURN", shift = At.Shift.BEFORE, ordinal = 0))
    private void enhanced_bes$updateOnWobble(int type, int data, CallbackInfoReturnable<Boolean> cir) {
        var self = (DecoratedPotBlockEntity)(Object)this;
        var world = self.getWorld();

        if (self.lastWobbleType == null) {
            return;
        }

        this.setModelState(1, world, self.getPos());

        WorldUtil.schedule(world, self.lastWobbleTime + self.lastWobbleType.lengthInTicks,
                () -> {
                    if (self.getWorld().getTime() >= self.lastWobbleTime + self.lastWobbleType.lengthInTicks) {
                        this.setModelState(0, world, self.getPos());
                    }
                });
    }

    @Override
    public int getModelState() {
        return enhanced_bes$modelState;
    }

    @Override
    public void applyModelState(int state) {
        this.enhanced_bes$modelState = state;
    }
}
