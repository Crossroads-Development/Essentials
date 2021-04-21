package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.ILinkTE;
import com.Da_Technomancer.essentials.tileentities.LinkHelper;
import com.Da_Technomancer.essentials.tileentities.redstone.RedstoneTransmitterTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class RedstoneTransmitter extends ContainerBlock implements IWireConnect{

	public RedstoneTransmitter(){
		super(AbstractBlock.Properties.of(Material.STONE).strength(0.5F).sound(SoundType.STONE));
		String name = "redstone_transmitter";
		setRegistryName(name);
		ESBlocks.toRegister.add(this);
		ESBlocks.blockAddQue(this);
		registerDefaultState(defaultBlockState().setValue(ESProperties.COLOR, DyeColor.WHITE));
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		worldIn.getBlockTicks().scheduleTick(pos, this, RedstoneUtil.DELAY, TickPriority.HIGH);

		if(blockIn != Blocks.REDSTONE_WIRE && !(blockIn instanceof RedstoneDiodeBlock)){
			//Simple optimization- if the source of the block update is just a redstone signal changing, we don't need to force a full connection rebuild
			TileEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof RedstoneTransmitterTileEntity){
				((RedstoneTransmitterTileEntity) te).buildConnections();
			}
		}
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side){
		return true;
	}

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		neighborChanged(state, worldIn, pos, this, pos, false);
	}

	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand){
		TileEntity rawTE = worldIn.getBlockEntity(pos);
		if(rawTE instanceof RedstoneTransmitterTileEntity){
			((RedstoneTransmitterTileEntity) rawTE).refreshOutput();
		}
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit){
		//Handle linking and dyeing
		ItemStack heldItem = playerIn.getItemInHand(hand);
		TileEntity te = worldIn.getBlockEntity(pos);
		Item item;
		if(LinkHelper.isLinkTool(heldItem) && te instanceof RedstoneTransmitterTileEntity){
			if(!worldIn.isClientSide){
				LinkHelper.wrench((ILinkTE) te, heldItem, playerIn);
			}
			return ActionResultType.SUCCESS;
		}else if((item = heldItem.getItem()) instanceof DyeItem && te instanceof RedstoneTransmitterTileEntity){
			if(!worldIn.isClientSide){
				((RedstoneTransmitterTileEntity) te).dye(((DyeItem) item).getDyeColor());
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TranslationTextComponent("tt.essentials.reds_trans.desc"));
		tooltip.add(new TranslationTextComponent("tt.essentials.reds_trans.linking"));
		tooltip.add(new TranslationTextComponent("tt.essentials.reds_trans.dyes"));
	}

	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn){
		return new RedstoneTransmitterTileEntity();
	}

	@Override
	public BlockRenderType getRenderShape(BlockState state){
		return BlockRenderType.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> container){
		container.add(ESProperties.COLOR);
	}

	@Override
	public boolean canConnect(Direction side, BlockState state){
		return true;
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving){
		if(state.getBlock() != newState.getBlock()){
			TileEntity te = worldIn.getBlockEntity(pos);
			if(te instanceof RedstoneTransmitterTileEntity){
				((RedstoneTransmitterTileEntity) te).linkHelper.unlinkAllEndpoints();
			}
		}
	}
}
