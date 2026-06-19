package arr.armuriii.stabilized.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockEntity.class)
public interface BlockEntityAccessor {

    @Invoker("getLevel")
    Level stabilized$getLevel();

    @Invoker("getBlockPos")
    BlockPos stabilized$getBlockPos();
}
