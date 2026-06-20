package arr.armuriii.stabilized.advancement.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GyroscopicBearingAssemblesTrigger extends SimpleCriterionTrigger<GyroscopicBearingAssemblesTrigger.TriggerInstance> {

    public GyroscopicBearingAssemblesTrigger() {

    }

    @Override
    public @NotNull Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Doubles distance) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((p_337349_) -> p_337349_
                .group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                MinMaxBounds.Doubles.CODEC.optionalFieldOf("distance", MinMaxBounds.Doubles.atLeast(10)).forGetter(TriggerInstance::distance))
                .apply(p_337349_, TriggerInstance::new));
    }
}
