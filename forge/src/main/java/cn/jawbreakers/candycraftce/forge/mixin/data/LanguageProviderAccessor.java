package cn.jawbreakers.candycraftce.forge.mixin.data;

import net.minecraftforge.common.data.LanguageProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Created in 2026/11/1 21:55 by <a href="https://github.com/Bread-NiceCat">Bread-NiceCat</a>
 * Project: CandyCraftCE
 */
@Mixin(value = LanguageProvider.class, remap = false)
@Pseudo
public interface LanguageProviderAccessor {
	@Accessor("data")
	Map<String, String> getData();

	@Accessor("locale")
	String getLocale();
}
