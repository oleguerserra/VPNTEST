import csv
import socket
import datetime
import threading
import sys
import os
import signal

LOG_FILE = 'connections.csv'
PORT_RANGE = range(25, 101)  # Change this to the range of ports you want to listen on

# Define a function to listen on a single port
def listen_on_port(port):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.bind(('0.0.0.0', port))
    sock.listen(socket.SOMAXCONN)
    #print(f'Listening on port {port}')
    with open(LOG_FILE, 'a', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        if csvfile.tell() == 0:
            writer.writerow(['Timestamp', 'IP', 'Port', 'Received Timestamp', 'Received IP', 'Received UUID', 'IP Match', 'IP Loc'])
        while True:
            try:
                conn, addr = sock.accept()
                timestamp = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                data = conn.recv(1024).decode('utf-8')  # Read the data sent by the client
                data = data.strip()  # Remove leading/trailing whitespaces
                fields = data.split(',')  # Split the received data into fields
                if len(fields) != 5:
                    raise ValueError('Invalid data format')
                received_timestamp, received_muid, received_ip, received_uuid, ip_loc = fields
                ip_match = 'Equal' if addr[0] == received_ip else 'Different'
                writer.writerow([timestamp, received_muid, addr[0], addr[1], received_timestamp, received_ip, received_uuid, ip_match, ip_loc])
                csvfile.flush()
                #print(f'New connection: {timestamp}, {addr[0]}, {addr[1]}')
                #print(f'Received data: {data}')
                response = f'{addr[0]},{received_uuid}'
                conn.sendall(response.encode('utf-8'))
            except (UnicodeDecodeError, ValueError) as e:
                print(f'Error processing data: {str(e)}. Skipping the current connection.')
            except Exception as e:
                print(f'Unexpected error occurred: {str(e)}. Skipping the current connection.')
            finally:
                if conn:
                    conn.close()

# Start a new thread for each port in the range
threads = []

def open_sockets():
    for port in PORT_RANGE:
        t = threading.Thread(target=listen_on_port, args=(port,))
        threads.append(t)
        t.start()

def stop_sockets():
    os.kill(os.getpid(), signal.SIGTERM)

def start_background_service():
    pid = os.fork()
    if pid == 0:
        # Child process
        open_sockets()
    else:
        # Parent process
        print(f'Started background service with PID {pid}')
        sys.exit(0)

def stop_background_service():
    pid = os.fork()
    if pid == 0:
        # Child process
        stop_sockets()
    else:
        # Parent process
        print(f'Stopped background service with PID {pid}')
        sys.exit(0)

def main():
    if len(sys.argv) != 2 or sys.argv[1] not in ['start', 'stop']:
        print('Usage: python script.py [start|stop]')
        sys.exit(1)

    command = sys.argv[1]
    if command == 'start':
        start_background_service()
    elif command == 'stop':
        stop_background_service()

if __name__ == '__main__':
    main()

