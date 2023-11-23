package cn.grant.dshw2.client;
import cn.grant.dshw2.FileBlockInfo;
import cn.grant.dshw2.IPsetting;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class directLoadClient extends MainClient{
    public directLoadClient(String Path,String Name) {
        super(Path,Name); // 调用父类构造函数

    }


    // main函数来控制后续的函数调用
    public static void main(String[] args) {

        directLoadClient dlc = new directLoadClient(args[1],args[2]);

        long startTime = System.currentTimeMillis();
        // 存储日志条目
        List<FileBlockInfo> logLines = dlc.getLogs();
        // 根据日志获取文件
        dlc.getFile(logLines,0);
        //合并文件
        dlc.mergeFile(logLines);

        long endTime = System.currentTimeMillis();

        // 计算执行时间（毫秒）
        long executionTime = endTime - startTime;
        System.out.println("此次下载共花费: " + executionTime + " 毫秒");

    }

    // 根据日志文件，分别从服务端进行下载文件,mode 0 为一般下载；mode 1 为接续下载
    public void getFile(List<FileBlockInfo> logLines,int mode){
        try {
            if(logLines.isEmpty()) {
                System.out.println("No file has been found");
                return;
            }else{

                for (FileBlockInfo logLine : logLines) {

                    int targetServer;
                    if (logLine.getServerAddress().equals(serverIP1)){
                        targetServer = 0;
                    }else if(logLine.getServerAddress().equals(serverIP2)){
                        targetServer = 1;
                    }else if(logLine.getServerAddress().equals(serverIP3)){
                        targetServer = 2;
                    }else
                    {
                        System.out.println("Log info is invalid");
                        continue;
                    }
                    connectToServers(targetServer); //启动连接

                    System.out.println(logLine.getFileName() + " " + logLine.getBlockNumber() + " " + logLine.getServerAddress() );
                    outs[targetServer].writeObject("download_"+ logLine.getFileName()+"_"+logLine.getBlockNumber());
                    ObjectInputStream in = new ObjectInputStream(servers[targetServer].getInputStream());
                    // 接收文件字节数
                    long fileSize = in.readLong(); // 从服务器接收文件的总字节数
                    // 根据字节数接收文件
                    String downloadPath = storagePath + logLine.getFileName()+"_"+logLine.getBlockNumber(); // 保存下载文件的路径
                    FileOutputStream fileOutputStream = new FileOutputStream(downloadPath);

                    // 创建缓冲区
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    // 根据文件字节数接收文件内容
                    while (fileSize > 0 && (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                        fileSize -= bytesRead;
                    }
                    fileOutputStream.close();
                  //  if(mode == 1){
                    logRecord(logLine);
                //    }

                    in.close();
                    System.out.println("Receive " + logLine.getFileName()+"_"+logLine.getBlockNumber()+" Successfully!");
                    if(logLine.isEnd()){
                        System.out.println("All blocks received!");
                    }
                    closeConnections(targetServer);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 请求服务器端的日志文件
    public List<FileBlockInfo> getLogs() {
        List<FileBlockInfo> logLines = new ArrayList<>();
        try {
            connectToServers(-1);
            for (int i = 0; i < serverHosts.length; i++) {
                try {
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
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            closeConnections(-1);
            // 处理日志文件中的信息
            Collections.sort(logLines, new FileBlockInfo.BlockNumberComparator());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logLines;
    }


    //文件合并函数，合并从服务端下载的文件，同时删除接受的文件块信息
    public void mergeFile(List<FileBlockInfo> fileLists){
        try {
            FileOutputStream outputStream = new FileOutputStream(storagePath + fileName);
            for(FileBlockInfo files : fileLists){
                FileInputStream tempFileInputStream = new FileInputStream(storagePath + files.getFileName()+"_" + files.getBlockNumber());
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = tempFileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                tempFileInputStream.close();
            }
            outputStream.close();
            // 删除块文件
            for(FileBlockInfo files : fileLists){
                File originalFile = new File(storagePath + files.getFileName()+"_" + files.getBlockNumber());
                if (originalFile.exists()) {
                    try {
                        if (!originalFile.delete()) {
                            System.out.println("This file " +storagePath + files.getFileName()+"_" + files.getBlockNumber()  + " can't be deleted!");
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("原始文件不存在: " + originalFile.getAbsolutePath());
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // 记录下载文件，用于续传
    public void logRecord(FileBlockInfo blockInfo) {
        // 每行信息为 源文件名 块号 存储的服务器IP 是否为末尾块
        String logMessage = fileName + " "+ blockInfo.getBlockNumber() + " " + blockInfo.getServerAddress() + " " + blockInfo.isEnd();
        try {
            File logFile = new File(storagePath+"clientRecord.log");
            FileWriter writer = new FileWriter(logFile, true);
            writer.write(logMessage + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //测试联通函数，供编程测试用
    private void checkSocket(int num)
    {
        System.out.println(num);
        for(int i = 0;i < 3; i++){
            if(servers[i].isClosed())
            {
                System.out.println("Server"+i+" is closed");
            }
            else {
                System.out.println("Server"+i+" is running");
            }

        }

    }
}

