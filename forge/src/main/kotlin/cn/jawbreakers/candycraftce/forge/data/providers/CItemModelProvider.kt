package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE
import net.minecraft.data.PackOutput
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class CItemModelProvider(output: PackOutput, efHelper: ExistingFileHelper) :
    ItemModelProvider(output, CandyCraftCE.MOD_ID, efHelper) {

    override fun registerModels() {
    }
}