# to run the server, a script must be used due to the required 
# linking of a matrix library used int he implementation

# first go to the /bin folder
cd bin

# next run the 'runServer.bat' file which takes the following format:
runServer.bat <mainport> <workercount>
e.g.
runServer.bat 1024 10
//the server should now be running with no output (disabled for high amounts of querying)

#now from a seperate cmd line return to the base folder for the MatrixServer program
#the java client should simply run from here using:
java MatrixClient <hostname> <port> <matrixSize>
e.g.
java MatrixClient localhost 1024 100
//you will see an output such as this
	Requesting size 100
	Time till conneciton return 110
	Result successful
