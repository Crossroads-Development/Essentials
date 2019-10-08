# Generates a model file using the normal template for circuits for each name in the list below

toGen = ["or", "min", "max", "sum", "dif", "prod", "quot", "pow", "inv", "sin", "cos", "tan", "asin", "acos", "atan", "reader"]

modelPath = "../../../../../resources/assets/essentials/models/block/circuit/"

for name in toGen:
	filepath = modelPath + name + ".json"
	with open(filepath, "w+") as f:
		f.write("{\n\t\"parent\": \"essentials:block/circuit/circuit\",\n\t\"textures\": {\n\t\t\"top\": \"essentials:block/circuit/" + name + "\"\n\t}\n}")
		f.close()
