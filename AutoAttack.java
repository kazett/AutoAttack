// ##kazet (c) 2022-2026 All rights reserved##
package autoattack;

import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class AutoAttack implements ClientModInitializer {
	public static String AUTOATTACK_VERSION;
	public static String SERVER_VERSION;
	public static String MINECRAFT_VERSION;
	public static Boolean UPDATE = false;

	private static int attackDelayTicks = 0;
	private static final Random RANDOM = new Random();
	private static int forcePressTicks = 0;
	private static boolean weForcedPress = false;

	@Override
	public void onInitializeClient() {
		ModContainer mod = FabricLoader.getInstance().getModContainer("autoattack")
				.orElseThrow(() -> new IllegalStateException("mod container not found"));

		AUTOATTACK_VERSION = String.valueOf(mod.getMetadata().getVersion());
		MINECRAFT_VERSION = UpdateUtil.getMinecraftVersion();

		try {
			JsonObject json = UpdateUtil.getJsonObject("https://raw.githubusercontent.com/kazet/AutoAttack/updates/updates.json");
			if (json != null && json.get(MINECRAFT_VERSION) != null) {
				SERVER_VERSION = json.get(MINECRAFT_VERSION).getAsJsonObject().get("latest").getAsString();
				UPDATE = UpdateUtil.compare(AUTOATTACK_VERSION, SERVER_VERSION) == -1;
			}
		} catch (Exception ignored) {}

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			if (UPDATE && AutoAttackConfig.checkUpdate && mc.player != null) {
				mc.player.sendMessage(Text.literal("AutoAttack: new Update Detected! Version: " + SERVER_VERSION), false);
				UPDATE = false;
			}

			// force press release
			if (forcePressTicks > 0) {
				mc.options.attackKey.setPressed(true);
				forcePressTicks--;
			} else if (weForcedPress) {
				mc.options.attackKey.setPressed(false);
				weForcedPress = false;
			}

			boolean holding = mc.options.attackKey.isPressed();
			boolean canAttack = (holding || AutoAttackConfig.afkAttack)
					&& mc.player != null && mc.world != null && mc.interactionManager != null;

			if (canAttack && mc.player.getAttackCooldownProgress(0.5F) >= 1.0F) {
				if (attackDelayTicks > 0) {
					attackDelayTicks--;
				} else {
					boolean didAttack = tryAttack(mc, holding);
					if (didAttack) {
						attackDelayTicks = RANDOM.nextInt(3); // 0-2
					}
				}
			} else {
				attackDelayTicks = 0;
			}

			// auto bow / crossbow / trident
			if (mc.options.useKey.isPressed() && mc.player != null && mc.interactionManager != null) {
				ItemStack stack = mc.player.getActiveItem();
				Item item = stack.getItem();

				if (item == Items.BOW && AutoAttackConfig.autoBow) {
					float progress = BowItem.getPullProgress(stack.getMaxUseTime() - mc.player.getItemUseTimeLeft() - 1);
					if (progress == 1.0F) mc.interactionManager.stopUsingItem(mc.player);
				}
				if (item == Items.CROSSBOW && AutoAttackConfig.autoCrossBow) {
					float progress = (stack.getMaxUseTime() - mc.player.getItemUseTimeLeft()) / (float) CrossbowItem.getPullTime(stack);
					if (progress > 1.0F) {
						mc.interactionManager.stopUsingItem(mc.player);
						mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
					}
				}
				if (item == Items.TRIDENT && AutoAttackConfig.autoTrident) {
					float progress = (stack.getMaxUseTime() - mc.player.getItemUseTimeLeft()) / 10.0F;
					if (progress > 1.0F) mc.interactionManager.stopUsingItem(mc.player);
				}
			}
		});
	}

	// check target based on config
	private static boolean isValidTarget(Entity e, net.minecraft.client.MinecraftClient mc) {
		if (e == null || !e.isAlive() || !e.isAttackable() || e == mc.player) return false;

		return switch (AutoAttackConfig.targetMode) {
			case HOSTILE -> e instanceof Monster;
			case EVERYONE -> e instanceof LivingEntity;
			case ALL -> true;
		};
	}

	private static boolean tryAttack(net.minecraft.client.MinecraftClient mc, boolean holding) {
		if (mc.crosshairTarget == null) return false;

		Entity target = null;

		if (mc.crosshairTarget.getType() == HitResult.Type.BLOCK && AutoAttackConfig.cleanCut) {
			BlockHitResult blockHit = (BlockHitResult) mc.crosshairTarget;
			BlockPos pos = blockHit.getBlockPos();
			BlockState state = mc.world.getBlockState(pos);

			if (state.getCollisionShape(mc.world, pos).isEmpty() || state.getHardness(mc.world, pos) == 0.0F) {
				float reach = mc.interactionManager.getReachDistance();
				Vec3d cam = mc.player.getCameraPosVec(1.0F);
				Vec3d rot = mc.player.getRotationVec(1.0F);
				Vec3d end = cam.add(rot.x * reach, rot.y * reach, rot.z * reach);
				EntityHitResult result = ProjectileUtil.raycast(mc.player, cam, end, new Box(cam, end),
						e -> !e.isSpectator() && e.isAttackable(), reach * reach);
				if (result != null) target = result.getEntity();
			}
		} else if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
			target = ((EntityHitResult) mc.crosshairTarget).getEntity();
		}

		if (!isValidTarget(target, mc)) return false;

		if (!holding) {
			forcePressTicks = 2;
			weForcedPress = true;
			mc.options.attackKey.setPressed(true);
		}

		mc.interactionManager.attackEntity(mc.player, target);
		mc.player.swingHand(Hand.MAIN_HAND);
		return true;
	}
}
// ##kazet (c) 2022-2026 All rights reserved##
