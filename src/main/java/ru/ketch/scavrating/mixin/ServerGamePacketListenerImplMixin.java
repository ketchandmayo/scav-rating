package ru.ketch.scavrating.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.ketch.scavrating.CheatTracker;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleChatCommand", at = @At("HEAD"))
    private void onHandleChatCommand(ServerboundChatCommandPacket packet, CallbackInfo ci) {
        try {
            String command = packet.command();
            String cmdName = command.split(" ")[0].toLowerCase();
            if (!cmdName.equals("scavenger") && !cmdName.equals("scav") && !cmdName.equals("say") && !cmdName.equals("msg") && !cmdName.equals("me") && !cmdName.equals("seed")) {
                if (this.player.server.getPlayerList().isOp(this.player.getGameProfile())) {
                    CheatTracker.markCheated(this.player);
                }
            }
        } catch (Throwable t) {
            // Safe fallback
        }
    }
}
