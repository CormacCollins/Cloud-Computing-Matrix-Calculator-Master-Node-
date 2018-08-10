

import sys
from shell_command import shell_call

# execute java server then multiple clients
client_count = 10
starting_matrx_size = 2

#compile
shell_call("javac MatrixServer.java")
shell_call("javac MatrixClient.java")

#start server
shell_call("java MatrixServer")
for i in range(0, 10):
    shell_call("java MatrixClient " + starting_matrx_size)
    starting_matrx_size += 1

