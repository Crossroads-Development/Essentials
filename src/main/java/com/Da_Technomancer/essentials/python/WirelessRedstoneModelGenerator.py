# Makes all 32 redstone transmitter/receiver models
# In other news, forge blockstate files still can't overwrite textures in MC1.14

colors = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black"]

modelPath = "../../../../../resources/assets/essentials/models/block/wireless_redstone/"

for color in colors:
	filepath = modelPath + "redstone_receiver_" + color + ".json"
	with open(filepath, "w+") as f:
		f.write("{\n\t\"parent\": \"block/cube_all\",\n\t\"textures\": {\n\t\t\"all\": \"essentials:block/redstone_receiver_" + color + "\"\n\t}\n}")
		f.close()

	filepath = modelPath + "redstone_transmitter_" + color + ".json"
	with open(filepath, "w+") as f:
		f.write("{\n\t\"parent\": \"block/cube_all\",\n\t\"textures\": {\n\t\t\"all\": \"essentials:block/redstone_transmitter_" + color + "\"\n\t}\n}")
		f.close()
