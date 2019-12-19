package com.Da_Technomancer.essentials.gui;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.gui.container.ConstantCircuitContainer;
import com.Da_Technomancer.essentials.packets.EssentialsPackets;
import com.Da_Technomancer.essentials.packets.SendNBTToServer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;

public class ConstantCircuitScreen extends ContainerScreen<ConstantCircuitContainer>{

	private static final ResourceLocation SEARCH_BAR_TEXTURE = new ResourceLocation(Essentials.MODID, "textures/gui/search_bar.png");
	private TextFieldWidget searchBar;


	public ConstantCircuitScreen(ConstantCircuitContainer cont, PlayerInventory playerInventory, ITextComponent text){
		super(cont, playerInventory, text);
		ySize = 18;
		xSize = 144;
	}

	@Override
	protected void init(){
		super.init();
		searchBar = new TextFieldWidget(font, (width - xSize) / 2 + 4, (height - ySize) / 2 + 8, 144 - 4, 18, I18n.format("container.search_bar"));
		searchBar.setCanLoseFocus(false);
		searchBar.changeFocus(true);
		searchBar.setTextColor(-1);
		searchBar.setDisabledTextColour(-1);
		searchBar.setEnableBackgroundDrawing(false);
		searchBar.setMaxStringLength(20);
		searchBar.setResponder(this::entryChanged);
		searchBar.setValidator(s -> {
			final String whitelist = "0123456789 xX*/+-^piPIeE().";
			for(int i = 0; i < s.length(); i++){
				if(!whitelist.contains(s.substring(i, i + 1))){
					return false;
				}
			}

			return true;
		});
		children.add(searchBar);
		setFocusedDefault(searchBar);

		searchBar.setText(container.conf);
	}

	@Override
	public void resize(Minecraft p_resize_1_, int p_resize_2_, int p_resize_3_){
		String s = searchBar.getText();
		init(p_resize_1_, p_resize_2_, p_resize_3_);
		searchBar.setText(s);
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_){
		if(p_keyPressed_1_ == 256){
			minecraft.player.closeScreen();
		}

		return searchBar.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) || searchBar.func_212955_f() || super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks){
		renderBackground();
		super.render(mouseX, mouseY, partialTicks);
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		searchBar.render(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		//drawTexturedModelRectangle

		minecraft.getTextureManager().bindTexture(SEARCH_BAR_TEXTURE);
		blit((width - xSize) / 2, (height - ySize) / 2, 0, 0, xSize, 18, xSize, 18);
	}

	private void entryChanged(String newFilter){
		float output = 0;


		boolean working = true;

		//Parse the newFilter string into a list of numbers and operations, stored in formula
		//Possibilities to handle: numbers, pi (upper and lowercase), e, E (treat as 10^), ^, x & X, /, (, ), +, -
		ArrayList<Object> formula = new ArrayList<>();
		final String opChars = "+-xX*/()^";
		int index = 0;
//		abort:
		while(index < newFilter.length()){
			//Find if the current active index contains a number, and if so add it
			int subInd = index;
			while(subInd < newFilter.length() && (Character.isDigit(newFilter.charAt(subInd)) || newFilter.charAt(subInd) == '.')){
				subInd++;
			}
			if(index < subInd){
				//Found a number
				try{
					//Add the entire number to the formula, and jump past it
					float val = Float.parseFloat(newFilter.substring(index, subInd));
					formula.add(val);
					index = subInd;
				}catch(NumberFormatException e){
					working = false;
					break;
				}
			}else if(newFilter.charAt(index) == 'e'){
				formula.add((float) Math.E);
				index++;
			}else if(newFilter.length() > index + 1 && newFilter.substring(index, index + 2).toLowerCase().equals("pi")){
				formula.add((float) Math.PI);
				index += 2;
			}else if(opChars.contains(newFilter.substring(index, index + 1))){
				//Handle operations +-Xx/()^
				char c = newFilter.charAt(index);
				formula.add(c == 'x' || c == 'X' ? '*' : newFilter.charAt(index));
				index++;
			}else if(newFilter.charAt(index) == 'E'){
				//Treat E as meaning x10^
				formula.add('*');
				formula.add(10F);
				formula.add('^');
				index++;
			}else if(newFilter.charAt(index) == ' '){
				//Ignore spaces
				index++;
			}else{
				//Some unhandled combination has been entered. Abort
				working = false;
				break;
			}
		}


		if(working){
			//Run through the entire formula and make sure it's legal
			int openParens = 0;
			boolean prevNumb = false;
			boolean prevReqNextNumber = false;
			boolean reqNextNumber = false;
			abort:
			for(Object o : formula){
				//Whether this term can be treated as a number from the left side
				boolean numberFromLeft = false;
				//Whether this term can be treated as a number from the right side
				boolean numberFromRight = false;
				//You might think those two values would be the same, but you would be wrong


				if(o instanceof Character){
					switch((char) o){
						case '(':
							openParens++;
							numberFromLeft = true;//Block in parenthesis can be interpreted as a number
							break;
						case ')':
							if(openParens == 0){
								//More close parens then open parens
								working = false;
								break abort;
							}else{
								openParens--;
								numberFromRight = true;//Block in parenthesis can be interpreted as a number
							}
							break;
						case '*':
						case '/':
						case '^':
						case '+':
							if(prevNumb){
								reqNextNumber = true;
							}else{
								//Orphaned operation. Not legal.
								working = false;
								break abort;
							}
							break;
						case '-':
							numberFromLeft = true;
							reqNextNumber = true;
							break;
						default:
							//Should never happen
							Essentials.logger.warn("Invalid formula state! Report to mod author, and give them this: " + newFilter);
							working = false;
							break abort;

					}
				}else if(o instanceof Float){
					numberFromLeft = true;
					numberFromRight = true;
				}else{
					//Should never happen
					Essentials.logger.warn("Invalid formula state! Report to mod author, and give them this: " + newFilter);
					working = false;
					break;
				}

				if(prevReqNextNumber && !numberFromLeft){
					//The previous term required that the next term be a number. It isn't.
					working = false;
					break;
				}

				prevNumb = numberFromRight;
				prevReqNextNumber = reqNextNumber;
				reqNextNumber = false;
			}

			if(prevReqNextNumber){
				working = false;
			}

			//Add missing close parenthesis to the end
			for(int i = 0; i < openParens; i++){
				formula.add(')');
			}
		}

		if(working){
			ArrayList<Object> formulaActive = new ArrayList<>(formula.size());
			formulaActive.addAll(formula);

			try{
				float value = operate(formulaActive);
				if(Float.isNaN(value) || Float.isInfinite(value) || value < 0F){
					output = 0;
				}else{
					output = value;
				}
			}catch(Exception e){
				Essentials.logger.warn("Error interpreting formula; Report to mod author");
				e.printStackTrace();
				Essentials.logger.warn("Full formula data: [" + newFilter + "]");
				Essentials.logger.warn("Parsed formula: ");
				for(Object o : formula){
					Essentials.logger.warn("\tFormula item: " + o.toString());
				}
				output = 0;
			}
		}
		container.output = output;
		CompoundNBT nbt = new CompoundNBT();
		nbt.putFloat("value", output);
		nbt.putString("config", newFilter);
		if(container.pos != null){
			EssentialsPackets.channel.sendToServer(new SendNBTToServer(nbt, container.pos));
		}
	}


	private static float operate(ArrayList<Object> formula){
		if(formula.size() == 0){
			return 0;
		}

		//Order of operations must be obeyed
		while(formula.size() > 1){
			//Do parenthesis first
			int openParenIndex = formula.indexOf('(');
			if(openParenIndex != -1){

				//Find the matching close paren
				int openParens = 1;
				int closeParenIndex = openParenIndex;
				while(openParens != 0 && closeParenIndex < formula.size() - 1){
					closeParenIndex++;
					Object o = formula.get(closeParenIndex);
					if(o instanceof Character){
						if((char) o == '('){
							openParens++;
						}else if((char) o == ')'){
							openParens--;
						}
					}
				}

				ArrayList<Object> subForm = new ArrayList<>(closeParenIndex - openParenIndex - 1);
				formula.remove(openParenIndex);//Remove open paren
				for(int i = openParenIndex; i < closeParenIndex - 1; i++){
					subForm.add(formula.get(openParenIndex));
					formula.remove(openParenIndex);//Remove everything in parens
				}
				formula.remove(openParenIndex);//Remove close paren

				formula.add(openParenIndex, operate(subForm));//Perform the formula inside parenthesis first, and replace the term in parenthesis with the calculates value
				continue;
			}

			//Exponents
			int expIndex = formula.indexOf('^');
			if(expIndex != -1){
				//Apply the negative sign(s) if they exist
				if(formula.size() - expIndex > 2){
					Float o = applyNeg(formula.get(expIndex + 1), formula.get(expIndex + 2));
					if(o != null){
						formula.remove(expIndex + 1);
						formula.remove(expIndex + 1);
						formula.add(expIndex + 1, o);
					}
				}

				if(expIndex > 1){
					Float o = applyNeg(formula.get(expIndex - 2), formula.get(expIndex - 1));
					if(o != null){
						expIndex -= 1;
						formula.remove(expIndex - 1);
						formula.remove(expIndex - 1);
						formula.add(expIndex - 1, o);
					}
				}

				float prev = (float) formula.get(expIndex - 1);
				float next = (float) formula.get(expIndex + 1);
				formula.remove(expIndex - 1);
				formula.remove(expIndex - 1);
				formula.remove(expIndex - 1);
				formula.add(expIndex - 1, (float) Math.pow(prev, next));
				continue;
			}

			//Multiplication via * symbol
			int multIndex = formula.indexOf('*');
			if(multIndex != -1){
				//Apply the negative sign(s) if they exist
				if(formula.size() - multIndex > 2){
					Float o = applyNeg(formula.get(multIndex + 1), formula.get(multIndex + 2));
					if(o != null){
						formula.remove(multIndex + 1);
						formula.remove(multIndex + 1);
						formula.add(multIndex + 1, o);
					}
				}

				if(multIndex > 1){
					Float o = applyNeg(formula.get(multIndex - 2), formula.get(multIndex - 1));
					if(o != null){
						multIndex -= 1;
						formula.remove(multIndex - 1);
						formula.remove(multIndex - 1);
						formula.add(multIndex - 1, o);
					}
				}

				float prev = (float) formula.get(multIndex - 1);
				float next = (float) formula.get(multIndex + 1);
				formula.remove(multIndex - 1);
				formula.remove(multIndex - 1);
				formula.remove(multIndex - 1);
				formula.add(multIndex - 1, prev * next);
				continue;
			}

			//Multiplication via adjacent floats
			boolean didMult = false;
			for(int i = 0; i < formula.size() - 1; i++){
				Object o = formula.get(i);
				Object o1 = formula.get(i + 1);

				if(o instanceof Float && o1 instanceof Float){
					formula.remove(i);
					formula.remove(i);
					formula.add(i, (float) o * (float) o1);
					didMult = true;
					break;
				}
			}
			if(didMult){
				continue;
			}

			//Division
			int divIndex = formula.indexOf('/');
			if(divIndex != -1){
				//Apply the negative sign(s) if they exist
				if(formula.size() - divIndex > 2){
					Float o = applyNeg(formula.get(divIndex + 1), formula.get(divIndex + 2));
					if(o != null){
						formula.remove(divIndex + 1);
						formula.remove(divIndex + 1);
						formula.add(divIndex + 1, o);
					}
				}

				if(divIndex > 1){
					Float o = applyNeg(formula.get(divIndex - 2), formula.get(divIndex - 1));
					if(o != null){
						divIndex -= 1;
						formula.remove(divIndex - 1);
						formula.remove(divIndex - 1);
						formula.add(divIndex - 1, o);
					}
				}

				float prev = (float) formula.get(divIndex - 1);
				float next = (float) formula.get(divIndex + 1);
				formula.remove(divIndex - 1);
				formula.remove(divIndex - 1);
				formula.remove(divIndex - 1);
				formula.add(divIndex - 1, prev / next);
				continue;
			}

			//Addition
			int sumIndex = formula.indexOf('+');
			if(sumIndex != -1){
				//Apply the negative sign(s) if they exist
				if(formula.size() - sumIndex > 2){
					Float o = applyNeg(formula.get(sumIndex + 1), formula.get(sumIndex + 2));
					if(o != null){
						formula.remove(sumIndex + 1);
						formula.remove(sumIndex + 1);
						formula.add(sumIndex + 1, o);
					}
				}

				if(sumIndex > 1){
					Float o = applyNeg(formula.get(sumIndex - 2), formula.get(sumIndex - 1));
					if(o != null){
						sumIndex -= 1;
						formula.remove(sumIndex - 1);
						formula.remove(sumIndex - 1);
						formula.add(sumIndex - 1, o);
					}
				}

				float prev = (float) formula.get(sumIndex - 1);
				float next = (float) formula.get(sumIndex + 1);
				formula.remove(sumIndex - 1);
				formula.remove(sumIndex - 1);
				formula.remove(sumIndex - 1);
				formula.add(sumIndex - 1, prev + next);
				continue;
			}

			int subIndex = formula.indexOf('-');
			if(subIndex != -1){
				if(subIndex == 0){
					formula.remove(0);
					Object o = formula.get(0);
					formula.remove(0);
					formula.add(0, -((float) o));
					continue;
				}


				//Apply the negative sign(s) if they exist
				if(formula.size() - subIndex > 2){
					Float o = applyNeg(formula.get(subIndex + 1), formula.get(subIndex + 2));
					if(o != null){
						formula.remove(subIndex + 1);
						formula.remove(subIndex + 1);
						formula.add(subIndex + 1, o);
					}
				}

				if(subIndex > 1){
					Float o = applyNeg(formula.get(subIndex - 2), formula.get(subIndex - 1));
					if(o != null){
						subIndex -= 1;
						formula.remove(subIndex - 1);
						formula.remove(subIndex - 1);
						formula.add(subIndex - 1, o);
					}
				}

				float prev = (float) formula.get(subIndex - 1);
				float next = (float) formula.get(subIndex + 1);
				formula.remove(subIndex - 1);
				formula.remove(subIndex - 1);
				formula.remove(subIndex - 1);
				formula.add(subIndex - 1, prev - next);
//				continue;
			}
		}

		return (float) formula.get(0);
	}

	/**
	 * If the first term is a -, returns the negative of the second term. Else returns null
	 * @param item1 The first term
	 * @param item2 The second term
	 * @return The negative of the second term if the first term is -, else null
	 */
	private static Float applyNeg(Object item1, Object item2){
		if(item1 instanceof Character && (char) item1 == '-'){
			return -1F * (float) item2;
		}else{
			return null;
		}
	}
}
