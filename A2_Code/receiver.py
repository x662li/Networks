import argparse
from packet import Packet
from socket import *
import threading
import logging

class Receiver:
    
    global ACK
    global DAT
    global EOT
    ACK = 0
    DAT = 1
    EOT = 2
    
    """ 
        Receiver constructor
    """
    def __init__(self, recv_port, emu_sock, debug=False):
        self.emu_sock = emu_sock # emulator socket for receiver (address, port number)
        self.recv_port = recv_port # receiver port to receive packet
        self.recv_socket = socket(AF_INET, SOCK_DGRAM) # receiver socket
        self.recv_socket.bind(("", self.recv_port)) # bind receiver socket to recv_port
        self.expect_seqnum = 0 # next seqnum expected from sender
        self.pkt_buff = [] # packet buffer
        self.debug = debug # debug mode, print messages if True
        self.logger = self.config_logger('recv_log', './logs/arrival.log') # arrival logger
    
    """ 
        increment expectd seqnum
    """
    def inc_exp_seq(self):
        self.expect_seqnum = (self.expect_seqnum + 1) % 32
    
    """ 
        get seqnum prior to expect_seqnum
    """
    def prev_seq(self):
        if self.expect_seqnum == 0:
            return 31
        else:
            return self.expect_seqnum - 1
    
    """ 
        send packet to emulator
    """
    def send_pkt(self, pkt):
        self.recv_socket.sendto(pkt.encode(), self.emu_sock)
    
    """ 
        receive packet from recv_socket
    """
    def recv_pkt(self, max_size):
        return self.recv_socket.recv(max_size)

    """ 
        create packet
    """
    def make_pkt(self, msg_type, seq_num, length, data):
        return Packet(msg_type, seq_num, length, data)
    
    """ 
        write data to file, (if file not exist, create one)
    """
    def write_file(self, file_name, data):
        with open(file_name, 'a+') as f:
            f.write(data)
            
    """ 
        send ack packet with seqnum
    """
    def ack_send(self, seqnum):
        ack = self.make_pkt(ACK, seqnum, 0, "")
        self.send_pkt(ack)
    
    """ 
        process incoming packet with rdt mechanism
    """
    def rdt_process(self, pkt, file_name):
        typ, seqnum, length, data = pkt.decode()
        if self.debug: print("[RDT] received seq num: " + str(seqnum))
        # if received seqnum is expected
        if seqnum == self.expect_seqnum:
            # if EOT received, send EOT back and terminate
            if typ == EOT:
                self.logger.info('EOT')
                if self.debug: print("[RDT] EOT received, send EOT back and terminate")
                pkt = self.make_pkt(EOT, self.prev_seq(), 0, '')
                self.send_pkt(pkt)
                return False
            else:
                # log to arrival.log, write file and increase expected seqnum
                self.logger.info(str(seqnum))
                self.write_file(file_name, data)
                if self.debug: print("[RDT] write file, seq: " + str(seqnum) + ", content: " + str(data))
                self.inc_exp_seq()
                # if item in buffer, find all consecutive seqnums and write to file, send cummulative ack
                if len(self.pkt_buff) > 0:
                    while self.expect_seqnum in [p[1] for p in self.pkt_buff]:
                        buff_idx = [p[1] for p in self.pkt_buff].index(self.expect_seqnum)
                        # if iterate to and EOT in buffer, terminate program
                        if self.pkt_buff[buff_idx][0] == EOT:
                            if self.debug: print("[RDT] EOT received, send EOT back and terminate")
                            pkt = self.make_pkt(EOT, self.prev_seq(), 0, '')
                            self.send_pkt(pkt)
                            return False
                        else: # other wise, write file, pop item and iterate to next seqnum
                            self.write_file(file_name, self.pkt_buff[buff_idx][3])
                            if self.debug: print("[RDT] write file, seq: " + str(self.expect_seqnum) + ", content: " + str(self.pkt_buff[buff_idx][3]))
                            self.pkt_buff.pop(buff_idx)
                            if self.debug: print("[RDT] remove item in buffer, index: " + str(buff_idx))
                            self.inc_exp_seq()
                # send ack with previously written packet seqnum
                self.ack_send(self.prev_seq())
                if self.debug: print("[RDT] send ACK, seq: " + str(self.prev_seq()))
                return True;
        else:
            # of seqnum not expected and within next 10 seqnum, store in buffer
            if ((seqnum > self.expect_seqnum)&(seqnum - self.expect_seqnum <= 10))|((seqnum < self.expect_seqnum)&(31- self.expect_seqnum + seqnum <= 10)):
                if (len(self.pkt_buff)==0)|((len(self.pkt_buff)>0)&(seqnum not in [p[1] for p in self.pkt_buff])):
                    self.pkt_buff.append((typ, seqnum, length, data))
                else:
                    # if already stored, ignore
                    if self.debug: print("[RDT] ignore duplicate pkt, seqnum: " + str(seqnum))
            else:
                # if seqnum not within next 10, ignore
                if self.debug: print("[RDT] pkt seq exceeds next 10; recv seqnum: " + str(seqnum) + " expected: " + str(self.expect_seqnum))
            # send ack with prev seqnum to expected one
            self.ack_send(self.prev_seq())
            if self.debug: print("[RDT] send ACK, seq: " + str(self.prev_seq()))
            return True
    
    """ 
        create logger
    """
    def config_logger(self, logger_name, log_file, level=logging.INFO):
        hander = logging.FileHandler(log_file)
        formatter = logging.Formatter('%(message)s')
        hander.setFormatter(formatter)
        logger = logging.getLogger(logger_name)
        logger.setLevel(level)
        logger.addHandler(hander)
        return logger                
            
if __name__ == '__main__':
    
    parser = argparse.ArgumentParser()
    parser.add_argument("<emu_addr>")
    parser.add_argument("<emu_port>")
    parser.add_argument("<recv_port>")
    parser.add_argument("<file_name>")
    args = parser.parse_args()
    
    args = args.__dict__
    emu_sock = (str(args["<emu_addr>"]), int(args["<emu_port>"]))
    recv_port = int(args["<recv_port>"])
    file_name = str(args["<file_name>"])
    
    receiver = Receiver(recv_port=recv_port, emu_sock=emu_sock, debug=True)
    while True:
        if receiver.debug: print("[MAIN] expected seq num: " + str(receiver.expect_seqnum))
        pkt = Packet(receiver.recv_pkt(1024))
        if not receiver.rdt_process(pkt, file_name):
            break    
        if receiver.debug: print('-------------------------')
    
        

        
        
        
        
        
        
    
    
    
    
    