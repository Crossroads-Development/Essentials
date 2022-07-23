package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.api.redstone.IWireConnect;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.api.ESProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nullable;
import java.util.List;

public class WireCircuit extends AbstractTile{

	public WireCircuit(){
		super("wire_circuit");
		String name = "wire_circuit";
		registerDefaultState(defaultBlockState().setValue(ESProperties.CONNECTIONS, 0));
		ESBlocks.blockAddQue(name, this);//Register an item form only for the actual wire circuit
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(ESProperties.CONNECTIONS);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
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
		BlockEntity te = worldIn.getBlockEntity(pos);
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
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new WireTileEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		worldIn.neighborChanged(pos, this, pos);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(Component.translatable("tt.essentials.wire_circuit"));
	}
}
