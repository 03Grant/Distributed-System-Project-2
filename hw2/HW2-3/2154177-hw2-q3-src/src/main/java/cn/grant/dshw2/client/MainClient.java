package cn.grant.dshw2.client;

import cn.grant.dshw2.IPsetting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainClient {
    public String[] serverHosts;
    public int serverPort;
    public Socket[] servers;
    public ObjectOutputStream[] outs;
    public ObjectInputStream[] ins;
    public String serverIP1;
    public String serverIP2;
    public String serverIP3;
    public String storagePath;
    public String fileName;

    public MainClient(String Path,String Name){
        serverIP1 = IPsetting.IP1;
        serverIP2 = IPsetting.IP2;
        serverIP3 = IPsetting.IP3;
        serverHosts = new String[]{serverIP1,serverIP2,serverIP3 };
        serverPort = 4177;
        servers = new Socket[3];
        outs = new ObjectOutputStream[3];
        ins = new ObjectInputStream[3];
        storagePath = Path;
        fileName = Name;
    }

    // 服务器连接函数
    // 参数 -1 尝试连接所有服务器
    // 1-3 中任一数字代表连接到指定的服务器
    public void connectToServers(int serverNum) throws IOException {
        if(serverNum == -1) {
            for (int i = 0; i < serverHosts.length; i++) {
                servers[i] = new Socket(serverHosts[i], serverPort);
                outs[i] = new ObjectOutputStream(servers[i].getOutputStream());
            }
        }
        else {
            servers[serverNum] = new Socket(serverHosts[serverNum], serverPort);
            outs[serverNum] = new ObjectOutputStream(servers[serverNum].getOutputStream());
        }
    }

    // 服务器断开函数
    // 参数 -1 尝试断开所有服务器
    // 1-3 中任一数字代表断开指定的服务器 （应与上面的函数配套使用）
    public void closeConnections(int serverNum) throws IOException {
        if (serverNum == -1) {
            for (int i = 0; i < serverHosts.length; i++) {
                if (outs[i] != null) {
                    outs[i].close();
                }
                if (servers[i] != null && !servers[i].isClosed()) {
                    servers[i].close();
                }
            }
        }else {
            if (outs[serverNum] != null) {
                outs[serverNum].close();
            }
            if (servers[serverNum] != null && !servers[serverNum].isClosed()) {
                servers[serverNum].close();
            }
        }
    }
}
