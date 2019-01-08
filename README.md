## Cloud computing Assignment

# Matrix calculation cloud application (Java)

includes:
* client
* server
* worker nodes

Client makes request for a matrix operation of a square matrix (n * n)
Matrix server returns a reciept code for 'Job' and allocates 'work' to each worker node.
Works with only static worker counts currently, not dynamically allocated. Server works 
using the Master-Slave architecture.

Matrix data splits are quite simple given the time constraints for this project, 
the server contains the class 'NodeMaster' which splits the work into rows/columns 
depending on the operation, previously more complex data splits were performed in a
localized multi-thread version of this app. 'NodeMaster' is a seperate thread to 
free up the server for further requests and/or worker data transmission.

The 'work' for each job is added to a job queue which is allocated to worker nodes
which are queried to assess their capacity for work. The worker also works asynchronously, 
leaving itself open to recieve further data - to prevent any blocking.
Upon completion of 'work' a worker node uses it's own async thread to return the work 
while begnning another stored segment of 'work'.

The server stores a list of MasterNode's working on each job and upon request will return the 
result of the calcultaion, thus ending the thread. Additional operations are available for the 
client to assess the progress of the job request.

*Workers used under existing Azure account of team member, therefore new cloud setup required to run program*
*However the program will run cleanly and fault free with large matrices and concurrent client requests.*

# Issues/Future fixes:
* Max load tolerance, small RAM machines used as worker nodes need to measure CPU usage and refuse work if maxed out.
* Cleaner querying code - The result of group work/ time constraints to submit a working copy
* Improved job allocatation algorithm
* Safe destruction of thread
* Improved data split for allocation (Will significantly speed up matrix multiplication)
* Dynamic start up of worker nodes to allow Cloud Elasticity


