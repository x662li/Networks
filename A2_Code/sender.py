import argparse
from packet import Packet
from socket import *
import threading
import time

class Sender:
    
    global ACK
    global DAT
    global EOT
    ACK = 0
    DAT = 1
    EOT = 2
    
    def __init__(self, win_size, timeout, emu_sock, ack_port, debug=False):
        # parameters
        self.win_size = win_size # current window size
        self.timeout = timeout # timeout threshold
        self.emu_sock = emu_sock # emulator socket for sender (address, port number)
        self.ack_port = ack_port # sender port to receive ack
        # trackers
        self.send_seq = 0 # seq number of current packet to send
        self.recv_seq = 0 # seq number of oldest un-acked packet
        self.ack_count = 0 # duplicated ack received for seq num n (n+1 is the oldest un-acked packet)
        self.timer = {"start_time": 0, "started": False} # track time, record start time when started
        # buffers
        self.data_buffer = [] # loaded data from file, stored by chunks
        self.seq_wind = [] # [oldest un-acked pkt ---- newly sent pkt], format (seq num, pkt) 
        # locks
        self.winsize_lock = threading.Lock() # lock for window size
        self.seqwind_lock = threading.Lock() # mutex for window 
        
        # self.seqwind_empty = threading.Semaphore(self.win_size) # empty slots in window
        # self.empty_lock = threading.Lock() # empty semaphore lock for window size change
        
        self.seqwind_full = threading.Condition()
        self.seqwind_empty = threading.Condition()
        
        
        self.ackcount_lock = threading.Lock()
        self.timer_lock = threading.Lock()
        # debug field
        self.debug = debug # debug mode, print messages
    
    def get_seqwind_length(self):
        self.seqwind_lock.acquire()
        length = len(self.seq_wind)
        self.seqwind_lock.release()
        return length
        
    def get_seqwind_element(self, idx):
        self.seqwind_lock.acquire()
        if len(self.seq_wind) > idx:
            element = self.seq_wind[idx]
        else:
            element = -1
        self.seqwind_lock.release()
        return element

    def get_seqwind_idx(self, seqnum):
        self.seqwind_lock.acquire()
        seqnums = [rec[0] for rec in self.seq_wind]
        self.seqwind_lock.release()
        if seqnum in seqnums:
            # print("found: " + len(seqnums))
            return seqnums.index(seqnum)
        else:
            # print("not found: " + len(seqnums))
            return -1
            
    def get_ackcount(self):
        self.ackcount_lock.acquire()
        count = self.ack_count
        self.ackcount_lock.release()
        return count

    def set_ackcount(self, reset):
        self.ackcount_lock.acquire()
        if reset:
            self.ack_count = 0
        else:
            self.ack_count += 1
        self.ackcount_lock.release()
    
    def read_file(self, file_name):
        with open(file_name) as f:
            data = f.readlines()
        return "".join(data)
    
    def make_pkt(self, msg_type, seq_num, length, data):
        return Packet(msg_type, seq_num, length, data)
    
    def start_timer(self):
        self.timer_lock.acquire()
        self.timer['start_time'] = int(round(time.time()*1000))
        self.timer['started'] = True
        self.timer_lock.release()
    
    def get_start_time(self):
        self.timer_lock.acquire()
        start_time = self.timer['start_time']
        self.timer_lock.release()
        return start_time
    
    def is_timer_started(self):
        self.timer_lock.acquire()
        started = self.timer['started']
        self.timer_lock.release()
        return started
    
    def stop_timer(self):
        self.timer_lock.acquire()
        self.timer['started'] = False
        self.timer_lock.release()
    
    def load_data(self, file_name, chunk_size):
        data = self.read_file(file_name)
        i = 0
        while i+chunk_size <= len(data):            
            self.data_buffer.append(data[i:i+chunk_size])
            i += chunk_size
        if i < len(data): 
            self.data_buffer.append(data[i+1:len(data)])
            
    def send_pkg(self, pkt):
        with socket(AF_INET, SOCK_DGRAM) as skt:
            skt.sendto(pkt.encode(), self.emu_sock)
    
    def rdt_send(self):
        time_watch = False
        while len(self.data_buffer) > 0:
            if self.debug: print("[SEND] length data buffer: " + str(len(self.data_buffer)))
            
            if self.get_seqwind_length() >= self.win_size:
                self.seqwind_full.acquire()
                self.seqwind_full.wait()
                self.seqwind_full.release()
            
            data = self.data_buffer.pop(0)
            pkt = self.make_pkt(DAT, self.send_seq, len(data), data)
            
            if self.debug: print("[SEND] push window, seq num: " + str(self.send_seq))
            
            self.seqwind_lock.acquire()
            self.seq_wind.append((self.send_seq, pkt))
            self.seqwind_empty.acquire()
            self.seqwind_empty.notify()
            self.seqwind_empty.release()
            if self.debug: print("[SEND] length window buffer: " + str(len(self.seq_wind)))
            self.seqwind_lock.release()
            
            self.send_pkg(pkt)
            self.send_seq = (self.send_seq + 1) % 32
                
            if not time_watch:
                self.start_timer()
                pkt_watch_thread = threading.Thread(target=self.pkt_watch)
                pkt_watch_thread.start()
                time_watch = True
                if self.debug: print("[SEND] pkt watch started")
        
        eot_pkt = self.make_pkt(EOT, self.send_seq, 0, '')
        self.send_pkg(eot_pkt)
        if self.debug: print("[SEND] EOT packet sent, sender thread terminate")
        
    def retransmit(self, pkt):
        self.send_pkg(pkt)
        self.winsize_lock.acquire()
        if self.win_size > 1:
            self.win_size = 1
            if self.debug: print("[RETRANS] window size change to: " + str(self.win_size))
        self.winsize_lock.release() 
    
    def pkt_watch(self):
        def get_time(start_time):
            return int(round(time.time()*1000)) - start_time
        while True:
            resend = True
            if self.get_seqwind_length() > 0:
                watch_item = self.get_seqwind_element(0)
                seqnum, pkt = watch_item[0], watch_item[1]
                orig_len = self.get_seqwind_length()
                
                if self.debug: print("[WATCH] watching seqnum: " + str(seqnum))
                
                start_time = self.get_start_time()
                while (self.is_timer_started()) & (get_time(start_time)<=self.timeout):
                    # if self.debug: print("[WATCH] time elapse: " + str(get_time(start_time)))
                    
                    if self.get_seqwind_length() < orig_len:
                        resend = False
                        self.start_timer()
                        if self.debug: print("[WATCH] pkt acked, timer restarted")
                        break
                    
                    if self.get_ackcount() > 2:
                        if self.debug: print("[WATCH] ack count = 3")
                        break
                    
                if resend:
                    self.retransmit(pkt)
                    self.set_ackcount(reset=True)
                    self.start_timer()
                    if self.debug: print("[WATCH] retransmit seqnum: " + str(seqnum))
            else:
                if self.is_timer_started() == True:
                    self.stop_timer()
                    if self.debug: print("[WATCH] timer stopped")
                elif self.is_timer_started() == 'EOT':
                    if self.debug: print("[WATCH] EOT detected, terminate")
                    break
                self.seqwind_empty.acquire()
                self.seqwind_empty.wait()
                self.seqwind_empty.release()
                
    def recv_pkt(self, max_size=1024):
        with socket(AF_INET, SOCK_DGRAM) as skt:
            skt.bind(('', self.ack_port))
            eot_recv = False
            while not eot_recv:
                ack = Packet(skt.recv(max_size))
                eot_recv = self.rdt_recv(ack)
    
    def rdt_recv(self, ack):
        typ, seqnum, _, _, = ack.decode()
        if self.debug: print("[RECV] receive seqnum: " + str(seqnum))
        if typ == EOT:
            self.seqwind_empty.acquire()
            self.seqwind_empty.notify()
            self.seqwind_empty.release()
            self.timer['started'] = 'EOT'
            if self.debug: print("[RECV] EOT received, end program")
            return True # terminate program
        else:
            drop_ind = self.get_seqwind_idx(seqnum)
            if self.debug: print("[RECV] drop index: " + str(drop_ind))
            if drop_ind >= 0: # seqnum inside window
                
                # if self.debug: print("[RECV] drop index: " + str(drop_ind))
                
                self.seqwind_full.acquire()
                self.seqwind_lock.acquire()
                self.seq_wind = self.seq_wind[drop_ind+1:]
                self.seqwind_lock.release()
                self.seqwind_full.notify()
                self.seqwind_full.release()
                
                if self.debug: print("[RECV] length window buffer after drop: " + str(self.get_seqwind_length()))
                if self.get_seqwind_length() > 0:
                    self.recv_seq = self.get_seqwind_element(0)[0]
                else:
                    self.recv_seq = (seqnum+1)%32
                
                if self.debug: print("[RECV] recv seqnum updated to: " + str(self.recv_seq))
                                    
                self.winsize_lock.acquire()
                if self.win_size <= 10:
                    self.seqwind_full.acquire()
                    self.win_size += 1
                    self.seqwind_full.notify()
                    self.seqwind_full.release()
                    if self.debug: print("[RECV] window size change to: " + str(self.win_size))
                self.winsize_lock.release()    
                
            elif (self.recv_seq == 0 & seqnum == 31) | (seqnum == self.recv_seq - 1): # received seqnum n-1 
                self.set_ackcount(reset=False)
            return False
    
    def run(self):
        rdtsend_thread = threading.Thread(target=self.rdt_send)
        rdtrecv_thread = threading.Thread(target=self.recv_pkt)
        
        if sender.debug: print("start threads")
        
        rdtsend_thread.start()
        rdtrecv_thread.start()
        
        rdtsend_thread.join()
        rdtrecv_thread.join()
            
            
        

if __name__ == '__main__':
    # parse arguments
    # parser = argparse.ArgumentParser()
    # parser.add_argument("<emu_addr>")
    # parser.add_argument("<emu_port_num>")
    # parser.add_argument("<sender_port_num>")
    # parser.add_argument("<file_name>")
    # args = parser.parse_args()
    
    # args = args.__dict__
    # emu_sock = (str(args["<emu_addr>"]), int(args["<emu_port_num>"]))
    # ack_port = int(args["<sender_port_num>"])
    # file_name = str(args["<file_name>"])
    
    emu_sock = (str('localhost'), int(8080))
    ack_port = int(8083)
    file_name = 'test.txt'
    
    sender = Sender(win_size=1, timeout=300, emu_sock=emu_sock, ack_port=ack_port, debug=True)
    if sender.debug: print("sender created")
    sender.load_data(file_name=file_name, chunk_size=1)
    if sender.debug: print("data loaded, length: " + str(len(sender.data_buffer)))
    
    sender.run()
    
    # test script
    # emu_sock = (str('localhost'), int(8080))
    # ack_port = int(8083)
    # file_name = 'test.txt'
    
    # sender = Sender(win_size=1, timeout=500, emu_sock=emu_sock, ack_port=ack_port, debug=True)
    
    # for i in range(50):
    #     seqnum = i % 32
    #     if seqnum == 3:
    #         continue
    #     elif seqnum == 6:
    #         data = 'packet ' + str(3) +  ' seqnum: ' + str(3) + '\n'
    #         pkt = sender.make_pkt(msg_type=DAT, seq_num=3, length=len(data), data=data)
    #         sender.send_pkg(pkt)
    #         time.sleep(0.5)
    #     data = 'packet ' + str(i) +  ' seqnum: ' + str(seqnum) + '\n'
    #     pkt = sender.make_pkt(msg_type=DAT, seq_num=seqnum, length=len(data), data=data)
    #     sender.send_pkg(pkt)
    #     time.sleep(0.5)
        
    # pkt = sender.make_pkt(msg_type=EOT, seq_num=(seqnum+1)%32, length=0, data='')
    # sender.send_pkg(pkt)
    
    
    
    