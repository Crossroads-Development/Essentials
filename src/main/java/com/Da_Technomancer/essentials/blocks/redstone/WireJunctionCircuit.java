package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.tileentities.redstone.WireJunctionTileEntity;
import com.Da_Technomancer.essentials.tileentities.redstone.WireTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class WireJunctionCircuit extends AbstractTile{

	public WireJunctionCircuit(){
		super("wire_junction_circuit");
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		//Wire junctions propagate block updates horizontally to make sure any attached circuit can update when a new connection is made/broken
		BlockEntity te = worldIn.getBlockEntity(pos);
		if(te instanceof WireTileEntity wte){

			//Prevent the repeated updating of the same wire within a gametick
			long worldTime = worldIn.getGameTime();
			if(worldTime == wte.lastUpdateTime){
				return;
			}

			wte.lastUpdateTime = worldTime;

			if(fromPos == null || fromPos.equals(pos)){
				for(Direction dir : Direction.Plane.HORIZONTAL){
					worldIn.neighborChanged(pos.relative(dir), this, pos);
				}
			}else{
				//If possible, only propagate the block update along the direction it came from- as this is a junction
				Direction dir = Direction.getNearest(pos.getX() - fromPos.getX(), pos.getY() - fromPos.getY(), pos.getZ() - fromPos.getZ());
				if(dir.getAxis() != Direction.Axis.Y){
					worldIn.neighborChanged(pos.relative(dir), this, pos);
				}
			}
		}

		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state){
		return new WireJunctionTileEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		worldIn.neighborChanged(pos, this, pos);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn){
		tooltip.add(new TranslatableComponent("tt.essentials.wire_junction_circuit"));
	}
}
