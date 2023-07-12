import os

srcPath = "../docs/src/"
# for inDir, subDirList, fileList in os.walk(srcPath):
# 	print('---')
# 	print(inDir)
# 	print(subDirList)
# 	print(fileList)
#
#
print('****')
print([val.name for val in os.scandir(srcPath + 'en_us')])


# print(os.path.normpath('../A/B'))
# print(os.path.normpath('A/B/'))
# print(os.path.normpath('A/B//'))
# print(os.path.normpath('A//B'))