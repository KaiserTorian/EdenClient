package at.haha007.edenclient.utils.area;

import at.haha007.edenclient.utils.config.loaders.ConfigLoader;
import at.haha007.edenclient.utils.config.loaders.Vec3iLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TorusAreaLoader implements ConfigLoader<CompoundTag, TorusArea> {

    @Override
    @NotNull
    public CompoundTag save(@NotNull TorusArea value) {
        CompoundTag tag = new CompoundTag();
        Vec3iLoader loader = new Vec3iLoader();
        IntArrayTag centerTag = loader.save(value.getCenter());
        tag.put("center", centerTag);
        tag.putDouble("majorRadius", value.getMajorRadius());
        tag.putDouble("minorRadius", value.getMinorRadius());
        return tag;
    }

    @Override
    @NotNull
    public TorusArea load(@NotNull CompoundTag nbtElement) {
        Vec3iLoader loader = new Vec3iLoader();
        Vec3i center = loader.load((IntArrayTag) Objects.requireNonNull(nbtElement.get("center")));
        double majorRadius = nbtElement.getDouble("majorRadius").orElseThrow();
        double minorRadius = nbtElement.getDouble("minorRadius").orElseThrow();
        return new TorusArea(new BlockPos(center.getX(), center.getY(), center.getZ()), majorRadius, minorRadius);
    }

    @Override
    @NotNull
    public CompoundTag parse(@NotNull String s) {
        CompoundTag tag = new CompoundTag();
        tag.put("center", new Vec3iLoader().save(Vec3i.ZERO));
        tag.putDouble("majorRadius", -1);
        tag.putDouble("minorRadius", -1);
        return tag;
    }

    public ConfigLoader<CompoundTag, BlockArea> asBlockAreaLoader() {
        return new ConfigLoader<>() {
            @Override
            @NotNull
            public CompoundTag save(@NotNull BlockArea value) {
                if (!(value instanceof TorusArea torusArea))
                    throw new IllegalArgumentException("Invalid block area type");
                return TorusAreaLoader.this.save(torusArea);
            }

            @Override
            @NotNull
            public BlockArea load(@NotNull CompoundTag nbtElement) {
                return TorusAreaLoader.this.load(nbtElement);
            }

            @Override
            @NotNull
            public CompoundTag parse(@NotNull String s) {
                return TorusAreaLoader.this.parse(s);
            }
        };
    }
}
