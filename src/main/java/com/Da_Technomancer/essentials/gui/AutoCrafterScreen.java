package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.AutoCrafterTileEntity;
import com.Da_Technomancer.essentials.gui.container.AutoCrafterContainer;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.crafting.IShapedRecipe;

import java.util.List;

public class AutoCrafterScreen extends AbstractContainerScreen<AutoCrafterContainer> implements RecipeUpdateListener{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/auto_crafter.png");
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");

	private final RecipeBookComponent recipeBook = new RecipeBookComponent();
	private boolean widthTooNarrow;
	private float time = 0;

	public AutoCrafterScreen(AutoCrafterContainer cont, Inventory playerInventory, Component text){
		super(cont, playerInventory, text);
		imageHeight = 186;
		//Fixes a vanilla UI bug- the field needs to be recalculated after changing ySize
		inventoryLabelY = imageHeight - 94;//MCP note: player inventory text overlay y position
	}

	protected void init(){
		super.init();
		widthTooNarrow = width < 379;
		recipeBook.init(width, height, Minecraft.getInstance(), widthTooNarrow, menu);
		leftPos = recipeBook.updateScreenPosition(width, imageWidth);
		addWidget(recipeBook);
//        children.add(recipeBook);
		setInitialFocus(recipeBook);
		addRenderableWidget(new ImageButton(leftPos + 5, height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (p_214076_1_) -> {
			recipeBook.initVisuals();
			recipeBook.toggleVisibility();
			leftPos = recipeBook.updateScreenPosition(width, imageWidth);
			((ImageButton) p_214076_1_).setPosition(leftPos + 5, height / 2 - 49);
		}));
	}

	@Override
	public void render(GuiGraphics matrix, int mouseX, int mouseY, float partialTicks){
		time += partialTicks;
		renderBackground(matrix);
		if(recipeBook.isVisible() && widthTooNarrow){
			renderBg(matrix, partialTicks, mouseX, mouseY);//MCP note: render screen
			recipeBook.render(matrix, mouseX, mouseY, partialTicks);
		}else{
			recipeBook.render(matrix, mouseX, mouseY, partialTicks);
			super.render(matrix, mouseX, mouseY, partialTicks);
			recipeBook.renderGhostRecipe(matrix, leftPos, topPos, true, partialTicks);//MCP note: renderGhostRecipe
		}

		renderTooltip(matrix, mouseX, mouseY);
		recipeBook.renderTooltip(matrix, leftPos, topPos, mouseX, mouseY);//MCP note: renderToolTip
		magicalSpecialHackyFocus(recipeBook);
	}

	@Override
	protected boolean isHovering(int p_195359_1_, int p_195359_2_, int p_195359_3_, int p_195359_4_, double p_195359_5_, double p_195359_7_){
		return (!widthTooNarrow || !recipeBook.isVisible()) && super.isHovering(p_195359_1_, p_195359_2_, p_195359_3_, p_195359_4_, p_195359_5_, p_195359_7_);
	}

	@Override
	public boolean mouseClicked(double x, double y, int p_mouseClicked_5_){
		if(recipeBook.mouseClicked(x, y, p_mouseClicked_5_)){
			return true;
		}else{
			return widthTooNarrow && recipeBook.isVisible() || super.mouseClicked(x, y, p_mouseClicked_5_);
		}
	}

	@Override
	protected boolean hasClickedOutside(double p_195361_1_, double p_195361_3_, int p_195361_5_, int p_195361_6_, int p_195361_7_){
		boolean flag = p_195361_1_ < (double) p_195361_5_ || p_195361_3_ < (double) p_195361_6_ || p_195361_1_ >= (double) (p_195361_5_ + imageWidth) || p_195361_3_ >= (double) (p_195361_6_ + imageHeight);
		return recipeBook.hasClickedOutside(p_195361_1_, p_195361_3_, leftPos, topPos, imageWidth, imageHeight, p_195361_7_) && flag;
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	@Override
	protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type){
		super.slotClicked(slotIn, slotId, mouseButton, type);
		recipeBook.slotClicked(slotIn);
	}

	@Override
	public void recipesUpdated(){
		recipeBook.recipesUpdated();
	}

	@Override
	public RecipeBookComponent getRecipeBookComponent(){
		return recipeBook;
	}

	@Override
	public void containerTick(){
		super.containerTick();
		recipeBook.tick();
	}

	protected ResourceLocation getBackgroundTexture(){
		return GUI_TEXTURE;
	}

	//MCP note: render screen
	@Override
	protected void renderBg(GuiGraphics matrix, float p_230450_2_, int p_230450_3_, int p_230450_4_){
		matrix.setColor(1.0F, 1.0F, 1.0F, 1.0F);
//		RenderSystem.setShaderTexture(0, getBackgroundTexture());
		//draw background
		matrix.blit(getBackgroundTexture(), leftPos, topPos, 0, 0, imageWidth, imageHeight);

		//foreground
		if(menu.te == null){
			return;
		}

		ItemStack[] inv = new ItemStack[19];
		//We start at 10 because the previous slots are ingredient storage slots not used for this
		for(int i = 10; i < inv.length; i++){
			inv[i] = menu.getSlot(i).getItem();
		}
		Recipe<CraftingContainer> iRecipe = menu.te.findRecipe(AutoCrafterTileEntity.prepareCraftingInv(inv), menu);

		if(iRecipe != null){
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
//			RenderSystem.enableRescaleNormal();
//			Lighting.turnBackOn();

			//If the recipe was set via recipe book/JEI, render the ingredients manually (if it was set via input slots, the slots will render the items for us)
			if(menu.te.recipe != null){
				boolean shaped = iRecipe instanceof IShapedRecipe;
				int width = 3;
				if(shaped){
					width = ((IShapedRecipe<CraftingContainer>) iRecipe).getRecipeWidth();
				}

				List<Ingredient> ingredients = iRecipe.getIngredients();
				for(int i = 0; i < ingredients.size(); i++){
					ItemStack[] matching = ingredients.get(i).getItems();
					if(matching.length == 0){
						continue;
					}
					ItemStack s = matching[(int) Math.floor(time / 30F) % matching.length];
//					itemRenderer.renderAndDecorateItem(matrix, s, 44 + 18 * (i % width) + leftPos, 15 + 18 * (i / width) + topPos);
					matrix.renderItem(s, 44 + 18 * (i % width) + leftPos, 15 + 18 * (i / width) + topPos);
				}
			}

			//Render the output
			ItemStack output = iRecipe.getResultItem(menu.te.getLevel().registryAccess());
//			itemRenderer.renderAndDecorateItem(matrix, output, 106 + leftPos, 33 + topPos);
//			itemRenderer.renderGuiItemDecorations(matrix, font, output, leftPos + 106, topPos + 33, null);
			matrix.renderItem(output, 106 + leftPos, 33 + topPos);
			matrix.renderItemDecorations(font, output, leftPos + 106, topPos + 33, null);

//			RenderSystem.disableRescaleNormal();
//			Lighting.turnOff();
		}
	}

	public static List<RecipeBookCategories> getRecipeCategories(){
		//Same categories as vanilla crafting table. Remember to change this if new crafting table categories are ever added
		return Lists.newArrayList(RecipeBookCategories.CRAFTING_SEARCH, RecipeBookCategories.CRAFTING_EQUIPMENT, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, RecipeBookCategories.CRAFTING_MISC, RecipeBookCategories.CRAFTING_REDSTONE);
	}
}
