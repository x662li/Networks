
# !/usr/bin/env bash

# sets bridge s0 to use OpenFlow 1.3
ovs-vsctl set bridge s0 protocols=OpenFlow13
# sets bridge s1 to use OpenFlow 1.3
ovs-vsctl set bridge s1 protocols=OpenFlow13
# sets bridge s2 to use OpenFlow 1.3
ovs-vsctl set bridge s2 protocols=OpenFlow13
# sets bridge s3 to use OpenFlow 1.3
ovs-vsctl set bridge s3 protocols=OpenFlow13
# sets bridge s4 to use OpenFlow 1.3
ovs-vsctl set bridge s4 protocols=OpenFlow13
# sets bridge s6 to use OpenFlow 1.3
ovs-vsctl set bridge s6 protocols=OpenFlow13

# print protocol each switch supports
for switch in s0 s1 s2 s3 s4 s6;
do 
    protos=$(ovs-vsctl get bridge $switch protocols)
    echo "switch $switch supports $protos"
done

# abbr for command
ofctl = 'ovs-ofctl -O OpenFlow13'

# h0 -> h2
$ofctl add-flow s0 \
    in_port=1,ip,nw_src=10.0.0.2,nw_dst=10.0.2.2,actions=mod_dl_src:0A:00:0B:01:00:03,mod_dl_dst:0A:00:0B:FE:00:02,output=3
$ofctl add-flow s2 \
    in_port=2,ip,nw_src=10.0.0.2,nw_dst=10.0.2.2,actions=mod_dl_src:0A:00:02:01:00:01,mod_dl_dst:0A:00:02:02:00:00,output=1
# h2 -> h0
$ofctl add-flow s2 \
    in_port=1,ip,nw_src=10.0.2.2,nw_dst=10.0.0.2,actions=mod_dl_src:0A:00:0B:FE:00:02,mod_dl_dst:0A:00:0B:01:00:03,output=2
$ofctl add-flow s0 \
    in_port=3,ip,nw_src=10.0.2.2,nw_dst=10.0.0.2,actions=mod_dl_src:0A:00:00:01:00:01,mod_dl_dst:0A:00:00:02:00:00,output=1

# h1 -> h4
$ofctl add-flow s1 \
    in_port=1,ip,nw_src=10.0.1.2,nw_dst=10.0.4.2,actions=mod_dl_src:0A:00:0C:01:00:03,mod_dl_dst:0A:00:0D:01:00:03,output=3
$offctl add-flow s2 \
    in_port=3,ip,nw_src=10.0.1.2,nw_dst=10.0.4.2,actions=mod_dl_src:0A:00:0C:FE:00:04,mod_dl_dst:0A:00:0D:FE:00:02,output=4
$ofctl add-flow s3 \
    in_port=2,ip,nw_src=10.0.1.2,nw_dst=10.0.4.2,actions=mod_dl_src:0A:00:0E:01:00:03,mod_dl_dst:0A:00:0E:FE:00:02,output=3
$ofctl add-flow s4 \
    in_port=2,ip,nw_src=10.0.1.2,nw_dst=10.0.4.2,actions=mod_dl_src:0A:00:04:01:00:01,mod_dl_dst:0A:00:04:02:00:00,output=1
# h4 -> h1
$ofctl add-flow s4 \
    in_port=1,ip,nw_src=10.0.4.2,nw_dst=10.0.1.2,actions=mod_dl_src:0A:00:0E:FE:00:02,mod_dl_dst:0A:00:0E:01:00:03,output=2
$ofctl add-flow s3 \
    in_port=3,ip,nw_src=10.0.4.2,nw_dst=10.0.1.2,actions=mod_dl_src:0A:00:0D:FE:00:02,mod_dl_dst:0A:00:0C:FE:00:04,output=2
$ofctl add-flow s2 \
    in_port=4,ip,nw_src=10.0.4.2,nw_dst=10.0.1.2,actions=mod_dl_src:0A:00:0D:01:00:03,mod_dl_dst:0A:00:0C:01:00:03,output=3
$ofctl add-flow s1 \
    in_port=3,ip,nw_src=10.0.4.2,nw_dst=10.0.1.2,actions=mod_dl_src:0A:00:01:01:00:01,mod_dl_dst:0A:00:01:02:00:00,output=1

# h3 -> h6
$offctl add-flow s3 \
    in_port=1,ip,nw_src=10.0.3.2,nw_dst=10.0.6.2,actions=mod_dl_src:0A:00:0F:01:00:04,mod_dl_dst:0A:00:0F:FE:00:02,output=4
$offctl add-flow s6 \
    in_port=2,ip,nw_src=10.0.3.2,nw_dst=10.0.6.2,actions=mod_dl_src:0A:00:06:01:00:01,mod_dl_dst:0A:00:06:02:00:00,output=1
# h6 -> h3
$offctl add-flow s6 \
    in_port=1,ip,nw_src=10.0.6.2,nw_dst=10.0.3.2,actions=mod_dl_src:0A:00:0F:FE:00:02,mod_dl_dst:0A:00:0F:01:00:04,output=2
$offctl add-flow s3 \
    in_port=4,ip,nw_src=10.0.6.2,nw_dst=10.0.3.2,actions=mod_dl_src:0A:00:03:01:00:01,mod_dl_dst:0A:00:03:02:00:00,output=1







