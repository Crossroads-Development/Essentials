package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.AutoCrafterContainer;
import com.Da_Technomancer.essentials.tileentities.AutoCrafterBlockEntity;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.crafting.IShapedRecipe;

import java.util.List;

public class AutoCrafterScreen extends ContainerScreen<AutoCrafterContainer> implements IRecipeShownListener{

	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/auto_crafter.png");
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");

	private final RecipeBookGui recipeBook = new RecipeBookGui();
	private boolean widthTooNarrow;
	private float time = 0;

	public AutoCrafterScreen(AutoCrafterContainer cont, PlayerInventory playerInventory, ITextComponent text){
		super(cont, playerInventory, text);
		imageHeight = 186;
		//Fixes a vanilla UI bug- the field needs to be recalculated after changing ySize
		inventoryLabelY = imageHeight - 94;//MCP note: player inventory text overlay y position
	}

	protected void init() {
		super.init();
		widthTooNarrow = width < 379;
		recipeBook.init(width, height, Minecraft.getInstance(), widthTooNarrow, menu);
		leftPos = recipeBook.updateScreenPosition(widthTooNarrow, width, imageWidth);
		children.add(recipeBook);
		setInitialFocus(recipeBook);
		addButton(new ImageButton(leftPos + 5, height / 2 - 49, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (p_214076_1_) -> {
			recipeBook.initVisuals(widthTooNarrow);
			recipeBook.toggleVisibility();
			leftPos = recipeBook.updateScreenPosition(widthTooNarrow, width, imageWidth);
			((ImageButton) p_214076_1_).setPosition(leftPos + 5, height / 2 - 49);
		}));
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks){
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
	public void removed(){
		recipeBook.removed();
		super.removed();
	}

	@Override
	public RecipeBookGui getRecipeBookComponent(){
		return recipeBook;
	}

	@Override
	public void tick(){
		super.tick();
		recipeBook.tick();
	}

	protected ResourceLocation getBackgroundTexture(){
		return GUI_TEXTURE;
	}

	//MCP note: render screen
	@Override
	protected void renderBg(MatrixStack matrix, float p_230450_2_, int p_230450_3_, int p_230450_4_){
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bind(getBackgroundTexture());
		//draw background
		blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		//foreground
		if(menu.te == null){
			return;
		}

		ItemStack[] inv = new ItemStack[19];
		//We start at 10 because the previous slots are ingredient storage slots not used for this
		for(int i = 10; i < inv.length; i++){
			inv[i] = menu.getSlot(i).getItem();
		}
		IRecipe<CraftingInventory> iRecipe = menu.te.findRecipe(AutoCrafterBlockEntity.prepareCraftingInv(inv), menu);

		if(iRecipe != null){
			RenderSystem.enableRescaleNormal();

			RenderHelper.turnBackOn();

			//If the recipe was set via recipe book/JEI, render the ingredients manually (if it was set via input slots, the slots will render the items for us)
			if(menu.te.recipe != null){
				boolean shaped = iRecipe instanceof IShapedRecipe;
				int width = 3;
				if(shaped){
					width = ((IShapedRecipe<CraftingInventory>) iRecipe).getRecipeWidth();
				}

				List<Ingredient> ingredients = iRecipe.getIngredients();
				for(int i = 0; i < ingredients.size(); i++){
					ItemStack[] matching = ingredients.get(i).getItems();
					if(matching.length == 0){
						continue;
					}
					ItemStack s = matching[(int) Math.floor(time / 30F) % matching.length];
					itemRenderer.renderGuiItem(s, 44 + 18 * (i % width) + leftPos, 15 + 18 * (i / width) + topPos);
				}
			}

			//Render the output
			ItemStack output = iRecipe.getResultItem();
			itemRenderer.renderGuiItem(output, 106 + leftPos, 33 + topPos);
			if(output.getCount() > 1){
				itemRenderer.renderGuiItemDecorations(font, output, leftPos + 106, topPos + 33, null);
			}

			RenderSystem.disableRescaleNormal();
			RenderHelper.turnOff();
		}
	}

	public static List<RecipeBookCategories> getRecipeCategories(){
		//Same categories as vanilla crafting table. Remember to change this if new crafting table categories are ever added
		return Lists.newArrayList(RecipeBookCategories.CRAFTING_SEARCH, RecipeBookCategories.CRAFTING_EQUIPMENT, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, RecipeBookCategories.CRAFTING_MISC, RecipeBookCategories.CRAFTING_REDSTONE);
	}
}
