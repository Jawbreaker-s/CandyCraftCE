package cn.jawbreakers.candycraftce.forge.data.providers

import cn.jawbreakers.candycraftce.CandyCraftCE
import net.minecraft.data.PackOutput
import net.minecraftforge.client.model.generators.BlockModelProvider
import net.minecraftforge.common.data.ExistingFileHelper

class CBlockModelProvider(output: PackOutput, efHelper: ExistingFileHelper) :
    BlockModelProvider(output, CandyCraftCE.MOD_ID, efHelper) {

    override fun registerModels() {

    }

}