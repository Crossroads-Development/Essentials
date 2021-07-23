package com.Da_Technomancer.essentials.blocks.redstone;

import com.Da_Technomancer.essentials.tileentities.redstone.WireJunctionBlockEntity;
import com.Da_Technomancer.essentials.tileentities.redstone.WireBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Level;

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
		if(te instanceof WireBlockEntity){
			WireBlockEntity wte = (WireBlockEntity) te;

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
	public BlockEntity newBlockEntity(IBlockReader worldIn){
		return new WireJunctionBlockEntity();
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
		worldIn.neighborChanged(pos, this, pos);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn){
		tooltip.add(new TranslationTextComponent("tt.essentials.wire_junction_circuit"));
	}
}
