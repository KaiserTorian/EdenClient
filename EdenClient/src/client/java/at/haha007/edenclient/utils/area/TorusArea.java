package at.haha007.edenclient.utils.area;

import at.haha007.edenclient.command.CommandManager;
import at.haha007.edenclient.render.Renderer;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.xpple.clientarguments.arguments.CBlockPosArgument;
import dev.xpple.clientarguments.arguments.CCoordinates;
import lombok.Getter;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TorusArea implements BlockArea {
    @Getter
    private final BlockPos center;
    @Getter
    private final double majorRadius;
    @Getter
    private final double minorRadius;

    public TorusArea(BlockPos center, double majorRadius, double minorRadius) {
        this.center = center;
        this.majorRadius = majorRadius;
        this.minorRadius = minorRadius;
    }

    @Override
    public boolean contains(BlockPos pos) {
        double dx = pos.getX() - center.getX();
        double dy = pos.getY() - center.getY();
        double dz = pos.getZ() - center.getZ();
        double distanceToRing = Math.sqrt(dx * dx + dz * dz) - majorRadius;
        return distanceToRing * distanceToRing + dy * dy <= minorRadius * minorRadius;
    }

    private BoundingBox bounds(double padding) {
        int horizontal = (int) Math.ceil(majorRadius + minorRadius + padding);
        int vertical = (int) Math.ceil(minorRadius + padding);
        Vec3i min = new Vec3i(center.getX() - horizontal, center.getY() - vertical, center.getZ() - horizontal);
        Vec3i max = new Vec3i(center.getX() + horizontal, center.getY() + vertical, center.getZ() + horizontal);
        return new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
    }

    @Override
    public Stream<BlockPos> stream() {
        return streamArea(bounds(0)).filter(this::contains);
    }

    @Override
    public Stream<BlockPos> wallStream() {
        return streamArea(bounds(2)).filter(this::isWall);
    }

    @Override
    public Stream<BlockPos> floorStream() {
        return streamArea(bounds(2)).filter(this::isFloor);
    }

    @Override
    public Stream<BlockPos> ceilingStream() {
        return streamArea(bounds(2)).filter(this::isCeiling);
    }

    @Override
    public BlockPos center() {
        return center;
    }

    @Override
    public Renderer renderer() {
        int majorSteps = (int) Math.clamp(majorRadius * 4, 16, 64);
        int minorSteps = (int) Math.clamp(minorRadius * 4, 8, 32);
        return new TorusAreaRenderer(Vec3.atCenterOf(center), majorRadius, minorRadius, majorSteps, minorSteps);
    }

    private static TorusArea fromCommand(CommandContext<FabricClientCommandSource> context, String keyCenter, String keyMajorRadius, String keyMinorRadius) {
        BlockPos center = CBlockPosArgument.getBlockPos(context, keyCenter);
        double majorRadius = DoubleArgumentType.getDouble(context, keyMajorRadius);
        double minorRadius = DoubleArgumentType.getDouble(context, keyMinorRadius);
        return new TorusArea(center, majorRadius, minorRadius);
    }

    public static RequiredArgumentBuilder<FabricClientCommandSource, CCoordinates> command(
            String keyCenter,
            String keyMajorRadius,
            String keyMinorRadius,
            BiConsumer<CommandContext<FabricClientCommandSource>, TorusArea> executor) {
        RequiredArgumentBuilder<FabricClientCommandSource, CCoordinates> center = CommandManager.argument(keyCenter, CBlockPosArgument.blockPos());
        RequiredArgumentBuilder<FabricClientCommandSource, Double> majorRadius = CommandManager.argument(keyMajorRadius, DoubleArgumentType.doubleArg(0));
        RequiredArgumentBuilder<FabricClientCommandSource, Double> minorRadius = CommandManager.argument(keyMinorRadius, DoubleArgumentType.doubleArg(0));

        minorRadius.executes(c -> {
            TorusArea area = fromCommand(c, keyCenter, keyMajorRadius, keyMinorRadius);
            executor.accept(c, area);
            return 1;
        });
        majorRadius.then(minorRadius);
        center.then(majorRadius);
        return center;
    }

    private Stream<BlockPos> streamArea(BoundingBox box) {
        Vec3i min = new Vec3i(box.minX(), box.minY(), box.minZ());
        Vec3i max = new Vec3i(box.maxX(), box.maxY(), box.maxZ());
        Vec3i size = max.subtract(min).offset(1, 1, 1);

        Stream<BlockPos> stream = Stream.generate(new Supplier<>() {
            private long i = 0;

            public BlockPos get() {
                int x = (int) (i % size.getX());
                int y = (int) ((i / size.getX()) % size.getY());
                int z = (int) ((i / size.getX() / size.getY()) % size.getZ());
                if (z > size.getZ())
                    return null;
                BlockPos pos = new BlockPos(x, y, z).offset(min);
                i++;
                return pos;
            }
        });
        return stream.limit((long) box.getXSpan() * box.getYSpan() * box.getZSpan()).filter(Objects::nonNull);
    }

    @Override
    public String toString() {
        return "TorusArea{" +
                "center=" + center +
                ", majorRadius=" + majorRadius +
                ", minorRadius=" + minorRadius +
                '}';
    }
}
