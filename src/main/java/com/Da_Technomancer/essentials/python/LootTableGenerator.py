import os
# Generates a basic loot table entry for every essentials blocks with a defined blockstate file,
# where the blocks drops itself

blockstates = os.listdir("../../../../../resources/assets/essentials/blockstates/")

regNames = [os.path.basename(bstate) for bstate in blockstates]

for name in regNames:
	filepath = "../../../../../resources/data/essentials/loot_tables/blocks/" + name
	with open(filepath, "w+") as f:
		f.write("{\n\t\"type\": \"minecraft:blocks\",\n\t\"pools\": [\n\t\t{\n\t\t\t\"rolls\": 1,\n\t\t\t\"entries\": [\n\t\t\t\t{\n\t\t\t\t\t\"type\": \"minecraft:item\",\n\t\t\t\t\t\"name\": \"essentials:" + name.replace(".json", "", 1) + "\"\n\t\t\t\t}\n\t\t\t],\n\t\t\t\"conditions\": [\n\t\t\t\t{\n\t\t\t\t\t\"condition\": \"minecraft:survives_explosion\"\n\t\t\t\t}\n\t\t\t]\n\t\t}\n\t]\n}")
		f.close()
