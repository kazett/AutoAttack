// ##kazet (c) 2022-2026 All rights reserved##
package autoattack;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class AutoAttackMixin {
	@Shadow
	public ClientPlayerInteractionManager interactionManager;
	@Shadow
	public ClientPlayerEntity player;

	// private void handleBlockBreaking(boolean bl) {
	@Inject(method = "handleBlockBreaking(Z)V", at = @At("HEAD"), cancellable = true)
	public void onHandleBlockBreaking(boolean isBreakPressed, CallbackInfo info) {
		if (isBreakPressed) {
			//player.sendMessage(Text.of(player.getInventory().getMainHandStack().getItem().toString()));
			if ((player.getInventory().getMainHandStack().getItem().toString().contains("sword") && AutoAttackConfig.preventsHittingBlocksSwords) || AutoAttackConfig.preventsHittingBlocks) {
				interactionManager.cancelBlockBreaking();
				info.cancel();
			}
		}
	}
}
// ##kazet (c) 2022-2026 All rights reserved##
