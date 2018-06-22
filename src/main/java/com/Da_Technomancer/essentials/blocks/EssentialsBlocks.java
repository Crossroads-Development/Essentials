package com.Da_Technomancer.essentials.blocks;

import com.Da_Technomancer.essentials.items.EssentialsItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;

public class EssentialsBlocks{

	public static Brazier brazier;
	public static SlottedChest slottedChest;
	public static SortingHopper sortingHopper;
	public static PortExtender portExtender;
	public static FertileSoil fertileSoil;
	public static CandleLilyPad candleLilyPad;
	public static ItemChute itemChute;
	public static ItemChutePort itemChutePort;

	public static final ArrayList<Block> toRegister = new ArrayList<Block>();

	/**
	 * Registers the item form of a block and the item model.
	 * @param block The block to register
	 * @return The passed block for convenience.
	 */
	public static <T extends Block> T blockAddQue(T block){
		return blockAddQue(block, true);
	}

	/**
	 * Registers the item form of a block and an if registerModel item model.
	 * @param block The block to register
	 * @param registerModel whether to register a model.
	 * @return The passed block for convenience.
	 */
	public static <T extends Block> T blockAddQue(T block, boolean registerModel){
		Item item = new ItemBlock(block).setRegistryName(block.getRegistryName());
		EssentialsItems.toRegister.add(item);
		if(registerModel){
			EssentialsItems.itemAddQue(item);
		}
		return block;
	}

	/**
	 * Registers the item form of a block and the item model for each metadata up to endMeta.
	 * @param block
	 * @param endMeta The end meta value of the item.
	 * @param multiItem
	 * @return The block for convenience.
	 */
	public static <T extends Block> T blockAddQueRange(T block, int endMeta, Item multiItem){
		EssentialsItems.toRegister.add(multiItem);
		multiItem.setRegistryName(block.getRegistryName());
		for(int i = 0; i <= endMeta; i++){
			EssentialsItems.toClientRegister.put(Pair.of(multiItem, i), new ModelResourceLocation(block.getRegistryName(), "inventory"));
		}
		return block;
	}

	public static void init(){
		brazier = new Brazier();
		slottedChest = new SlottedChest();
		sortingHopper = new SortingHopper();
		candleLilyPad = new CandleLilyPad();
		fertileSoil = new FertileSoil();
		portExtender = new PortExtender();
		itemChute = new ItemChute();
		itemChutePort = new ItemChutePort();
	}
}
