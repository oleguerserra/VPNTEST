import requests
from scapy.all import *
import sys
import csv
from ipaddress import ip_address, IPv4Address, IPv6Address
import socket

def is_public_ip(ip):
    """
    Checks if the IP address is within the public IP range.
    """
    ip_obj = ip_address(ip)
    return not ip_obj.is_private

def get_ip_geolocation(ip, api_key):
    """
    Retrieves the geolocation information for the given IP using the ipgeolocation.io API.
    """
    url = f"https://api.ipgeolocation.io/ipgeo?apiKey={api_key}&ip={ip}"
    response = requests.get(url)
    if response.status_code == 200:
        data = response.json()
        return {
            'country': data['country_name'],
            'isp': data['isp'],
            'organization': data['organization'],
            'latitude': data['latitude'],
            'longitude': data['longitude']
        }
    else:
        return None

def get_service_from_port(port):
    """
    Attempts to guess the service based on the port number.
    """
    try:
        service = socket.getservbyport(port)
        return service
    except OSError:
        return 'Unknown'

def packet_callback(packet):
    if TCP in packet:
        dst_ip = packet[IP].dst
        #print(f'IP:{dst_ip}')
        dst_port = packet[TCP].dport
        if is_public_ip(dst_ip):
            if dst_ip not in discovered_ips:
                discovered_ips.add(dst_ip)
                geolocation = get_ip_geolocation(dst_ip, api_key)
                if geolocation is not None:
                    location = f"{geolocation['country']}-{geolocation['isp']}-{geolocation['organization']}-{geolocation['latitude']}.{geolocation['longitude']}"
                    service = get_service_from_port(dst_port)
                    log_entry = [
                        dst_ip,
                        location,
                        dst_port,
                        service
                    ]
                    writer.writerow(log_entry)
                    csvfile.flush()  # Flush the CSV file after each write

if len(sys.argv) < 3:
    print("Usage: python script.py <interface> <api_key>")
    sys.exit(1)

interface = sys.argv[1]
api_key = sys.argv[2]

discovered_ips = set()

with open('log/ips.csv', 'a', newline='') as csvfile:  # Open the file in append mode ('a')
    writer = csv.writer(csvfile)
    # If the file is empty, write the header row
    if csvfile.tell() == 0:
        writer.writerow(['IP', 'Location', 'Port', 'Service'])

    sniff(iface=interface, filter='tcp', prn=packet_callback)

