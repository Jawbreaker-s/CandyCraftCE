package cn.jawbreakers.candycraftce.mixin.item;


import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AxeItem.class)
public interface AxeItemAccessor {
	@Accessor("STRIPPABLES")
	static Map<Block, Block> getStrippedBlocks() {
		throw new AssertionError();
	}

	@Mutable
	@Accessor("STRIPPABLES")
	static void setStrippedBlocks(Map<Block, Block> strippedBlocks) {
		throw new AssertionError();
	}

}
