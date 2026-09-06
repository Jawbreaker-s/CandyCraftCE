//package cn.jawbreakers.candycraftce.client
//
//import cn.jawbreakers.candycraftce.utils.ClientOnly
//import com.mojang.blaze3d.systems.RenderSystem
//import com.mojang.blaze3d.vertex.DefaultVertexFormat
//import com.mojang.blaze3d.vertex.Tesselator
//import com.mojang.blaze3d.vertex.VertexFormat
//import net.minecraft.client.Camera
//import net.minecraft.client.Minecraft
//import net.minecraft.client.ParticleStatus
//import net.minecraft.client.multiplayer.ClientLevel
//import net.minecraft.client.renderer.GameRenderer
//import net.minecraft.client.renderer.LevelRenderer
//import net.minecraft.client.renderer.LightTexture
//import net.minecraft.core.BlockPos
//import net.minecraft.core.Direction
//import net.minecraft.core.particles.ParticleOptions
//import net.minecraft.core.particles.ParticleTypes
//import net.minecraft.resources.ResourceLocation
//import net.minecraft.sounds.SoundEvents
//import net.minecraft.sounds.SoundSource
//import net.minecraft.tags.FluidTags
//import net.minecraft.util.Mth
//import net.minecraft.util.RandomSource
//import net.minecraft.world.level.biome.Biome
//import net.minecraft.world.level.block.Blocks
//import net.minecraft.world.level.block.CampfireBlock
//import net.minecraft.world.level.levelgen.Heightmap
//import java.util.function.Supplier
//import kotlin.math.max
//import kotlin.math.sqrt
//
//@ClientOnly
//object MilkRainRenderer {
//    private val MILK_RAIN = ResourceLocation("textures/environment/milk_rain.png")
//    private val SNOW = ResourceLocation("textures/environment/snow.png")
//    private val RAIN_SIZE_X = FloatArray(1024)
//    private val RAIN_SIZE_Z = FloatArray(1024)
//    private val RAIN_COLORS = arrayOf(
//        floatArrayOf(0.612f, 0.357f, 0.235f),  // Chocolate
//        floatArrayOf(1.0f, 0.878f, 0.639f) // Cream
//    )
//    private var rainSoundTime = 0
//
//    init {
//        for (z in 0..31) {
//            for (x in 0..31) {
//                val offsetX = (x - 16).toFloat()
//                val offsetZ = (z - 16).toFloat()
//                val length = Mth.sqrt(offsetX * offsetX + offsetZ * offsetZ)
//                val index = z * 32 + x
//                if (length == 0.0f) {
//                    RAIN_SIZE_X[index] = 0.0f
//                    RAIN_SIZE_Z[index] = 0.0f
//                } else {
//                    RAIN_SIZE_X[index] = -offsetZ / length
//                    RAIN_SIZE_Z[index] = offsetX / length
//                }
//            }
//        }
//    }
//
//    fun render(
//        level: ClientLevel, ticks: Int, partialTick: Float, lightTexture: LightTexture,
//        cameraX: Double, cameraY: Double, cameraZ: Double,
//    ) {
//        val strength = level.getRainLevel(partialTick)
//        if (strength <= 0.0f) {
//            return
//        }
//
//        lightTexture.turnOnLightLayer()
//        RenderSystem.disableCull()
//        RenderSystem.enableBlend()
//        RenderSystem.enableDepthTest()
//        RenderSystem.depthMask(Minecraft.useShaderTransparency())
//        RenderSystem.setShader(Supplier { GameRenderer.getParticleShader() })
//
//        val radius = if (Minecraft.useFancyGraphics()) 10 else 5
//        renderRain(level, ticks, partialTick, cameraX, cameraY, cameraZ, strength, radius)
//        renderSnow(level, ticks, partialTick, cameraX, cameraY, cameraZ, strength, radius)
//
//        RenderSystem.enableCull()
//        RenderSystem.disableBlend()
//        lightTexture.turnOffLightLayer()
//    }
//
//    private fun renderRain(
//        level: ClientLevel, ticks: Int, partialTick: Float, cameraX: Double, cameraY: Double,
//        cameraZ: Double, strength: Float, radius: Int,
//    ) {
//        RenderSystem.setShaderTexture(0, MILK_RAIN)
//        val tesselator = Tesselator.getInstance()
//        val buffer = tesselator.builder
//        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE)
//
//        val centerX = Mth.floor(cameraX)
//        val centerY = Mth.floor(cameraY)
//        val centerZ = Mth.floor(cameraZ)
//        val sample = BlockPos.MutableBlockPos()
//
//        for (worldZ in centerZ - radius..centerZ + radius) {
//            for (worldX in centerX - radius..centerX + radius) {
//                val columnHash = rainColumnHash(worldX, worldZ)
//                val index = (worldZ - centerZ + 16) * 32 + worldX - centerX + 16
//                val sideX = RAIN_SIZE_X[index] * 0.5
//                val sideZ = RAIN_SIZE_Z[index] * 0.5
//                sample.set(worldX.toDouble(), cameraY, worldZ.toDouble())
//                if (!level.isRainingAt(sample)) {
//                    continue
//                }
////                if (CandyPrecipitation.at(level, sample) !== Biome.Precipitation.RAIN) {
////                    continue
////                }
//
//                val surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ)
//                val bottomY = max(centerY - radius, surfaceY)
//                val topY = max(centerY + radius, surfaceY)
//                val lightY = max(surfaceY, centerY)
//                if (bottomY == topY) {
//                    continue
//                }
//
//                val seed =
//                    (worldX * worldX * 3121 + worldX * 45238971 + worldZ * worldZ * 418711 + worldZ * 13761).toLong()
//                val random = RandomSource.create(seed)
//                val phase =
//                    (ticks + worldX * worldX * 3121 + worldX * 45238971 + worldZ * worldZ * 418711 + worldZ * 13761) and 31
//                val scroll = -((phase + partialTick) / 32.0f) * (3.0f + random.nextFloat())
//                val distanceX = worldX + 0.5 - cameraX
//                val distanceZ = worldZ + 0.5 - cameraZ
//                val distance = sqrt(distanceX * distanceX + distanceZ * distanceZ).toFloat() / radius
//                val alpha = ((1.0f - distance * distance) * 0.5f + 0.5f) * strength
//                sample.set(worldX, lightY, worldZ)
//                val light = LevelRenderer.getLightColor(level, sample)
//
//                val x = worldX - cameraX + 0.5
//                val z = worldZ - cameraZ + 0.5
//                val bottom = bottomY - cameraY
//                val top = topY - cameraY
//                val bottomV = bottomY * 0.25f + scroll
//                val topV = topY * 0.25f + scroll
//                val color = RAIN_COLORS[columnHash and 1]
//
//                // Vanilla intentionally maps bottomV to the top vertices. Reversing this makes rain move upward.
//                buffer.vertex(x - sideX, top, z - sideZ).uv(0.0f, bottomV).color(color[0], color[1], color[2], alpha)
//                    .uv2(light).endVertex()
//                buffer.vertex(x + sideX, top, z + sideZ).uv(1.0f, bottomV).color(color[0], color[1], color[2], alpha)
//                    .uv2(light).endVertex()
//                buffer.vertex(x + sideX, bottom, z + sideZ).uv(1.0f, topV).color(color[0], color[1], color[2], alpha)
//                    .uv2(light).endVertex()
//                buffer.vertex(x - sideX, bottom, z - sideZ).uv(0.0f, topV).color(color[0], color[1], color[2], alpha)
//                    .uv2(light).endVertex()
//            }
//        }
//        tesselator.end()
//    }
//
//    private fun renderSnow(
//        level: ClientLevel, ticks: Int, partialTick: Float, cameraX: Double, cameraY: Double,
//        cameraZ: Double, strength: Float, radius: Int,
//    ) {
//        RenderSystem.setShaderTexture(0, SNOW)
//        val tesselator = Tesselator.getInstance()
//        val buffer = tesselator.builder
//        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE)
//
//        val centerX = Mth.floor(cameraX)
//        val centerY = Mth.floor(cameraY)
//        val centerZ = Mth.floor(cameraZ)
//        val scroll = -((ticks and 511) + partialTick) / 512.0f
//        val sample = BlockPos.MutableBlockPos()
//
//        for (worldZ in centerZ - radius..centerZ + radius) {
//            for (worldX in centerX - radius..centerX + radius) {
//                val index = (worldZ - centerZ + 16) * 32 + worldX - centerX + 16
//                val sideX = RAIN_SIZE_X[index] * 0.5
//                val sideZ = RAIN_SIZE_Z[index] * 0.5
//                sample.set(worldX.toDouble(), cameraY, worldZ.toDouble())
//                if (CandyPrecipitation.at(level, sample) !== Biome.Precipitation.SNOW) {
//                    continue
//                }
//
//                val surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ)
//                val bottomY = max(centerY - radius, surfaceY)
//                val topY = max(centerY + radius, surfaceY)
//                val lightY = max(surfaceY, centerY)
//                if (bottomY == topY) {
//                    continue
//                }
//
//                val seed =
//                    (worldX * worldX * 3121 + worldX * 45238971 + worldZ * worldZ * 418711 + worldZ * 13761).toLong()
//                val random = RandomSource.create(seed)
//                val uOffset = random.nextFloat() + (ticks + partialTick) * 0.01f * random.nextGaussian().toFloat()
//                val vOffset = random.nextFloat() + (ticks + partialTick) * 0.001f * random.nextGaussian().toFloat()
//                val distanceX = worldX + 0.5 - cameraX
//                val distanceZ = worldZ + 0.5 - cameraZ
//                val distance = sqrt(distanceX * distanceX + distanceZ * distanceZ).toFloat() / radius
//                val alpha = ((1.0f - distance * distance) * 0.3f + 0.5f) * strength
//                sample.set(worldX, lightY, worldZ)
//                val light = LevelRenderer.getLightColor(level, sample)
//
//                val x = worldX - cameraX + 0.5
//                val z = worldZ - cameraZ + 0.5
//                val bottom = bottomY - cameraY
//                val top = topY - cameraY
//                val bottomV = bottomY * 0.25f + scroll + vOffset
//                val topV = topY * 0.25f + scroll + vOffset
//
//                buffer.vertex(x - sideX, top, z - sideZ).uv(uOffset, bottomV).color(1.0f, 1.0f, 1.0f, alpha).uv2(light)
//                    .endVertex()
//                buffer.vertex(x + sideX, top, z + sideZ).uv(uOffset + 1.0f, bottomV).color(1.0f, 1.0f, 1.0f, alpha)
//                    .uv2(light).endVertex()
//                buffer.vertex(x + sideX, bottom, z + sideZ).uv(uOffset + 1.0f, topV).color(1.0f, 1.0f, 1.0f, alpha)
//                    .uv2(light).endVertex()
//                buffer.vertex(x - sideX, bottom, z - sideZ).uv(uOffset, topV).color(1.0f, 1.0f, 1.0f, alpha).uv2(light)
//                    .endVertex()
//            }
//        }
//        tesselator.end()
//    }
//
//    fun tick(level: ClientLevel, ticks: Int, camera: Camera) {
//        val strength = level.getRainLevel(1.0f) / (if (Minecraft.useFancyGraphics()) 1.0f else 2.0f)
//        if (strength <= 0.0f) {
//            return
//        }
//
//        val minecraft = Minecraft.getInstance()
//        val random = RandomSource.create(ticks.toLong() * 312987231L)
//        val cameraPos = BlockPos.containing(camera.position)
//        var lastLanding: BlockPos? = null
//        val attempts = (100.0f * strength * strength).toInt() / (if (minecraft.options.particles()
//                .get() == ParticleStatus.DECREASED
//        ) 2 else 1)
//
//        for (i in 0..<attempts) {
//            val offsetX = random.nextInt(21) - 10
//            val offsetZ = random.nextInt(21) - 10
//            val surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, cameraPos.offset(offsetX, 0, offsetZ))
//            if (surface.y <= level.minBuildHeight || surface.y > cameraPos.y + 10 || surface.y < cameraPos.y - 10 || CandyPrecipitation.at(
//                    level,
//                    surface
//                ) !== Biome.Precipitation.RAIN
//            ) {
//                continue
//            }
//
//            val landing = surface.below()
//            lastLanding = landing
//            if (minecraft.options.particles().get() == ParticleStatus.MINIMAL) {
//                break
//            }
//
//            val localX = random.nextDouble()
//            val localZ = random.nextDouble()
//            val state = level.getBlockState(landing)
//            val fluid = level.getFluidState(landing)
//            val shape = state.getCollisionShape(level, landing)
//            val collisionHeight = shape.max(Direction.Axis.Y, localX, localZ)
//            val fluidHeight = fluid.getHeight(level, landing).toDouble()
//            val landingHeight = max(collisionHeight, fluidHeight)
//            val particle: ParticleOptions =
//                if (!fluid.`is`(FluidTags.LAVA) && !state.`is`(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLitCampfire(state))
//                    CCParticleTypes.MILK_RAIN_SPLASH.get()
//                else
//                    ParticleTypes.SMOKE
//            level.addParticle(
//                particle, landing.x + localX, landing.y + landingHeight,
//                landing.z + localZ, 0.0, 0.0, 0.0
//            )
//        }
//
//        if (lastLanding != null && random.nextInt(3) < rainSoundTime++) {
//            rainSoundTime = 0
//            if (lastLanding.y > cameraPos.y + 1
//                && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, cameraPos).y > Mth.floor(
//                    cameraPos.y.toFloat()
//                )
//            ) {
//                level.playLocalSound(
//                    lastLanding,
//                    SoundEvents.WEATHER_RAIN_ABOVE,
//                    SoundSource.WEATHER,
//                    0.1f,
//                    0.5f,
//                    false
//                )
//            } else {
//                level.playLocalSound(lastLanding, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2f, 1.0f, false)
//            }
//        }
//    }
//
//    private fun rainColumnHash(x: Int, z: Int): Int {
//        var hash = x * 73428767 xor z * 912931
//        hash = hash xor (hash ushr 16)
//        return hash
//    }
//}
