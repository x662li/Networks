import argparse
from packet import Packet
from socket import *
import threading
import time
import logging

class Sender:
    
    global ACK
    global DAT
    global EOT
    ACK = 0
    DAT = 1
    EOT = 2
    """
        Sender constructor
    """
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
        self.tstmp = 0 # time stamp for logging
        self.timer = {"start_time": 0, "started": False} # track time, record start time when started, when EOT, started = 'EOT'
        # buffers
        self.data_buffer = [] # loaded data from file, stored as chunks
        self.seq_wind = [] # window buffer for seq num and packet, [oldest un-acked pkt ---- newly sent pkt], format (seq num, pkt) 
        # locks
        self.winsize_lock = threading.Lock() # window size lock
        self.seqwind_lock = threading.Lock() # window buffer lock
        self.tstmp_lock = threading.Lock() # time stamp lock
        self.seqwind_full = threading.Condition() # window buffer full condition 
        self.seqwind_empty = threading.Condition() # window buffer empty condition
        self.ackcount_lock = threading.Lock() # ack counter lock
        self.timer_lock = threading.Lock() # timer lock
        # loggers
        self.seqnum_log = self.config_logger('seqnum_log', './logs/seqnum.log') # seqnum logger
        self.ack_log = self.config_logger('ack_log', './logs/ack.log') # ack logger
        self.n_log = self.config_logger('n_log', './logs/N.log') # N logger
        # debug field
        self.debug = debug # debug mode, print messages if True
    
    """
        get length of seqwind
    """
    def get_seqwind_length(self):
        self.seqwind_lock.acquire()
        length = len(self.seq_wind)
        self.seqwind_lock.release()
        return length
    
    """
        find seqwind element by index, return -1 if not found
    """
    def get_seqwind_element(self, idx):
        self.seqwind_lock.acquire()
        if len(self.seq_wind) > idx:
            element = self.seq_wind[idx]
        else:
            element = -1
        self.seqwind_lock.release()
        return element

    """
        get seqwind index from seqnum, return -1 if not found
    """
    def get_seqwind_idx(self, seqnum):
        self.seqwind_lock.acquire()
        seqnums = [rec[0] for rec in self.seq_wind]
        self.seqwind_lock.release()
        if seqnum in seqnums:
            return seqnums.index(seqnum)
        else:
            return -1
    
    """
        getter for ackcount
    """
    def get_ackcount(self):
        self.ackcount_lock.acquire()
        count = self.ack_count
        self.ackcount_lock.release()
        return count
    
    """
        setter for ackcount, either reset to 0 or increment by 1
    """
    def set_ackcount(self, reset):
        self.ackcount_lock.acquire()
        if reset:
            self.ack_count = 0
        else:
            self.ack_count += 1
        self.ackcount_lock.release()
    
    """
        read content from file, return a single string
    """
    def read_file(self, file_name):
        with open(file_name) as f:
            data = f.readlines()
        return "".join(data)
    
    """
        construct packet
    """
    def make_pkt(self, msg_type, seq_num, length, data):
        return Packet(msg_type, seq_num, length, data)
    
    """
        set timer to start, and record current time in milliseconds
    """
    def start_timer(self):
        self.timer_lock.acquire()
        if self.timer['started'] != 'EOT': # if EOT received, do not start again
            self.timer['start_time'] = int(round(time.time()*1000))
            self.timer['started'] = True
        self.timer_lock.release()
    
    """
        getter for start_time
    """
    def get_start_time(self):
        self.timer_lock.acquire()
        start_time = self.timer['start_time']
        self.timer_lock.release()
        return start_time
    
    """
        check timer status
    """
    def is_timer_started(self):
        self.timer_lock.acquire()
        started = self.timer['started']
        self.timer_lock.release()
        return started
    
    """
        set timer to stop if not EOT
    """
    def stop_timer(self):
        self.timer_lock.acquire()
        if self.timer['started'] != 'EOT':
            self.timer['started'] = False
        self.timer_lock.release()
    
    """
        divide data into chunks, and store in data_buffer
    """
    def load_data(self, file_name, chunk_size):
        data = self.read_file(file_name)
        i = 0
        while i+chunk_size <= len(data):            
            self.data_buffer.append(data[i:i+chunk_size]) # chunks of chunk_size large
            i += chunk_size
        if i < len(data): 
            self.data_buffer.append(data[i:len(data)]) # last chunk
    
    """
        open a UDP socket and send packet through it to the emulator
    """
    def send_pkg(self, pkt):
        with socket(AF_INET, SOCK_DGRAM) as skt:
            skt.sendto(pkt.encode(), self.emu_sock)
    
    """
        reliable data transfer send, run as a thread
    """
    def rdt_send(self):
        time_watch = False # check if start timer for the first time
        while len(self.data_buffer) > 0: # while still data to send
            if self.debug: print("[SEND] length data buffer: " + str(len(self.data_buffer)))
            # wait on full condition if seq_wind is full
            if self.get_seqwind_length() >= self.win_size:
                self.seqwind_full.acquire()
                self.seqwind_full.wait()
                self.seqwind_full.release()
            # if not full pop from data_buffer, and send
            data = self.data_buffer.pop(0)
            pkt = self.make_pkt(DAT, self.send_seq, len(data), data)
            if self.debug: print("[SEND] push window, seq num: " + str(self.send_seq))
            # push poped packet to seq_wind, notify threads wait on empty condition
            self.seqwind_lock.acquire()
            self.seq_wind.append((self.send_seq, pkt))
            self.seqwind_empty.acquire()
            self.seqwind_empty.notify()
            self.seqwind_empty.release()
            if self.debug: print("[SEND] length window buffer: " + str(len(self.seq_wind)))
            self.seqwind_lock.release()
            # send packet, record seqnum_log, update send seqnum
            self.send_pkg(pkt)
            self.rec_log(self.seqnum_log, self.send_seq)
            self.send_seq = (self.send_seq + 1) % 32
            # start timer if its first iteration, and start packet_watch thread   
            if not time_watch:
                self.start_timer()
                pkt_watch_thread = threading.Thread(target=self.pkt_watch)
                pkt_watch_thread.start()
                time_watch = True
                if self.debug: print("[SEND] pkt watch started")
        # after all data sent, send EOT packet and terminate thread
        eot_pkt = self.make_pkt(EOT, self.send_seq, 0, '')
        self.send_pkg(eot_pkt)
        self.rec_log(self.seqnum_log, 'EOT')
        if self.debug: print("[SEND] EOT packet sent, sender thread terminate")
    
    """ 
        re-transmit packet (send immediately, does not contrained by window size)
    """
    def retransmit(self, pkt):
        self.send_pkg(pkt) # send packet
        # set window size to 1
        self.winsize_lock.acquire()
        if self.win_size > 1:
            self.win_size = 1
            self.rec_log(self.n_log, self.win_size)
            if self.debug: print("[RETRANS] window size change to: " + str(self.win_size))
        self.winsize_lock.release() 
    
    """ 
        packet_watch thread, packet timing, retransmit and controlling timer
    """
    def pkt_watch(self):
        # helper function to compute time elapsed since start_time (millisec)
        def get_time(start_time):
            return int(round(time.time()*1000)) - start_time
        while True:
            # if EOT, terminate thread
            if self.is_timer_started() == 'EOT':
                if self.debug: print("[WATCH] EOT detected, terminate")
                break
            resend = True # set resend indicator to true
            # if there are still item in seq_wind, record seqwind length, and record item being watched
            if self.get_seqwind_length() > 0:
                watch_item = self.get_seqwind_element(0)
                seqnum, pkt = watch_item[0], watch_item[1]
                orig_len = self.get_seqwind_length()
                if self.debug: print("[WATCH] watching seqnum: " + str(seqnum))
                # check for timeout
                start_time = self.get_start_time()
                while (self.is_timer_started() == True) & (get_time(start_time)<=self.timeout):
                    # if item removed from seq_wind (acked), re-start timer and watch the next oldest one
                    if self.get_seqwind_length() < orig_len:
                        resend = False
                        self.start_timer()
                        if self.debug: print("[WATCH] pkt acked, timer restarted")
                        break
                    # if ack_account = 3, keep resend indicator to true
                    if self.get_ackcount() > 2:
                        if self.debug: print("[WATCH] ack count = 3")
                        break
                # retransmit (time out or dup ack), then re-start timer, reset ack_count and record seqnum_log
                if resend:
                    self.retransmit(pkt)
                    self.set_ackcount(reset=True)
                    self.start_timer()
                    self.rec_log(self.seqnum_log, seqnum)
                    if self.debug: print("[WATCH] retransmit seqnum: " + str(seqnum))
            # if seq_wind is empty, stop timer and wait on empty condition
            else:
                if self.is_timer_started() == True:
                    self.stop_timer()
                    if self.debug: print("[WATCH] timer stopped")
                self.seqwind_empty.acquire()
                self.seqwind_empty.wait()
                self.seqwind_empty.release()
    
    """ 
        packet receive thread, call rdt_recv when each packet (ack or EOT) arrived
    """        
    def recv_pkt(self, max_size=1024):
        with socket(AF_INET, SOCK_DGRAM) as skt: # open socket to listen on ack_port
            skt.bind(('', self.ack_port))
            eot_recv = False
            while not eot_recv:
                ack = Packet(skt.recv(max_size))
                eot_recv = self.rdt_recv(ack)
    
    """ 
        reliable data transfer receive, update seq_wind, ack_count and win_size
    """
    def rdt_recv(self, ack):
        typ, seqnum, _, _, = ack.decode()
        if self.debug: print("[RECV] receive seqnum: " + str(seqnum))
        # if EOT received, send timer started to EOT (to notify packet watcher), terminate thread
        if typ == EOT:
            self.seqwind_empty.acquire()
            self.seqwind_empty.notify()
            self.seqwind_empty.release()
            self.timer['started'] = 'EOT'
            self.rec_log(self.ack_log, 'EOT')
            if self.debug: print("[RECV] EOT received, end program")
            return True
        else:
            self.rec_log(self.ack_log, seqnum) # record ack_log
            # find index in seq_wind, then truncate the already acked ones, notify threads wait on full condition
            drop_ind = self.get_seqwind_idx(seqnum)
            if self.debug: print("[RECV] drop index: " + str(drop_ind))
            if drop_ind >= 0: # drop index inside window
                self.seqwind_full.acquire()
                self.seqwind_lock.acquire()
                self.seq_wind = self.seq_wind[drop_ind+1:]
                self.seqwind_lock.release()
                self.seqwind_full.notify()
                self.seqwind_full.release()
                # update oldest_unacket seqnum (recv_seq)
                if self.debug: print("[RECV] length window buffer after drop: " + str(self.get_seqwind_length()))
                if self.get_seqwind_length() > 0:
                    self.recv_seq = self.get_seqwind_element(0)[0] # recv_seq = seq of first element in seq_wind
                else:
                    self.recv_seq = (seqnum+1)%32 # if empty, increase recv_seq by 1
                if self.debug: print("[RECV] recv seqnum updated to: " + str(self.recv_seq))
                # acked successfully, increase win_size by 1 if <= 10, notify threas wait on full condition
                self.winsize_lock.acquire()
                if self.win_size < 10:
                    self.seqwind_full.acquire()
                    self.win_size += 1
                    self.rec_log(self.n_log, self.win_size)
                    self.seqwind_full.notify()
                    self.seqwind_full.release()
                    if self.debug: print("[RECV] window size change to: " + str(self.win_size))
                self.winsize_lock.release()    
            # if received an previouw ack, (one before recv_seq), increase ack_count by 1
            elif (self.recv_seq == 0 & seqnum == 31) | (seqnum == self.recv_seq - 1):
                self.set_ackcount(reset=False)
            return False
    
    """ 
        config loggers to log into different files
    """
    def config_logger(self, logger_name, log_file, level=logging.INFO):
        hander = logging.FileHandler(log_file)
        formatter = logging.Formatter('%(message)s')
        hander.setFormatter(formatter)
        logger = logging.getLogger(logger_name)
        logger.setLevel(level)
        logger.addHandler(hander)
        return logger
    
    """ 
        record logs with specific format "t=TSTAMP RECORD"
    """
    def rec_log(self, logger, record):
        self.tstmp_lock.acquire()
        logger.info('t=' + str(self.tstmp) + ' ' + str(record))
        self.tstmp += 1
        self.tstmp_lock.release()
    
    """ 
        run method for sender, start send and receive threads
    """
    def run(self):
        
        rdtsend_thread = threading.Thread(target=self.rdt_send)
        rdtrecv_thread = threading.Thread(target=self.recv_pkt)
        
        if sender.debug: print("start threads")
        
        self.rec_log(self.n_log, self.win_size)
        
        rdtsend_thread.start()
        rdtrecv_thread.start()
        
        rdtsend_thread.join()
        rdtrecv_thread.join()
            
            
        

if __name__ == '__main__':
    # parse arguments
    parser = argparse.ArgumentParser()
    parser.add_argument("<emu_addr>")
    parser.add_argument("<emu_port_num>")
    parser.add_argument("<sender_port_num>")
    parser.add_argument("<file_name>")
    args = parser.parse_args()
    
    args = args.__dict__
    emu_sock = (str(args["<emu_addr>"]), int(args["<emu_port_num>"]))
    ack_port = int(args["<sender_port_num>"])
    file_name = str(args["<file_name>"])
    
    sender = Sender(win_size=1, timeout=200, emu_sock=emu_sock, ack_port=ack_port, debug=False) # construct sender
    if sender.debug: print("sender created")
    sender.load_data(file_name=file_name, chunk_size=500) # load data
    if sender.debug: print("data loaded, length: " + str(len(sender.data_buffer)))
    
    sender.run()
    
    
    