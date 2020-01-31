# Generates a model and blockstate file using the normal templates for circuits for each name in the list below

toGen = ["or", "min", "max", "sum", "dif", "prod", "quot", "pow", "inv", "sin", "cos", "tan", "asin", "acos", "atan", "equals", "less", "more", "round", "ceil", "floor", "log", "reader"]

modelPath = "../../../../../resources/assets/essentials/models/block/circuit/"
statePath = "../../../../../resources/assets/essentials/blockstates/"

for name in toGen:
	filepath = modelPath + name + ".json"
	with open(filepath, "w+") as f:
		f.write("{\n\t\"parent\": \"essentials:block/circuit/circuit\",\n\t\"textures\": {\n\t\t\"top\": \"essentials:block/circuit/" + name + "\"\n\t}\n}")
		f.close()
	with open(statePath + name + "_circuit.json", "w+") as f:
		f.write("{\n\t\"forge_marker\": 1,\n\t\"defaults\": {\n\t\t\"model\": \"essentials:block/circuit/" + name + "\",\n\t\t\"uvlock\": false\n\t},\n\t\"variants\": {\n\t\t\"horiz_facing\": {\n\t\t\t\"north\": {\n\n\t\t\t},\n\t\t\t\"south\": {\n\t\t\t\t\"y\": 180\n\t\t\t},\n\t\t\t\"east\": {\n\t\t\t\t\"y\": 90\n\t\t\t},\n\t\t\t\"west\": {\n\t\t\t\t\"y\": 270\n\t\t\t}\n\t\t}\n\t}\n}")
		f.close()
