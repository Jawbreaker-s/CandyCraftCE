package cn.jawbreakers.candycraftce.forge;

import cn.jawbreakers.candycraftce.CandyCraftCE;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 负责在玩家切换世界/维度时，替换或覆盖原版加载屏幕（ReceivingLevelScreen、
 * LevelLoadingScreen、GenericDirtMessageScreen），显示 CandyCraft 自定义的
 * 地牢 / 糖果世界加载背景。
 * <p>
 * 与“传送门扭曲画面”无关：扭曲由 CCClient 里的 portalOverlayTicks 控制。
 */
@Mod.EventBusSubscriber(modid = CandyCraftCE.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CandyLoadingScreens {

	// ============ 加载界面纹理 ============

	private static final ResourceLocation PUDDING_LOADING_TOP =
			new ResourceLocation(CandyCraftCE.MOD_ID, "textures/block/pudding_side.png");
	private static final ResourceLocation FLOUR_LOADING_BACKGROUND =
			new ResourceLocation(CandyCraftCE.MOD_ID, "textures/block/flour.png");
	private static final ResourceLocation JAWBREAKER_LOADING_BACKGROUND =
			new ResourceLocation(CandyCraftCE.MOD_ID, "textures/block/jaw_breaker_block.png");
	private static final ResourceLocation JAWBREAKER_RUNE_BACKGROUND =
			new ResourceLocation(CandyCraftCE.MOD_ID, "textures/block/jaw_breaker_light.png");

	// ============ 加载状态 ============

	private static boolean dungeonLoadingActive;
	private static boolean dungeonLoadingSuguard;
	private static int dungeonLoadingTimeoutTicks;

	private static boolean candyWorldLoadingActive;
	private static int candyWorldLoadingTimeoutTicks;
	private static int candyWorldLoadingGraceTicks;
	private static boolean candyWorldLoadingExit;

	private static boolean loadingBackgroundDrawnThisFrame;

	private CandyLoadingScreens() {
	}

	// ================================================================
	//  公共入口（供 CCClient 调用）
	// ================================================================

	/**
	 * 每个客户端 tick 由 CCClient 调用。
	 *
	 * @param portalOverlayActive 本 tick 是否处于传送门扭曲状态下，
	 *                            与原 CCClient 里 portalOverlayTicks > 0 对应。
	 */
	public static void tick(Minecraft minecraft, boolean portalOverlayActive) {
		if (minecraft.player == null) {
			reset();
			return;
		}
		tickDungeonLoadingScreen(minecraft);
		tickCandyWorldLoadingScreen(minecraft, portalOverlayActive);
	}

	/**
	 * 玩家为空或断开连接时清理所有加载状态。
	 */
	public static void reset() {
		dungeonLoadingActive = false;
		dungeonLoadingTimeoutTicks = 0;
		clearCandyWorldLoadingState(false);
	}

	/**
	 * 玩家进入糖果传送门方块时由 CCClient 调用，提前标记糖果世界加载。
	 */
	public static void onCandyPortalEntered() {
		beginCandyWorldLoadingScreen();
	}

	// ================================================================
	//  事件处理
	// ================================================================

//	@SubscribeEvent
//	public static void onClientRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
//		if (!event.getLevel().isClientSide()) {
//			return;
//		}
//		if (!event.getLevel().getBlockState(event.getPos()).is(CCBlocks.BLOCK_TELEPORTER.get())) {
//			return;
//		}
//		BlockState portal = event.getLevel().getBlockState(event.getPos());
//		if (portal.getValue(DungeonTeleporterBlock.ROLE) != DungeonTeleporterBlock.PortalRole.ENTRY) {
//			return;
//		}
//		DungeonTeleporterBlock.DungeonKind kind = portal.getValue(DungeonTeleporterBlock.DUNGEON);
//		if (hasMatchingDungeonKey(event.getEntity(), kind)) {
//			clearDungeonLoadingScreen();
//			return;
//		}
//		dungeonLoadingSuguard = kind == DungeonTeleporterBlock.DungeonKind.SUGUARD;
//		beginDungeonLoadingScreen();
//	}

	@SubscribeEvent
	public static void renderLoadingBackground(ScreenEvent.BackgroundRendered event) {
		if (!isLevelLoadingScreen(event.getScreen())) {
			return;
		}
		loadingBackgroundDrawnThisFrame = true;
		renderCustomLoadingBackground(event.getGuiGraphics(), event.getScreen());
	}

	@SubscribeEvent
	public static void beforeLoadingScreenRender(ScreenEvent.Render.Pre event) {
		if (!isLevelLoadingScreen(event.getScreen())) {
			return;
		}
		loadingBackgroundDrawnThisFrame = false;
		if (isDungeonLoadingContext()) {
			loadingBackgroundDrawnThisFrame = true;
//			renderDungeonLoadingFrame(event.getGuiGraphics());
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void afterLoadingScreenRender(ScreenEvent.Render.Post event) {
		if (!loadingBackgroundDrawnThisFrame
				&& isLevelLoadingScreen(event.getScreen())
				&& !isDungeonLoadingContext()) {
			renderCustomLoadingBackground(event.getGuiGraphics(), event.getScreen());
		}
	}

	// ================================================================
	//  内部逻辑
	// ================================================================

//	private static boolean hasMatchingDungeonKey(Player player, DungeonTeleporterBlock.DungeonKind kind) {
//		for (InteractionHand hand : InteractionHand.values()) {
//			ItemStack stack = player.getItemInHand(hand);
//			if (stack.getItem() instanceof JellyDungeonKeyItem key && key.matchesDungeon(kind)) {
//				return true;
//			}
//		}
//		return false;
//	}

	private static boolean isDungeonLoadingContext() {
		Minecraft minecraft = Minecraft.getInstance();
		return dungeonLoadingActive || isDungeonLevel(minecraft.level);
	}

//	private static void renderDungeonLoadingFrame(GuiGraphics graphics) {
//		Minecraft minecraft = Minecraft.getInstance();
//		int width = minecraft.getWindow().getGuiScaledWidth();
//		int height = minecraft.getWindow().getGuiScaledHeight();
//		renderDungeonLoadingBackground(graphics, width, height);
//
//		boolean suguard = dungeonLoadingSuguard
//				|| (minecraft.level != null
//				&& "suguard_dungeon".equals(minecraft.level.dimension().location().getPath()));
//		ItemStack key = new ItemStack(suguard ? CCItems.SUGUARD_KEY.get() : CCItems.JELLY_KEY.get());
//
//		// 加载屏可能暂停游戏 tick，因此用墙钟时间驱动上下浮动。
//		double timeSeconds = System.nanoTime() / 1_000_000_000.0D;
//		float bob = (float) Math.sin(timeSeconds * 2.2D) * 4.0F;
//
//		graphics.pose().pushPose();
//		graphics.pose().translate(width * 0.5F, height * 0.5F + bob, 0.0F);
//		graphics.pose().scale(2.0F, 2.0F, 1.0F);
//		graphics.renderItem(key, -8, -8);
//		graphics.pose().popPose();
//	}

	private static void renderCustomLoadingBackground(GuiGraphics graphics, Screen screen) {
		Minecraft minecraft = Minecraft.getInstance();
		boolean dungeon = dungeonLoadingActive || isDungeonLevel(minecraft.level);
		int width = minecraft.getWindow().getGuiScaledWidth();
		int height = minecraft.getWindow().getGuiScaledHeight();
		if (dungeon) {
			renderDungeonLoadingBackground(graphics, width, height);
		} else if (candyWorldLoadingActive || isCandyWorldLevel(minecraft.level)) {
			renderCandyWorldLoadingBackground(graphics, width, height);
		}
	}

	private static void beginDungeonLoadingScreen() {
		Minecraft minecraft = Minecraft.getInstance();
		dungeonLoadingActive = true;
		dungeonLoadingTimeoutTicks = 20 * 60;
		if (!(minecraft.screen instanceof ReceivingLevelScreen
				|| minecraft.screen instanceof LevelLoadingScreen)) {
			minecraft.setScreen(new GenericDirtMessageScreen(Component.translatable("chat.generating")));
		}
	}

	private static void clearDungeonLoadingScreen() {
		Minecraft minecraft = Minecraft.getInstance();
		dungeonLoadingActive = false;
		dungeonLoadingTimeoutTicks = 0;
		if (minecraft.screen instanceof GenericDirtMessageScreen) {
			minecraft.setScreen(null);
		}
	}

	private static void tickDungeonLoadingScreen(Minecraft minecraft) {
		if (!dungeonLoadingActive) {
			return;
		}
		if (isDungeonLevel(minecraft.level)
				&& !(minecraft.screen instanceof ReceivingLevelScreen)
				&& !(minecraft.screen instanceof LevelLoadingScreen)) {
			dungeonLoadingActive = false;
			dungeonLoadingTimeoutTicks = 0;
			if (minecraft.screen instanceof GenericDirtMessageScreen) {
				minecraft.setScreen(null);
			}
			return;
		}
		if (--dungeonLoadingTimeoutTicks <= 0) {
			dungeonLoadingActive = false;
			if (minecraft.screen instanceof GenericDirtMessageScreen) {
				minecraft.setScreen(null);
			}
		}
	}

	private static void beginCandyWorldLoadingScreen() {
		Minecraft minecraft = Minecraft.getInstance();
		boolean inCandyWorld = isCandyWorldLevel(minecraft.level);
		if (!candyWorldLoadingActive) {
			candyWorldLoadingExit = inCandyWorld;
		} else {
			candyWorldLoadingExit |= inCandyWorld;
		}
		candyWorldLoadingActive = true;
		candyWorldLoadingTimeoutTicks = 20 * 30;
		candyWorldLoadingGraceTicks = 20 * 3;
	}

	private static void tickCandyWorldLoadingScreen(Minecraft minecraft, boolean portalOverlayActive) {
		if (!candyWorldLoadingActive) {
			return;
		}
		if (minecraft.screen instanceof ReceivingLevelScreen
				|| minecraft.screen instanceof LevelLoadingScreen) {
			if (--candyWorldLoadingTimeoutTicks <= 0) {
				clearCandyWorldLoadingState(false);
			}
			return;
		}
		if (portalOverlayActive) {
			candyWorldLoadingGraceTicks = 20 * 3;
			return;
		}
		if (--candyWorldLoadingGraceTicks <= 0 || --candyWorldLoadingTimeoutTicks <= 0) {
			clearCandyWorldLoadingState(true);
		}
	}

	private static void clearCandyWorldLoadingState(boolean closeLegacyScreen) {
		candyWorldLoadingActive = false;
		candyWorldLoadingTimeoutTicks = 0;
		candyWorldLoadingGraceTicks = 0;
		candyWorldLoadingExit = false;
		if (closeLegacyScreen && Minecraft.getInstance().screen instanceof GenericDirtMessageScreen) {
			Minecraft.getInstance().setScreen(null);
		}
	}

	private static boolean isLevelLoadingScreen(Screen screen) {
		return screen instanceof ReceivingLevelScreen
				|| screen instanceof LevelLoadingScreen
				|| screen instanceof GenericDirtMessageScreen;
	}

	private static void renderDungeonLoadingBackground(GuiGraphics graphics, int width, int height) {
		renderTiledBackground(graphics, JAWBREAKER_LOADING_BACKGROUND, width, height, 32, 0xFFFFFFFF);
		int tile = 32;
		for (int x = 0; x < width + tile; x += tile * 4) {
			for (int y = ((x / tile) % 2) * tile * 2; y < height + tile; y += tile * 4) {
				graphics.blit(JAWBREAKER_RUNE_BACKGROUND, x, y, 0, 0, tile, tile, tile, tile);
			}
		}
	}

	private static void renderCandyWorldLoadingBackground(GuiGraphics graphics, int width, int height) {
		int tile = 32;
		renderTiledBackground(graphics, FLOUR_LOADING_BACKGROUND, width, height, tile, 0xFFFFFFFF);
		for (int x = 0; x < width + tile; x += tile) {
			graphics.blit(PUDDING_LOADING_TOP, x, 0, 0, 0, tile, tile, tile, tile);
		}
	}

	private static void renderTiledBackground(GuiGraphics graphics, ResourceLocation texture,
	                                          int width, int height, int tile, int color) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		for (int x = 0; x < width + tile; x += tile) {
			for (int y = 0; y < height + tile; y += tile) {
				graphics.blit(texture, x, y, 0, 0, tile, tile, tile, tile);
			}
		}
		if ((color >>> 24) < 255) {
			graphics.fill(0, 0, width, height, color);
		}
		RenderSystem.disableBlend();
	}

	private static boolean isDungeonLevel(Level level) {
		if (level == null) {
			return false;
		}
		ResourceLocation dimension = level.dimension().location();
		return CandyCraftCE.MOD_ID.equals(dimension.getNamespace())
				&& ("jelly_dungeon".equals(dimension.getPath())
				|| "suguard_dungeon".equals(dimension.getPath()));
	}

	private static boolean isCandyWorldLevel(Level level) {
		if (level == null) {
			return false;
		}
		ResourceLocation dimension = level.dimension().location();
		return CandyCraftCE.MOD_ID.equals(dimension.getNamespace())
				&& "candy_world".equals(dimension.getPath());
	}
}