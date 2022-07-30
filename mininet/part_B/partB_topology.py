from mininet.cli import CLI
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.link import TCLink
from mininet.log import setLogLevel

class CSLRTopo(Topo):
    
    def __init__(self):
        Topo.__init__(self)
        
        # add hosts
        Alice = self.addHost("Alice")
        Bob = self.addHost("Bob")
        David = self.addHost("David")
        Carol = self.addHost("Carol")
        
        # add switches
        s1 = self.addSwitch('s1', listenPort=6634)
        s2 = self.addSwitch('s2', listenPort=6635)
        s3 = self.addSwitch('s3', listenPort=6636)
        R1 = self.addSwitch('R1', listenPort=6637)
        R2 = self.addSwitch('R2', listenPort=6638)
        
        