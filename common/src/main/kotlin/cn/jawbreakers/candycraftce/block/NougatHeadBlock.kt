package cn.jawbreakers.candycraftce.block


class NougatHeadBlock(properties: Properties) //	@Override
//	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
//		super.onPlace(state, level, pos, oldState, moving);
//		if (level.isClientSide || moving || !(level instanceof ServerLevel serverLevel)) {
//			return;
//		}
//		if (!isNougatBody(level.getBlockState(pos.below()))
//				|| !isNougatBody(level.getBlockState(pos.below(2)))) {
//			return;
//		}
//
//		int bodySegments = 0;
//		BlockPos cursor = pos.below();
//		while (cursor.getY() > level.getMinBuildHeight() && isNougatBody(level.getBlockState(cursor))) {
//			level.removeBlock(cursor, false);
//			bodySegments++;
//			cursor = cursor.below();
//		}
//		level.removeBlock(pos, false);
//
//		NougatGolemEntity base = CCEntityTypes.NOUGAT_GOLEM.get().create(serverLevel);
//		if (base == null) {
//			return;
//		}
//		base.moveTo(pos.getX() + 0.5D, pos.getY() - bodySegments, pos.getZ() + 0.5D, state.getValue(FACING).toYRot(), 0.0F);
//		serverLevel.addFreshEntity(base);
//		NougatGolemEntity.createStack(serverLevel, base, bodySegments);
//	}
//
//	private static boolean isNougatBody(BlockState state) {
//		return state.is(CCBlocks.NOUGAT_BLOCK.get())
//				|| state.is(CCBlocks.CHISELED_NOUGAT_BLOCK.get())
//				|| state.is(CCBlocks.SQUARE_PATTERN_NOUGAT_BLOCK.get());
//	}
    : FacingModelBlock(properties)
