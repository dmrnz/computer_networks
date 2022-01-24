import socket


class DHCPPacket:
    def __init__(self, type: bytes = bytes([0x00, 0x35, 0x01, 0x02])):
        self.OP = bytes([0x02])
        self.HTYPE = bytes([0x01])
        self.HLEN = bytes([0x06])
        self.HOPS = bytes([0x00])
        self.XID = bytes([0x39, 0x03, 0xF3, 0x26])
        self.SECS = bytes([0x00, 0x00])
        self.FLAGS = bytes([0x00, 0x00])
        self.CIADDR = bytes([0x00, 0x00, 0x00, 0x00])
        self.YIADDR = bytes([0xC0, 0xA8, 0x01, 0x64])  # 192.168.1.100
        self.SIADDR = bytes([0xC0, 0xA8, 0x01, 0x01])  # 192.168.1.1
        self.GIADDR = bytes([0x00, 0x00, 0x00, 0x00])
        self.CHADDR1 = bytes([0x00, 0x05, 0x3C, 0x04])
        self.CHADDR2 = bytes([0x8D, 0x59, 0x00, 0x00])
        self.CHADDR3 = bytes([0x00, 0x00, 0x00, 0x00])
        self.CHADDR4 = bytes([0x00, 0x00, 0x00, 0x00])
        self.CHADDR5 = bytes(192)
        self.Magiccookie = bytes([0x63, 0x82, 0x53, 0x63])
        self.DHCPOptions1: bytes = type
        self.DHCPOptions2 = bytes([1, 4, 0xFF, 0xFF, 0xFF, 0x00])  # 255.255.255.0 subnet mask
        self.DHCPOptions3 = bytes([3, 4, 0xC0, 0xA8, 0x01, 0x01])  # 192.168.1.1 router
        self.DHCPOptions4 = bytes([51, 4, 0x00, 0x01, 0x51, 0x80])  # 86400s(1 day) IP address lease time
        self.DHCPOptions5 = bytes([54, 4, 0xC0, 0xA8, 0x01, 0x01])  # DHCP server

    def pack(self):
        package = self.OP + self.HTYPE + self.HLEN + self.HOPS + self.XID + self.SECS + self.FLAGS + self.CIADDR + self.YIADDR + self.SIADDR + self.GIADDR \
                  + self.CHADDR1 + self.CHADDR2 + self.CHADDR3 + self.CHADDR4 + self.CHADDR5 + self.Magiccookie \
                  + self.DHCPOptions1 + self.DHCPOptions2 + self.DHCPOptions3 + self.DHCPOptions4 + self.DHCPOptions5
        return package

    def unpack(self, data: bytes):
        OP = bytes([0x02])
        HTYPE = bytes([0x01])
        HLEN = bytes([0x06])
        HOPS = bytes([0x00])
        XID = bytes([0x39, 0x03, 0xF3, 0x26])
        SECS = bytes([0x00, 0x00])
        FLAGS = bytes([0x00, 0x00])
        CIADDR = bytes([0x00, 0x00, 0x00, 0x00])
        YIADDR = bytes([0xC0, 0xA8, 0x01, 0x64])
        SIADDR = bytes([0xC0, 0xA8, 0x01, 0x01])
        GIADDR = bytes([0x00, 0x00, 0x00, 0x00])
        CHADDR1 = bytes([0x00, 0x05, 0x3C, 0x04])
        CHADDR2 = bytes([0x8D, 0x59, 0x00, 0x00])
        CHADDR3 = bytes([0x00, 0x00, 0x00, 0x00])
        CHADDR4 = bytes([0x00, 0x00, 0x00, 0x00])
        CHADDR5 = bytes(192)
        Magiccookie = bytes([0x63, 0x82, 0x53, 0x63])
        DHCPOptions1 = bytes([53, 1, 5])  # DHCP ACK(value = 5)
        DHCPOptions2 = bytes([1, 4, 0xFF, 0xFF, 0xFF, 0x00])  # 255.255.255.0 subnet mask
        DHCPOptions3 = bytes([3, 4, 0xC0, 0xA8, 0x01, 0x01])  # 192.168.1.1 router
        DHCPOptions4 = bytes([51, 4, 0x00, 0x01, 0x51, 0x80])  # 86400s(1 day) IP address lease time
        DHCPOptions5 = bytes([54, 4, 0xC0, 0xA8, 0x01, 0x01])  # DHCP server
        package = OP + HTYPE + HLEN + HOPS + XID + SECS + FLAGS + CIADDR + YIADDR + SIADDR + GIADDR \
                  + CHADDR1 + CHADDR2 + CHADDR3 + CHADDR4 + CHADDR5 + Magiccookie \
                  + DHCPOptions1 + DHCPOptions2 + DHCPOptions3 + DHCPOptions4 + DHCPOptions5
        return package


def get_fraction(number, precision):
    return int((number - int(number)) * 2 ** precision)


serverPort = 67
clientPort = 68

if __name__ == '__main__':
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    s.bind(('', serverPort))
    dest = ('255.255.255.255', clientPort)
    answer = DHCPPacket()

    while 1:
        try:
            print("Wait DHCP discovery.")
            data, address = s.recvfrom(1024)
            print("Receive DHCP discovery.")
            print(answer.unpack(data))

            print("Send DHCP offer.")
            data = DHCPPacket(bytes([0x00, 0x35, 0x01, 0x02]))
            s.sendto(data.pack(), dest)

            while 1:
                try:
                    print("Wait DHCP request.")
                    data, address = s.recvfrom(1024)
                    print("Receive DHCP request.")

                    print("Send DHCP ack.\n")
                    data = DHCPPacket(bytes([0x00, 0x35, 0x01, 0x05]))
                    s.sendto(data.pack(), dest)
                    break
                except:
                    raise

        except:
            raise
