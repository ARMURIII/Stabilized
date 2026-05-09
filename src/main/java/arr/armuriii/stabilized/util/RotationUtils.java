package arr.armuriii.stabilized.util;

import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import static java.lang.Math.atan2;

public class RotationUtils {

    public static double getAxisAngle(Quaternionf q, Direction direction) {
        final Vector3d ld = JOMLConversion.toJOML(Vec3.atLowerCornerOf(Direction.DOWN.getNormal()));
        q.transformInverse(ld);

        Vector3d forward = JOMLConversion.toJOML(Vec3.atLowerCornerOf(Direction.NORTH.getNormal()));
        q.transformInverse(forward);

        var XAngle = atan2(ld.z(), -ld.y());
        var YAngle = atan2(forward.x(), -forward.z());
        var ZAngle = -atan2(ld.x(), -ld.y());
        return switch (direction.getAxis()) {
            case X -> XAngle;
            case Y -> YAngle;
            case Z -> ZAngle;
        }*(direction.getAxisDirection()== Direction.AxisDirection.NEGATIVE ? -1 : 1);
    }
}
