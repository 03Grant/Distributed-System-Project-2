package cn.grant.dshw2.client;

import cn.grant.dshw2.FileBlockInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class checkClient extends MainClient {
    public checkClient(String Path,String Name){
        super(Path,Name);
    }

    public static void main(String[] args){
        checkClient ckc = new checkClient("",args[1]);
        int[] connected = null;
        try{
            connected = ckc.connectAndCheckServers();
        }catch(Exception e){
            e.printStackTrace();
        }

        List<FileBlockInfo> infos = null;
        if(connected != null && !ckc.fileName.isEmpty()) {
            infos = ckc.getLogs(connected);
            if(infos.isEmpty()){
                System.out.println("无文件："+ ckc.fileName+" 相关信息！");
            }else {
                for (FileBlockInfo info : infos) {
                    System.out.println("从" + info.getServerAddress() + "上检索到：" + info.getFileName() + " 的第" + info.getBlockNumber() + "块文件");
                    if (info.isEnd()) {
                        System.out.println("该文件在所有的服务器上的存储是完整的");
                    }
                }
            }
        }

    }

    public List<FileBlockInfo> getLogs(int[] connected) {
        List<FileBlockInfo> logLines = new ArrayList<>();
        try {

            for (int i = 0; i < serverHosts.length; i++) {
                if (connected[i] != 1)
                    continue;
                try {
                    connectToServers(i);
                    // 连接到服务器提示
                    //System.out.println("Connected to server " + serverHosts[i]);
                    // 请求日志文件
                    outs[i].writeObject("request_" + fileName);
                    outs[i].flush();

                    // 接收日志文件
                    FileBlockInfo logLine;
                    ins[i] = new ObjectInputStream(servers[i].getInputStream());
                    while ((logLine = (FileBlockInfo) ins[i].readObject()) != null) {
                        // -3 代表服务器无相应文件
                        if(logLine.getBlockNumber() == -3)
                            break;
                        logLines.add(logLine);
                        //System.out.println(logLine.getFileName() + " " + logLine.getBlockNumber());
                    }
                    ins[i].close();
                    closeConnections(i);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            // 处理日志文件中的信息
            Collections.sort(logLines, new FileBlockInfo.BlockNumberComparator());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logLines;
    }
    public int[] connectAndCheckServers() throws IOException {
        int[] connected = new int[3];

        int timeoutMillis = 2000; // 设置连接超时为2秒
        for (int i = 0; i < serverHosts.length; i++) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(serverHosts[i], serverPort), timeoutMillis);

                // 如果连接成功，将输出服务器联通成功消息
                if (socket.isConnected()) {
                    System.out.println("服务器联通成功: " + serverHosts[i]);
                    outs[i] = new ObjectOutputStream(socket.getOutputStream());
                    outs[i].writeObject("check_" + fileName);
                    outs[i].flush();
                    connected[i]=1;
                    outs[i].close();
                    socket.close(); // 关闭连接
                } else {
                    System.out.println("服务器联通失败: " + serverHosts[i]);
                    connected[i]=0;
                }
            } catch (IOException e) {
                System.out.println("服务器联通失败: " + serverHosts[i] + "，错误信息: " + e.getMessage());
            }
        }
        return connected;
    }


}
