package arr.armuriii.stabilized.mixin;

import arr.armuriii.stabilized.mixin.accessor.BlockEntityAccessor;
import arr.armuriii.stabilized.util.StabilizedLockingSettings;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlock;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlockEntity;
import net.createmod.catnip.config.ConfigBase;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static arr.armuriii.stabilized.util.RotationUtils.*;

@Debug(export = true)
@Mixin(value = SwivelBearingBlockEntity.class,remap = false)
public abstract class SwivelBearingBlockEntityMixin {

    @Shadow @Final private static MutableComponent SCROLL_OPTION_TITLE;
    @Shadow private double targetAngleDegrees;

    @Shadow protected abstract SubLevel getContainingSubLevel();

    @Shadow @Final @NotNull private SwivelBearingBlockEntity.@NotNull SwivelBearingCogwheelBlockEntity cogwheel;

    @Shadow
    @Nullable
    protected abstract SubLevel getAttachedSubLevel();

    @Unique
    private ScrollOptionBehaviour<StabilizedLockingSettings> stabilized$lockedDefaultOption;

    @Unique
    private double stabilized$lastStabilizedAngle = 0;

    @ModifyArg(method = "addBehaviours",at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/blockEntity/behaviour/scrollValue/ScrollOptionBehaviour;<init>(Ljava/lang/Class;Lnet/minecraft/network/chat/Component;Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueBoxTransform;)V"),index = 3)
    private ValueBoxTransform storeSelectionMode(ValueBoxTransform slot, @Share("SelectionModeValueBox")LocalRef<ValueBoxTransform> localRef) {
        localRef.set(slot);
        return slot;
    }


    @ModifyArg(method = "addBehaviours", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private Object swapBehavior(Object original, @Share("SelectionModeValueBox")LocalRef<ValueBoxTransform> localRef) {
        this.stabilized$lockedDefaultOption = new ScrollOptionBehaviour<>(StabilizedLockingSettings.class,
                SCROLL_OPTION_TITLE,
                (SwivelBearingBlockEntity)(Object)this,
                localRef.get());
        this.stabilized$lockedDefaultOption.value = 1;
        return this.stabilized$lockedDefaultOption;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Ldev/simulated_team/simulated/content/blocks/swivel_bearing/SwivelBearingBlockEntity$LockingSetting;shouldLock(I)Z"))
    private boolean useStabilized(SwivelBearingBlockEntity.LockingSetting instance, int signal, Operation<Boolean> original) {
        return stabilized$lockedDefaultOption.get().shouldLock(signal);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/simulated_team/simulated/content/blocks/swivel_bearing/SwivelBearingBlockEntity;targetAngleDegrees:D",
                    opcode = Opcodes.PUTFIELD,
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void changeBehavior(CallbackInfo ci) {
        if (this.stabilized$lockedDefaultOption.get().stabilizationCoef() == 0) {
            return;
        }

        BlockEntityAccessor a = (BlockEntityAccessor) this;

        int bestSignal = a.stabilized$getLevel().getBestNeighborSignal(a.stabilized$getBlockPos());
        boolean shouldLock = this.stabilized$lockedDefaultOption.get().shouldLock(bestSignal);

        if (!shouldLock) {
            return;
        }

        SwivelBearingBlockEntity self = (SwivelBearingBlockEntity) (Object) this;

        this.targetAngleDegrees = 0;

        SubLevel attached = this.getAttachedSubLevel();
        SubLevel containing = this.getContainingSubLevel();

        if (containing != null) {
            Quaterniond q = containing.logicalPose().orientation();
            Direction facing = self.getBlockState().getValue(SwivelBearingBlock.FACING);

            this.targetAngleDegrees =
                    AngleHelper.angleLerp(
                            Math.sqrt(Math.abs(this.cogwheel.getSpeed())) / 16.0,
                            this.stabilized$lastStabilizedAngle,
                            -Math.toDegrees(
                                    getAxisAngle(
                                            new Quaternionf(q),
                                            facing
                                    )
                            )
                    )
                            * Math.signum(this.cogwheel.getSpeed())
                            * this.stabilized$lockedDefaultOption.get().stabilizationCoef();
        }

        if (Double.isNaN(this.targetAngleDegrees)) {
            this.targetAngleDegrees = 0;
        }

        this.targetAngleDegrees %= 360.0;

        if (attached instanceof ServerSubLevel serverAttached
                && containing instanceof ServerSubLevel serverContaining) {

            if (Math.sqrt(serverContaining.getMassTracker().getMass())
                    < serverAttached.getMassTracker().getMass()) {

                this.targetAngleDegrees =
                        AngleHelper.angleLerp(
                                (
                                        Math.sqrt(serverContaining.getMassTracker().getMass())
                                                / serverAttached.getMassTracker().getMass()
                                ) / 1.5,
                                this.stabilized$lastStabilizedAngle,
                                this.targetAngleDegrees
                        );
            }
        }

        this.stabilized$lastStabilizedAngle = this.targetAngleDegrees;
    }

    @WrapOperation(method = "updateServoCoefficients", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/config/ConfigBase$ConfigFloat;get()Ljava/lang/Object;",ordinal = 2))
    private Object modifyDamping(ConfigBase.ConfigFloat instance, Operation<Object> original) {
        if (this.stabilized$lockedDefaultOption.get().stabilizationCoef() == 0)
            return original.call(instance);
        return instance.get()/4;
    }
}
