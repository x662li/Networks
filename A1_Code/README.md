## Introduction

This socket program contains two packages: server and client, which can be deployed in two separate machines. each package contains a utility class and a entrance class named as their package names.

## Makefile

A makefile is provided for compling the project, start by entering
```
$ make clear
```
in the commandline to delete all the .class file pre-exist, then enter
```
$ make
```
to compile the 4 files. 

## Program Execution

Two shell script files are provided, The server program is tested on both "ubuntu2004-002.student.cs.uwaterloo.ca" and "ubuntu2004-004.student.cs.uwaterloo.ca ". 

First, start server program by entering
```
$ sh server.sh <req_code>
```
in the commandline, this will start the server and pass a require code for authentication. The server will output a negotiation port (server port). 

Then on another machine (or the same machine) enter
```
$ sh client.sh <server_address> <n_port> <req_code> <msg1> <msg2> ... 
```
to start the client. where "server_address" is the address which server is running and "n_port" should be the port number output from server. An example input is shown below:
```
$ sh client.sh ubuntu2004-004.student.cs.uwaterloo.ca 1205 42 "this is a test" "EXIT"
```
An exit code "EXIT" should always be provided at the end to indicate termination. Then the server will be waiting on "n_port" for another client.

