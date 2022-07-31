# !/usr/bin/env bash

# sets bridge R1 to use OpenFlow 1.3
ovs-vsctl set bridge R1 protocols=OpenFlow13
# sets bridge R2 to use OpenFlow 1.3
ovs-vsctl set bridge R2 protocols=OpenFlow13
# sets bridge S1 to use OpenFlow 1.3
ovs-vsctl set bridge S1 protocols=OpenFlow13
# sets bridge S2 to use OpenFlow 1.3
ovs-vsctl set bridge S2 protocols=OpenFlow13
# sets bridge S3 to use OpenFlow 1.3
ovs-vsctl set bridge S3 protocols=OpenFlow13

# print protocol each switch supports
for switch in R1 R2 S1 S2 S3;
do
    protos=$(ovs-vsctl get bridge $switch protocols)
    echo "Switch $switch supports $protos"
done

# Avoid having to write "-O OpenFlow13" before all of your ovs-ofctl commands.
ofctl='ovs-ofctl -O OpenFlow13'

# Alice - Bob
$ofctl add-flow S1 \
    in_port=1,actions=mod_dl_src:00:0A:00:00:00:02,mod_dl_dst:0A:00:00:00:00:01,output=2
$ofctl add-flow R1 \
    in_port=1,ip,nw_src=10.1.1.17,nw_dst=10.4.4.48,actions=mod_dl_src:0A:00:00:00:00:02,mod_dl_dst:00:0B:00:00:00:03,output=2
$ofctl add-flow S2 \
    in_port=3,actions=mod_dl_src:00:0B:00:00:00:01,mod_dl_dst:B0:B0:B0:B0:B0:B0,output=1

# Bob - Alice
$ofctl add-flow S2 \
    in_port=1,actions=mod_dl_src:00:0B:00:00:00:03,mod_dl_dst:0A:00:00:00:00:02,output=3
$ofctl add-flow R1 \
    in_port=2,ip,nw_src=10.4.4.48,nw_dst=10.1.1.17,actions=mod_dl_src:0A:00:00:00:00:01,mod_dl_dst:00:0A:00:00:00:02,output=1
$ofctl add-flow S1 \
    in_port=2,actions=mod_dl_src:00:0A:00:00:00:01,mod_dl_dst:AA:AA:AA:AA:AA:AA,output=1

# David - Carol
$ofctl add-flow S2 \
    in_port=2,actions=mod_dl_src:00:0B:00:00:00:04,mod_dl_dst:0B:00:00:00:00:01,output=4
$ofctl add-flow R2 \
    in_port=1,ip,nw_src=10.4.4.96,nw_dst=10.6.6.69,actions=mod_dl_src:0B:00:00:00:00:02,mod_dl_dst:00:0C:00:00:00:02,output=2
$ofctl add-flow S3 \
    in_port=2,actions=mod_dl_src:00:0C:00:00:00:01,mod_dl_dst:CC:CC:CC:CC:CC:CC,output=1

# Carol - David
$ofctl add-flow S3 \
    in_port=1,actions=mod_dl_src:00:0C:00:00:00:02,mod_dl_dst:0B:00:00:00:00:02,output=2
$ofctl add-flow R2 \
    in_port=2,ip,nw_src=10.6.6.69,nw_dst=10.4.4.96,actions=mod_dl_src:0B:00:00:00:00:01,mod_dl_dst:00:0B:00:00:00:04,output=1
$ofctl add-flow S2 \
    in_port=4,actions=mod_dl_src:00:0B:00:00:00:02,mod_dl_dst:D0:D0:D0:D0:D0:D0,output=2

# Print the flows installed in each switch
for switch in R1 R2 S1 S2 S3;
do
    echo "Flows installed in $switch:"
    $ofctl dump-flows $switch
    echo ""
done