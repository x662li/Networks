import argparse
from concurrent.futures import thread
from packet import Packet
from socket import *
import threading

class Receiver:
    
    global ACK
    global DAT
    global EOT
    ACK = 0
    DAT = 1
    EOT = 2
    
    def __init__(self, recv_port, emu_sock, debug=False):
        self.emu_sock = emu_sock
        self.recv_port = recv_port
        self.recv_socket = socket(AF_INET, SOCK_DGRAM)
        self.recv_socket.bind(("", self.recv_port))
        self.expect_seqnum = 0
        self.pkt_buff = [] 
        self.debug = debug
        
    def inc_exp_seq(self):
        self.expect_seqnum = (self.expect_seqnum + 1) % 32
    
    def prev_seq(self):
        if self.expect_seqnum == 0:
            return 31
        else:
            return self.expect_seqnum - 1
        
    def send_pkt(self, pkt):
        self.recv_socket.sendto(pkt.encode(), self.emu_sock)
        
    def recv_pkt(self, max_size):
        return self.recv_socket.recv(max_size)

    def make_pkt(self, msg_type, seq_num, length, data):
        return Packet(msg_type, seq_num, length, data)
    
    def write_file(self, file_name, data):
        with open(file_name, 'a+') as f:
            f.write(data)
    
    def ack_send(self, seqnum):
        ack = self.make_pkt(ACK, seqnum, 0, "")
        self.send_pkt(ack)
            
    def rdt_process(self, pkt, file_name):
        typ, seqnum, length, data = pkt.decode()
        if self.debug: print("[RDT] received seq num: " + str(seqnum))
        if seqnum == self.expect_seqnum:
            if typ == EOT:
                if self.debug: print("[RDT] EOT received, send EOT back and terminate")
                pkt = self.make_pkt(EOT, self.prev_seq(), 0, '')
                self.send_pkt(pkt)
                return False
            else:
                self.write_file(file_name, data)
                if self.debug: print("[RDT] write file, seq: " + str(seqnum))
                self.inc_exp_seq()
                
                if len(self.pkt_buff) > 0:
                    while self.expect_seqnum in [p[1] for p in self.pkt_buff]:
                        buff_idx = [p[1] for p in self.pkt_buff].index(self.expect_seqnum)
                        if self.pkt_buff[buff_idx][0] == EOT:
                            if self.debug: print("[RDT] EOT received, send EOT back and terminate")
                            pkt = self.make_pkt(EOT, self.prev_seq(), 0, '')
                            self.send_pkt(pkt)
                            return False
                        else:
                            self.write_file(file_name, self.pkt_buff[buff_idx][3])
                            if self.debug: print("[RDT] write file, seq: " + str(self.expect_seqnum))
                            self.pkt_buff.pop(buff_idx)
                            if self.debug: print("[RDT] remove item in buffer, index: " + str(buff_idx))
                            self.inc_exp_seq()
                self.ack_send(self.prev_seq())
                if self.debug: print("[RDT] send ACK, seq: " + str(self.prev_seq()))
                return True;
        else:
            if ((seqnum > self.expect_seqnum)&(seqnum - self.expect_seqnum <= 10))|((seqnum < self.expect_seqnum)&(31- self.expect_seqnum + seqnum <= 10)):
                if (len(self.pkt_buff)==0)|((len(self.pkt_buff)>0)&(seqnum not in [p[1] for p in self.pkt_buff])):
                    self.pkt_buff.append((typ, seqnum, length, data))
                else:
                    if self.debug: print("[RDT] ignore duplicate pkt, seqnum: " + str(seqnum))
            else:
                if self.debug: print("[RDT] pkt seq exceeds next 10; recv seqnum: " + str(seqnum) + " expected: " + str(self.expect_seqnum))
            self.ack_send(self.prev_seq())
            if self.debug: print("[RDT] send ACK, seq: " + str(self.prev_seq()))
            return True
                
            
if __name__ == '__main__':
    
    # parser = argparse.ArgumentParser()
    # parser.add_argument("<emu_addr>")
    # parser.add_argument("<emu_port>")
    # parser.add_argument("<recv_port>")
    # parser.add_argument("<file_name>")
    # args = parser.parse_args()
    
    # args = args.__dict__
    # emu_sock = (str(args["<emu_addr>"]), int(args["<emu_port>"]))
    # recv_port = int(args["<recv_port>"])
    # file_name = str(args["<file_name>"])
    
    emu_sock = (str('localhost'), int(8082))
    recv_port = int(8081)
    file_name = 'recv.txt'
    
    receiver = Receiver(recv_port=recv_port, emu_sock=emu_sock, debug=True)
    while True:
        if receiver.debug: print("[MAIN] expected seq num: " + str(receiver.expect_seqnum))
        pkt = Packet(receiver.recv_pkt(1024))
        if not receiver.rdt_process(pkt, file_name):
            break    
        if receiver.debug: print('-------------------------')
    
        

        
        
        
        
        
        
    
    
    
    
    