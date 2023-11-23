package cn.grant.dshw2.client;

import cn.grant.dshw2.FileBlockInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class relayLoadClient extends MainClient {
    public relayLoadClient(String Path,String Name){
        super(Path,Name);
    }

    public static void main(String[] args){
        // 1. 先根据文件名检查日志文件 （以下是自定义的规则）
        // 1.1 若日志文件不存在，说明不存在接续的问题，直接正常下载（因为如果接收到了完整文件，文件合并时会删去日志文件）
        // 1.2 若日志文件存在，说明存在接续的问题（可能有很多文件都有接续问题）
        // 2.先读取客户端日志文件，寻找是否有要下载文件的接续信息
        // 2.1 若没有，则同1.1
        // 2.2 若有，则保存下载文件的接续信息，再从服务端得到服务端日志文件，对比之后再进行下载。
        relayLoadClient rlc = new relayLoadClient(args[1],args[2]);
        directLoadClient dlc = new directLoadClient(rlc.storagePath,rlc.fileName);
        // 统一获取日志文件
        List<FileBlockInfo> logLines = dlc.getLogs();
        if(clientCheckExistence(rlc.storagePath)){
            List<FileBlockInfo> localLogs = clientCheckIntegrity(rlc.storagePath,rlc.fileName);
            if(localLogs.isEmpty()) {
                long startTime = System.currentTimeMillis();
                //1.1 && 2.1 不存在接续的问题，直接正常下载
                dlc.getFile(logLines,1);
                long endTime = System.currentTimeMillis();
                // 计算执行时间（毫秒）
                long executionTime = endTime - startTime;
                System.out.println("此次下载共花费: " + executionTime + " 毫秒");
                //合并文件
                dlc.mergeFile(logLines);
            }else{
                long startTime = System.currentTimeMillis();
                List<FileBlockInfo> waitlist = toBeDownLoaded(logLines,localLogs);
                dlc.getFile(waitlist,1);

                long endTime = System.currentTimeMillis();
                // 计算执行时间（毫秒）
                long executionTime = endTime - startTime;
                System.out.println("此次接续下载共花费: " + executionTime + " 毫秒");

                dlc.mergeFile(logLines);
            }
        }else{
            long startTime = System.currentTimeMillis();
            //1.1 && 2.1 不存在接续的问题，直接正常下载
            dlc.getFile(logLines,1);
            long endTime = System.currentTimeMillis();
            // 计算执行时间（毫秒）
            long executionTime = endTime - startTime;
            System.out.println("此次下载共花费: " + executionTime + " 毫秒");
            //合并文件
            dlc.mergeFile(logLines);
        }
    }


    // 检查需要下载的文件
    public static List<FileBlockInfo> toBeDownLoaded(List<FileBlockInfo> logLines, List<FileBlockInfo> localLogs) {
        List<FileBlockInfo> difference = new ArrayList<>();

        for (FileBlockInfo logLine : logLines) {
            boolean found = false;
            for (FileBlockInfo localLog : localLogs) {
                // 比较 FileBlockInfo 的四个元素是否相等
                if (logLine.getFileName().equals(localLog.getFileName()) &&
                        logLine.getBlockNumber() == localLog.getBlockNumber() &&
                        logLine.getServerAddress().equals(localLog.getServerAddress()) &&
                        logLine.isEnd() == localLog.isEnd()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                difference.add(logLine);
            }
        }

        return difference;
    }

    //检查日志文件是否包含要下载的文件信息
    public static List<FileBlockInfo> clientCheckIntegrity(String storagePath,String fileName){
        String logFilePath = storagePath + "clientRecord.log"; // 日志文件路径
        List<FileBlockInfo> logEntries = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFilePath));
            String line;
            while ((line = reader.readLine()) != null) {
                // 解析每一行日志条目并添加到 logEntries 中
                String[] parts = line.split(" ");
                if (parts.length == 4 && parts[0].equals(fileName)) {
                    int blockNumber = Integer.parseInt(parts[1]);
                    String serverIP = parts[2];
                    boolean isEnd = parts[3].equals("true");
                    FileBlockInfo logEntry = new FileBlockInfo(fileName, blockNumber, serverIP, isEnd);
                    logEntries.add(logEntry);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logEntries;
    }
    //检查日志文件是否存在
    public static boolean clientCheckExistence(String storagePath){
        String filePath = storagePath + "clientRecord.log";
        File file = new File(filePath);
        // 使用File对象的exists方法检查文件是否存在
        return file.exists();
    }

}
