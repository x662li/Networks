from mininet.cli import CLI
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.link import TCLink
from mininet.log import setLogLevel

class CSLRTopo(Topo):
    
    def __init__(self):
        "Create Topology"
        
        Topo.__init__(self)
        
        # add hosts
        Alice = self.addHost("Alice")
        Bob = self.addHost("Bob")
        David = self.addHost("David")
        Carol = self.addHost("Carol")
        
        # add switches
        S1 = self.addSwitch('S1', listenPort=6634)
        S2 = self.addSwitch('S2', listenPort=6635)
        S3 = self.addSwitch('S3', listenPort=6636)
        R1 = self.addSwitch('R1', listenPort=6637)
        R2 = self.addSwitch('R2', listenPort=6638)
         
        # link between hosts and switches
        self.addLink(Alice, S1) # eth0 - eth1
        self.addLink(Bob, S2) # eth0 - eth1
        self.addLink(David, S2) # etho0 - eth4
        self.addLink(Carol, S3) # eth0 - eth1
        
        # link between switches
        self.addLink(S1, R1, bw=100) # eth2 - eth1
        self.addLink(R1, S2, bw=100) # eth2 - eth2
        self.addLink(S2, R2, bw=100) # eth3 - eth1
        self.addLink(R2, S3, bw=100) # eth2 - eth2
        
def run():
    # create network
    topo = CSLRTopo()
    net = Mininet(topo=topo, link=TCLink, controller=None)
    
    # set IP, MAC for hosts
    Alice = net.get('Alice')
    Alice.intf("Alice-eth0").setIP("10.1.1.17", 24)
    Alice.intf("Alice-eth0").setMAC("AA:AA:AA:AA:AA:AA")
    
    Bob = net.get('Bob')
    Bob.intf("Bob-eth0").setIP("10.4.4.48", 24)
    Bob.intf("Bob-eth0").setMAC("B0:B0:B0:B0:B0:B0")
    
    David = net.get('David')
    David.intf("David-eth0").setIP("10.4.4.96", 24)
    David.intf("David-eth0").setMAC("D0:D0:D0:D0:D0:D0")
    
    Carol = net.get('Carol')
    Carol.intf("Carol-eth0").setIP("10.6.6.69", 24)
    Carol.intf("Carol-eth0").setMAC("CC:CC:CC:CC:CC:CC")
    
    # set IP, MAC for Routers
    R1 = net.get('R1')
    R1.intf("R1-eth1").setMAC("0A:00:00:00:00:01")
    R1.intf("R1-eth2").setMAC("0A:00:00:00:00:02")
    
    R2 = net.get('R2')
    R2.intf("R2-eth1").setMAC("0B:00:00:00:00:01")
    R2.intf("R2-eth2").setMAC("0B:00:00:00:00:02")
    
    # set MAC for Routers
    S1 = net.get('S1')
    S1.intf("S1-eth1").setMAC("00:0A:00:00:00:01")
    S1.intf("S1-eth2").setMAC("00:0A:00:00:00:02")
    
    S2 = net.get('S2')
    S2.intf("S2-eth1").setMAC("00:0B:00:00:00:01")
    S2.intf("S2-eth2").setMAC("00:0B:00:00:00:02")
    S2.intf("S2-eth3").setMAC("00:0B:00:00:00:03")
    S2.intf("S2-eth4").setMAC("00:0B:00:00:00:04")
    
    S3 = net.get('S3')
    S3.intf("S3-eth1").setMAC("00:0C:00:00:00:01")
    S3.intf("S3-eth2").setMAC("00:0C:00:00:00:02")
    
    net.start()
    
    # routing table entry
    Alice.cmd('route add default gw 10.1.1.14 dev Alice-eth0')
    Bob.cmd('route add default gw 10.4.4.14 dev Bob-eth0')
    David.cmd('route add default gw 10.4.4.28 dev David-eth0')
    Carol.cmd('route add default gw 10.6.6.46 dev Carol-eth0')
    
    # add arp cache
    Alice.cmd('arp -s 10.1.1.14 00:0A:00:00:00:01 -i Alice-eth0')
    Bob.cmd('arp -s 10.4.4.14 00:0B:00:00:00:01 -i Bob-eth0')
    David.cmd('arp -s 10.4.4.28 00:0B:00:00:00:04 -i David-eth0')
    Carol.cmd('arp -s 10.6.6.46 00:0C:00:00:00:01 -i Carol-eth0')
    
    # open line interface
    CLI(net)

    # tear down and cleanup
    net.stop()
        
if __name__ == '__main__':
    setLogLevel('info')
    run()
            
            
            
            
            
            
            
        