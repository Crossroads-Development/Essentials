package com.Da_Technomancer.essentials.api.redstone;

import com.Da_Technomancer.essentials.ESConfig;
import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.api.BlockUtil;
import com.Da_Technomancer.essentials.items.CircuitWrench;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class RedstoneUtil extends BlockUtil{

	public static final Capability<IRedstoneHandler> REDSTONE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

	/**
	 * Allows other mods to support being read by a reader circuit without a hard dependency on Essentials
	 *
	 * Public for read-only; Modify using registerReadable()
	 */
	public static final Map<ResourceLocation, IReadable> READABLES = new HashMap<>();

	/**
	 * Maximum value that circuits should be able to transfer- signal strengths above this should be capped to this value
	 */
	public static final float MAX_POWER = 1_000_000_000;
	/**
	 * Minimum value that circuits should be able to transfer- signal strengths below this should be capped to this value
	 */
	public static final float MIN_POWER = -MAX_POWER;
	//In game ticks
	public static final int DELAY = 2;

	public static void registerReadable(Block block, IReadable readable){
		ResourceLocation blockRegName = ForgeRegistries.BLOCKS.getKey(block);
		if(!READABLES.containsKey(blockRegName) && !(block instanceof IReadable)){
			READABLES.put(blockRegName, readable);
		}else{
			Essentials.logger.warn("Redundant readable handler registration: " + blockRegName);
		}
	}

	/**
	 * This is a public method addons can use to add their circuits to the CircuitWrench
	 * @param toRegister The circuit to be registered
	 * @param icon A path to a valid square icon to represent the circuit. If null, uses the generic missing texture
	 */
	public static void registerCircuit(@Nonnull IWireConnect toRegister, @Nullable ResourceLocation icon){
		if(!CircuitWrench.MODES.contains(toRegister)){
			CircuitWrench.MODES.add(toRegister);
			CircuitWrench.ICONS.add(icon);
		}else{
			Essentials.logger.warn("Redundant circuit registration: " + ForgeRegistries.BLOCKS.getKey(toRegister.wireAsBlock()));
		}
	}

	@Nullable
	public static IReadable getReadable(Block block){
		if(block instanceof IReadable){
			return ((IReadable) block);
		}else{
			return READABLES.get(ForgeRegistries.BLOCKS.getKey(block)); //Return an IReadable handler from the registry instead.
		}
	}

	/**
	 * Get the maximum range Essentials redstone signals can travel
	 * @return The maximum range, from the config
	 */
	public static int getMaxRange(){
		return ESConfig.maxRedstoneRange.get();
	}

	/**
	 * Sanitizes a redstone value
	 * @param input The value to sanitize
	 * @return The sanitized value
	 */
	public static float sanitize(float input){
		if(Float.isNaN(input)){
			//NaN check
			return 0;
		}
		if(input >= MAX_POWER){
			return MAX_POWER;
		}
		if(input <= MIN_POWER){
			return MIN_POWER;
		}
		return input;
	}

	/**
	 * Clamps a redstone signal strength to the vanilla range
	 * @param input The original strength
	 * @return The vanilla redstone strength to emit
	 */
	public static int clampToVanilla(float input){
		input = sanitize(input);
		if(input > 15){
			return 15;
		}else if(input < 0){
			return 0;
		}
		return Math.round(input);
	}

	/**
	 * Whether the value effectively changed
	 * Keep in mind that with Java floating point numbers, just because the value changed 8 decimal points down doesn't mean we should consider this changed
	 * @param prevVal The old value
	 * @param newVal The new value
	 * @return Whether the value should be considered "changed"
	 */
	public static boolean didChange(float prevVal, float newVal){
		//If the value changes sign/between zero and non-zero, or if the value changed by more than 0.005%, consider this changed
		return Math.signum(newVal) != Math.signum(prevVal) || newVal != 0 && Math.abs(newVal - prevVal) / Math.abs(newVal) > 0.00_005;
	}

	/**
	 * @param w The world
	 * @param pos The position of the block receiving the signal
	 * @param dir The side of the blocks the redstone signal is coming in
	 * @return The strength of the redstone signal a blocks is receiving on a given side
	 */
	public static int getRedstoneOnSide(Level w, BlockPos pos, Direction dir){
		BlockPos offsetPos = pos.relative(dir);
		BlockState state = w.getBlockState(offsetPos);
		return Math.min(15, state.getBlock() == Blocks.REDSTONE_WIRE ? state.getValue(RedStoneWireBlock.POWER) : w.getSignal(offsetPos, dir));
	}

	/**
	 * Return the highest redstone strength at a block position
	 * @param w The world
	 * @param pos The position to test
	 * @return The redstone strength at the passed position
	 */
	public static int getRedstoneAtPos(Level w, BlockPos pos){
		int val = 0;
		for(Direction dir : Direction.values()){
			val = Math.max(val, getRedstoneOnSide(w, pos, dir));
		}
		return val;
	}

	/**
	 * Evaluates an expression string and returns the value
	 * Currently supports: numbers, pi (uppercase and lowercase), e, E (treated as 10^), ^, *, x & X, /, (, ), +, -
	 * The returned value is guaranteed to be finite and not NaN, but is not sanitized beyond this (result may be negative or above MAX_POWER)
	 * @param input A user-entered string to be interpreted as an expression
	 * @return The value of the expression, or 0 if it was unable to interpret the expression
	 */
	public static float interpretFormulaString(String input){
		float output = 0;
		boolean working = true;

		//Parse the input string into a list of numbers and operations, stored in formula
		//Possibilities to handle: numbers, pi (upper and lowercase), e, E (treat as 10^), ^, x & X, *, /, (, ), +, -
		ArrayList<Object> formula = new ArrayList<>();
		final String opChars = "+-xX*/()^";
		int index = 0;
//		abort:
		while(index < input.length()){
			//Find if the current active index contains a number, and if so add it
			int subInd = index;
			while(subInd < input.length() && (Character.isDigit(input.charAt(subInd)) || input.charAt(subInd) == '.')){
				subInd++;
			}
			if(index < subInd){
				//Found a number
				try{
					//Add the entire number to the formula, and jump past it
					float val = Float.parseFloat(input.substring(index, subInd));
					formula.add(val);
					index = subInd;
				}catch(NumberFormatException e){
					working = false;
					break;
				}
			}else if(input.charAt(index) == 'e'){
				formula.add((float) Math.E);
				index++;
			}else if(input.length() > index + 1 && input.substring(index, index + 2).toLowerCase().equals("pi")){
				formula.add((float) Math.PI);
				index += 2;
			}else if(opChars.contains(input.substring(index, index + 1))){
				//Handle operations +-Xx/()^
				char c = input.charAt(index);
				formula.add(c == 'x' || c == 'X' ? '*' : input.charAt(index));
				index++;
			}else if(input.charAt(index) == 'E'){
				//Treat E as meaning x10^
				formula.add('*');
				formula.add(10F);
				formula.add('^');
				index++;
			}else if(input.charAt(index) == ' '){
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
						case '-'://Minus is a little unusual, in that it acts as both a bi-operation (a-b) and a unary operation (-a)
							numberFromLeft = true;
							reqNextNumber = true;
							break;
						default:
							//Should never happen, due to unsupported operations getting stripped by the filter
							Essentials.logger.warn("Invalid formula state! Report to mod author, and give them this: " + input);
							working = false;
							break abort;

					}
				}else if(o instanceof Float){
					numberFromLeft = true;
					numberFromRight = true;
				}else{
					//Should never happen
					Essentials.logger.warn("Invalid formula state! Report to mod author, and give them this: " + input);
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
				if(Float.isNaN(value)){
					output = 0;
				}else if(Float.isInfinite(value)){
					return Math.copySign(Float.MAX_VALUE, value);//Instead of returning an infinite value, return the largest finite value
				}else{
					output = value;
				}
			}catch(Exception e){
				Essentials.logger.warn("Error interpreting formula; Report to mod author");
				e.printStackTrace();
				Essentials.logger.warn("Full formula data: [" + input + "]");
				Essentials.logger.warn("Parsed formula: ");
				for(Object o : formula){
					Essentials.logger.warn("\tFormula item: " + o.toString());
				}
				output = 0;
			}
		}

		return output;
	}

	/**
	 * Evaluates a formula, divided into an list of individual terms (ex: -3, *, e, ^, (, pi, -, 2, ))
	 * @param formula A list of individual terms in the formula
	 * @return The evaluated value, or 0 if it was unable to evaluate the formula.
	 */
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

				formula.add(openParenIndex, operate(subForm));//Perform the formula inside parenthesis first, and replace the term in parenthesis with the calculated value
				continue;
			}

			//Exponents
			int expIndex = formula.indexOf('^');
			if(expIndex != -1){
				//Apply the negative sign(s) if they exist
				expIndex = checkForNeg(formula, expIndex);

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
				multIndex = checkForNeg(formula, multIndex);

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
				divIndex = checkForNeg(formula, divIndex);

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
				sumIndex = checkForNeg(formula, sumIndex);

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
				subIndex = checkForNeg(formula, subIndex);

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
	 * When run at the operator of a two-term operation (ex: 4/9), applies any negatives to both terms (ex: -, 4, /, 9 -> -4, /, 9)
	 * @param formula The list of all entries in the operation
	 * @param opIndex The index of the operator in the operation (ex: ^)
	 * @return The new index of the operator, if changed
	 */
	private static int checkForNeg(ArrayList<Object> formula, int opIndex){
		if(formula.size() - opIndex > 2){
			Float o = applyNeg(formula.get(opIndex + 1), formula.get(opIndex + 2));
			if(o != null){
				formula.remove(opIndex + 1);
				formula.remove(opIndex + 1);
				formula.add(opIndex + 1, o);
			}
		}

		if(opIndex > 1){
			Float o = applyNeg(formula.get(opIndex - 2), formula.get(opIndex - 1));
			if(o != null){
				opIndex -= 1;
				formula.remove(opIndex - 1);
				formula.remove(opIndex - 1);
				formula.add(opIndex - 1, o);
			}
		}
		return opIndex;
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

	/**
	 * Given two possible input values, chooses the input value to take as the 'true' input.
	 * chooseInput(a, b) = chooseInput(b, a)
	 * chooseInput(a, chooseInput(b, c)) = chooseInput(b, chooseInput(a, c))
	 * @param in0 The first possible input value
	 * @param in1 The second possible input value
	 * @return The value to use as the real input
	 */
	public static float chooseInput(float in0, float in1){
		//The highest magnitude input will be taken as the true input, with positive values winning ties
		if(Math.abs(in0) < Math.abs(in1) || in0 == -in1 && in1 > 0){
			return in1;
		}else{
			return in0;
		}
	}
}
