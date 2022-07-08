## Introduction

This program implements a TCP-like reliable data transfer (RDT) protocol. RDT functionalities are implemented in both the sender and the receiver. In order to improve performance, the sender is desinged to be multi-threaded, which a send, a receive and a packet watching threads. 

The network emulator is provided to serve as a unrealiable channel, and the packet class is also provided. The emulator and the packet class are not implemented by the author.

The timeout for sender is set to 200 and chunk size for each packet is set to 500 bytes, feel free to change these parameters if needed.

The debug mode prints debug info to the console during execution. It is set to False by default.

## Program Execution

Emulator, sender and reciever programs are expected to run on different hosts. The code are tested on "ubuntu2004-004.student.cs.uwaterloo.ca", "ubuntu2004-008.student.cs.uwaterloo.ca" and "ubuntu2004-010.student.cs.uwaterloo.ca". 

Before testing the program, creata a log folder:

```
$ mkdir logs
```
When testing the program, first run the emulator:
```
$ python3 network_emulator.py <sender_port_send> <receiver_addr> <receiver_port_recv> <receiver_port_send> <sender_addr> <sender_port_recv> <max_delay> <drop_prob> <verbose>
```
Then run the receiver:
```
$ python3 receiver.py <emulator_addr> <receiver_port_send> <receiver_port_recv> <file_name_write>
```
Finally, run the sender:
```
$ python3 sender.py <emulator_addr> <sender_port_send> <sender_port_recv> <file_name_read>
```
And example execution command is provided below:
```
$ python3 network_emulator.py 9330 ubuntu2004-008.student.cs.uwaterloo.ca 9331 9332 ubuntu2004-004.student.cs.uwaterloo.ca 9333 20 0.2 1
$ python3 receiver.py ubuntu2004-010.student.cs.uwaterloo.ca 9332 9331 recv.txt
$ python3 sender.py ubuntu2004-010.student.cs.uwaterloo.ca 9330 9333 test.txt
```




