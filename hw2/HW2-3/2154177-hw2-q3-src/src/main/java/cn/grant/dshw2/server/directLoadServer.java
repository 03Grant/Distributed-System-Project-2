package cn.grant.dshw2.server;

import cn.grant.dshw2.IPsetting;

public class directLoadServer {
    public static void main(String[] args) {
        String serverIP1 = IPsetting.IP1;
        int serverPort1 = 4177;

        String serverIP2 = IPsetting.IP2;
        int serverPort2 = 4177;

        String serverIP3 = IPsetting.IP3;
        int serverPort3 = 4177;

        serverLoadThread serverLoadThread1 = new serverLoadThread(serverIP1, serverPort1);
        serverLoadThread1.start();

        serverLoadThread serverLoadThread2 = new serverLoadThread(serverIP2, serverPort2);
        serverLoadThread2.start();

        serverLoadThread serverLoadThread3 = new serverLoadThread(serverIP3, serverPort3);
        serverLoadThread3.start();
    }
}
