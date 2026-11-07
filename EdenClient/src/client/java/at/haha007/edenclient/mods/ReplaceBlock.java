package at.haha007.edenclient.mods;

import at.haha007.edenclient.EdenClient;
import at.haha007.edenclient.annotations.Mod;
import at.haha007.edenclient.callbacks.PlayerBlockDrestroyedCallback;
import at.haha007.edenclient.utils.PlayerUtils;
import at.haha007.edenclient.utils.Scheduler;
import at.haha007.edenclient.utils.config.ConfigSubscriber;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static at.haha007.edenclient.command.CommandManager.literal;
import static at.haha007.edenclient.command.CommandManager.register;
import static at.haha007.edenclient.utils.PlayerUtils.getPlayer;

@Mod(dependencies = Scheduler.class)
public class ReplaceBlock {

    boolean enabled = false;

    public ReplaceBlock() {
        registerCommand();
        PlayerBlockDrestroyedCallback.EVENT.register(this::onDestroyed,getClass());
    }

    private void registerCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> node = literal("ereplaceblock");
        node.then(literal("toggle").executes(_ -> {
            enabled = !enabled;
            PlayerUtils.sendModMessage((enabled ? "ReplaceBlock enabled" : "ReplaceBlock disabled"));
            return 1;
        }));
        register(node,
                "ReplaceBlock replaces broken blocks.");
    }


    private void onDestroyed(LocalPlayer localPlayer, BlockPos blockPos, Direction direction) {
        if (!enabled) {
            return;
        }
        EdenClient.getMod(Scheduler.class).scheduleSyncDelayed(() -> clickPos(blockPos),1);
    }

    private void clickPos(BlockPos bp) {
        Direction dir = Direction.UP;
        MultiPlayerGameMode im = Minecraft.getInstance().gameMode;
        if (im == null) {
            return;
        }
        BlockHitResult hitResult = new BlockHitResult(Vec3.atLowerCornerOf(bp.relative(dir)), dir, bp, false);
        im.useItemOn(getPlayer(), InteractionHand.OFF_HAND, hitResult);
    }
}
