# Generates a model and blockstate file using the normal templates for circuits for each name in the list below

toGen = ["cons", "not", "interface", "and", "or", "xor", "min", "max", "sum", "dif", "prod", "quot", "pow", "inv", "sin", "cos", "tan", "asin", "acos", "atan", "equals", "less", "more", "round", "ceil", "floor", "log", "reader"]

modelPath = "../../../../../resources/assets/essentials/models/block/circuit/"
statePath = "../../../../../resources/assets/essentials/blockstates/"

state_template = "{\n\t\"variants\": {\n\t\t\"horiz_facing=north\": { \"model\": \"essentials:block/circuit/{TYPE}\" },\n\t\t\"horiz_facing=south\": { \"model\": \"essentials:block/circuit/{TYPE}\", \"y\": 180 },\n\t\t\"horiz_facing=east\": { \"model\": \"essentials:block/circuit/{TYPE}\", \"y\": 90 },\n\t\t\"horiz_facing=west\": { \"model\": \"essentials:block/circuit/{TYPE}\", \"y\": 270 }\n\t}\n}"

for name in toGen:
	#Generate the model
	filepath = modelPath + name + ".json"
	with open(filepath, "w+") as f:
		f.write("{\n\t\"parent\": \"essentials:block/circuit/circuit\",\n\t\"textures\": {\n\t\t\"top\": \"essentials:block/circuit/" + name + "\"\n\t}\n}")
		f.close()

	# Generate the blockstate
	with open(statePath + name + "_circuit.json", "w+") as f:
		f.write(state_template.replace("{TYPE}", name))
		f.close()
