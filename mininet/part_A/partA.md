### OVS rules for switch s0

```
ofctl add-flow s0
```
Add openflow control rules to switch s0.

```
in_port=1, ip, nw_src=10.0.0.2, nw_dst=10.0.1.2, actions=mod_dl_src:0A:00:0A:01:00:02,mod_dl_dst:0A:00:0A:FE:00:02,output=2
```
For s0, match datagram from s0-eth1 port, with source ip address 10.0.0.2 (h0) and destination ip address 10.0.1.2 (h1);
Change its source MAC address to 0A:00:0A:01:00:02 (s0-eth2) and its destination MAC address to 0A:00:0A:FE:00:02 (s1-eth2), and send through port is s0-eth2.


```
in_port=2, ip, nw_src=10.0.1.2, nw_dst=10.0.0.2, actions=mod_dl_src:0A:00:0A:01:00:01,mod_dl_dst:0A:00:00:02:00:00,output=1
```
For s0, match datagram from s0-eth2 port, with source ip address 10.0.1.2 (h1) and destination ip address 10.0.0.2 (h0);
Change its source MAC address to 0A:00:0A:01:00:01 (s0-eth1) and its destination MAC address to 0A:00:00:02:00:00 (h0-eth0), and send through port is s0-eth1.


### OVS rules for switch s1

```
ofctl add-flow s1
```
Add openflow control rules to switch s1.

```
in_port=2, ip, nw_src=10.0.0.2, nw_dst=10.0.1.2, actions=mod_dl_src:0A:00:01:01:00:01,mod_dl_dst:0A:00:01:02:00:00,output=1
```
For s1, match datagram from s1-eth2 port, with source ip address 10.0.0.2 (h0) and destination ip address 10.0.1.2 (h1);
Change its source MAC address to 0A:00:01:01:00:01 (s1-eth1) and its destination MAC address to 0A:00:01:02:00:00 (h1-eth0), and send through port is s1-eth1.

```
inport=1, ip, nw_src=10.0.1.2, nw_dst=10.0.0.2, actions=mod_dl_src:0A:00:0A:FE:00:02,mod_dl_dst:0A:00:0A:01:00:02,output=2
```
For s1, match datagram from s1-eth1 port, with source ip address 10.0.1.2 (h1) and destination ip address 10.0.0.2 (h0);
Change its source MAC address to 0A:00:0A:FE:00:02 (s1-eth2) and its destination MAC address to 0A:00:0A:01:00:02 (s0-eth2), and send through port is s1-eth2.