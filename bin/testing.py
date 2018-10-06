#get testing details

import os
import sys

import subprocess



os.system("javac MatrixClient.java")
# shell commands executed in order
port = 1024
matrixSize = 500

isAsync = True


list = []
processId = 0
if isAsync:
    # async process
    for i in range(0,8):
        os.system("start java MatrixClient localhost " + str(port) + " " + str(matrixSize) + " " + str(processId))
        processId += 1
        # result = subprocess.check_output("start java MatrixClient localhost " + str(port) + " " + str(matrixSize) + " /K", shell=False)
        # list.append(result)
        #os.system("start java MatrixClient localhost " + str(port) + " " + str(matrixSize))
        #os.system("timeout /t 1")
        #matrixSize +=  1
else:
    #sync
    for i in range(0, 10000):
        os.system("java MatrixClient localhost " + str(port) + " " + str(matrixSize))
        #os.system("timeout /t 1")
    # matrixSize +=  1

print(list)


