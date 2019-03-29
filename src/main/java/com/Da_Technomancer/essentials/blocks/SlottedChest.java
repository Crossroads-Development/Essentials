package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.tileentities.SlottedChestTileEntity;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

public class SlottedChest extends BlockContainer{

	protected SlottedChest(){
		super(Properties.create(Material.WOOD).hardnessAndResistance(2).sound(SoundType.WOOD));
		String name = "slotted_chest";
		setRegistryName(name);
		EssentialsBlocks.toRegister.add(this);
		EssentialsBlocks.blockAddQue(this);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader world){
		return new SlottedChestTileEntity();
	}

	@Override
	public void onPlayerDestroy(IWorld world, BlockPos pos, IBlockState blockstate){
		SlottedChestTileEntity te = (SlottedChestTileEntity) world.getTileEntity(pos);
		InventoryHelper.dropInventoryItems(world.getWorld(), pos, te.iInv);
		super.onPlayerDestroy(world, pos, blockstate);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ){
		if(!worldIn.isRemote){
			TileEntity te = worldIn.getTileEntity(pos);
			if(te instanceof SlottedChestTileEntity){
				NetworkHooks.openGui((EntityPlayerMP) playerIn, ((SlottedChestTileEntity) te).iInv, pos);
			}
		}
		return true;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag advanced){
		tooltip.add(new TextComponentString("Slots can be locked in the UI to only accept one item type"));
		tooltip.add(new TextComponentString("The partitions make it bigger somehow"));
	}
}
