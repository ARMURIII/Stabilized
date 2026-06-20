package arr.armuriii.stabilized;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = Stabilized.MOD_ID)
public class Config {

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}
