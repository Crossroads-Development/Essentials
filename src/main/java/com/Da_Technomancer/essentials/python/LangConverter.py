# Converts MC1.12 style lang files to MC1.13 style lang files, leaving the original at the top of the file
# which must be deleted manually once satisfied
# Currently configured for US english

with open('../../../../../resources/assets/essentials/lang/en_us.lang', 'r+') as f:
	lines = f.readlines()
	print(lines)

	prefixed = [line.replace("tile.", "blocks.", 1) if line.startswith("tile.") else line for line in lines]

	id = [line.replace(".", ".essentials.", 1) if line.startswith("blocks.") | line.startswith('item.') else line for line in prefixed]

	cut = [line.replace(".name", "", 1) for line in id]

	quoted = ["\"" + line.replace("=", "\": \"", 1).replace("\n", "\",\n", 1) for line in cut]

	print(lines)
	print(prefixed)
	print(id)
	print(cut)
	print(quoted)

	f.seek(0, 2)

	f.write("\n\n")
	for line in quoted:
		f.write(line)

	f.close()