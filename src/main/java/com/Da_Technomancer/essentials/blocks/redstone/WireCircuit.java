package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.ESProperties;
import com.Da_Technomancer.essentials.tileentities.redstone.WireTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class WireCircuit extends AbstractTile{

	public WireCircuit(){
		super("wire_circuit");
		registerDefaultState(defaultBlockState().setValue(ESProperties.CONNECTIONS, 0));
		ESBlocks.blockAddQue(this);//Register an item form only for the actual wire circuit
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder){
		builder.add(ESProperties.CONNECTIONS);
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		//Adjust blockstate visual
		int meta = 0;
		for(int i = 2; i < 6; i++){
			Direction dir = Direction.from3DDataValue(i);
			BlockState otherState = worldIn.getBlockState(pos.relative(dir));
			Block otherBlock = otherState.getBlock();
			if(otherBlock instanceof IWireConnect && ((IWireConnect) otherBlock).canConnect(dir.getOpposite(), otherState)){
				meta |= 1 << (i - 2);
			}
		}

		if(meta != state.getValue(ESProperties.CONNECTIONS)){
			worldIn.setBlock(pos, state.setValue(ESProperties.CONNECTIONS, meta), 2);
		}

		//Wires propogate block updates in all horizontal directions to make sure any attached circuit can update when a new connection is made/broken
		TileEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof WireTileEntity){
			WireTileEntity wte = (WireTileEntity) te;

			//Prevent the repeated updating of the same wire within a gametick
			long worldTime = worldIn.getGameTime();
			if(worldTime == wte.lastUpdateTime){
				return;
			}

			wte.lastUpdateTime = worldTime;

			for(Direction dir : Direction.Plane.HORIZONTAL){
				worldIn.neighborChanged(pos.relative(dir), this, pos);
			}
		}

		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn){
		return new WireTileEntity();
	}

	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		worldIn.neighborChanged(pos, this, pos);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt.essentials.wire_circuit"));
	}
}
