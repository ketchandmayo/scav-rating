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
                // If it's a command execution, check if the player is OP
                boolean isOp = false;
                try {
                    Object server = this.player.getClass().getMethod("getServer").invoke(this.player);
                    Object playerList = server.getClass().getMethod("getPlayerList").invoke(server);
                    for (java.lang.reflect.Method m : playerList.getClass().getMethods()) {
                        if (m.getName().equals("isOp") && m.getParameterCount() == 1) {
                            Class<?> paramType = m.getParameterTypes()[0];
                            if (paramType == com.mojang.authlib.GameProfile.class) {
                                isOp = (Boolean) m.invoke(playerList, this.player.getGameProfile());
                            } else {
                                java.util.UUID id = this.player.getUUID();
                                String name = this.player.getScoreboardName();
                                for (java.lang.reflect.Constructor<?> c : paramType.getConstructors()) {
                                    if (c.getParameterCount() == 2) {
                                        try {
                                            Object nameAndId = c.newInstance(id, name);
                                            isOp = (Boolean) m.invoke(playerList, nameAndId);
                                            break;
                                        } catch (Exception e1) {
                                            try {
                                                Object nameAndId = c.newInstance(name, id);
                                                isOp = (Boolean) m.invoke(playerList, nameAndId);
                                                break;
                                            } catch (Exception e2) {}
                                        }
                                    }
                                }
                            }
                            break;
                        }
                    }
                } catch (Exception e) {}

                if (isOp) {
                    CheatTracker.markCheated(this.player);
                }
            }
        } catch (Throwable t) {
            // Safe fallback
        }
    }
}
