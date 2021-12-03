package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import com.Da_Technomancer.essentials.tileentities.redstone.RedstoneTransmitterTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.ticks.TickPriority;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class RedstoneTransmitter extends BaseEntityBlock implements IWireConnect{

	public RedstoneTransmitter(){
		super(ESBlocks.getRockProperty());
		String name = "redstone_transmitter";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
		registerDefaultState(defaultBlockState().setValue(ESProperties.COLOR, DyeColor.WHITE));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		worldIn.scheduleTick(pos, this, RedstoneUtil.DELAY, TickPriority.HIGH);

		if(blockIn != Blocks.REDSTONE_WIRE && !(blockIn instanceof DiodeBlock)){
			//Simple optimization- if the source of the block update is just a redstone signal changing, we don't need to force a full connection rebuild
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof RedstoneTransmitterTileEntity){
				((RedstoneTransmitterTileEntity) te).buildConnections();
			}
		}
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side){
		return true;
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		neighborChanged(state, worldIn, pos, this, pos, false);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand){
		BlockEntity rawTE = worldIn.getBlockEntity(pos);
		if(rawTE instanceof RedstoneTransmitterTileEntity){
			((RedstoneTransmitterTileEntity) rawTE).refreshOutput();
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit){
		//Handle linking and dyeing
		ItemStack heldItem = playerIn.getItemInHand(hand);
		BlockEntity te = worldIn.getBlockEntity(pos);
		Item item;
		if(LinkHelper.isLinkTool(heldItem) && te instanceof RedstoneTransmitterTileEntity){
			if(!worldIn.isClientSide){
				LinkHelper.wrench((ILinkTE) te, heldItem, playerIn);
			}
			return InteractionResult.SUCCESS;
		}else if((item = heldItem.getItem()) instanceof DyeItem && te instanceof RedstoneTransmitterTileEntity){
			if(!worldIn.isClientSide){
				((RedstoneTransmitterTileEntity) te).dye(((DyeItem) item).getDyeColor());
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip, TooltipFlag advanced){
		tooltip.add(new TranslatableComponent("tt.essentials.reds_trans.desc"));
		tooltip.add(new TranslatableComponent("tt.essentials.reds_trans.linking"));
		tooltip.add(new TranslatableComponent("tt.essentials.reds_trans.dyes"));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new RedstoneTransmitterTileEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> container){
		container.add(ESProperties.COLOR);
	}

	@Override
	public boolean canConnect(Direction side, BlockState state){
		return true;
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
		if(state.getBlock() != newState.getBlock()){
			BlockEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof RedstoneTransmitterTileEntity){
				((RedstoneTransmitterTileEntity) te).linkHelper.unlinkAllEndpoints();
			}
		}

		super.onRemove(state, worldIn, pos, newState, isMoving);
	}
}
