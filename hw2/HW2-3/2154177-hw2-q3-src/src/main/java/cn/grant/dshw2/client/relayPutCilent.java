package cn.grant.dshw2.client;

import cn.grant.dshw2.FileBlockInfo;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class relayPutCilent extends MainClient{

    public relayPutCilent(String Path,String Name) {
        super(Path,Name); // 调用父类构造函数
    }

    public static void main(String[] args) {

        relayPutCilent rpc = new relayPutCilent(args[1],args[2]);
        directLoadClient dlc = new directLoadClient(rpc.storagePath,rpc.fileName);
        // 存储日志条目
        List<FileBlockInfo> logLines = dlc.getLogs();
        // 如果为空，说明之前没有传过文件，直接进行传递即可
        if(logLines.isEmpty()){
            directPutClient dpc = new directPutClient(rpc.storagePath,rpc.fileName);
            dpc.putFile();
        }
        // 首先检验文件是否已经是完整的了。
        else{
            int blocks = fileIntegrityCheck(logLines);
            if(blocks == -2){
                System.out.println("Error at Server End");
            }else if(blocks != -1){
                // 是完整的，则退出;不是完整的，则开始续传
                rpc.putFileRelay(blocks);
            }
        }


    }

    public void putFileRelay(int blocks){
        try{
            connectToServers(-1);
            File fileToUpload = new File(fileName);
            if (!fileToUpload.exists()) {
                System.err.println("File " + fileName + " does not exist.");
                return;
            } else {
                // 记录时间
                long startTime = System.currentTimeMillis();
                // 打开文件并执行上传操作
                long fileSize = fileToUpload.length();
                int blockSize = 1024 * 1024; // 1MB block size
                int numBlocks = (int) Math.ceil((double) fileSize / blockSize);
                Random rand = new Random();

                for(int i =0;i <3;i++)
                {
                    outs[i].writeObject("put_" + fileName);
                    outs[i].flush();
                }
                // Send file information to servers
                for (int i = 0; i < numBlocks; i++) {
                    // 已经传过的就不用传了
                    if(i<blocks){
                        continue;
                    }
                    int serverChoice = rand.nextInt(3);
                    int serverIndex = serverChoice % servers.length;

                    String thisServerIP = serverHosts[serverIndex];
                    String outFileName = fileName;

                    FileBlockInfo blockInfo = new FileBlockInfo(outFileName, i + 1, thisServerIP, i == numBlocks-1);

                    //logRecord(blockInfo);
                    System.out.println("上传：" + blockInfo.getFileName() + "的" + blockInfo.getBlockNumber()+"块到"+blockInfo.getServerAddress());

                    //传输文件信息（包括 文件名、块号、服务器地址） 这个内容会存储到服务器上
                    outs[serverIndex].writeObject(blockInfo);

                    byte[] buffer = new byte[blockSize];
                    FileInputStream fileInputStream = new FileInputStream(fileToUpload);

                    fileInputStream.skip(i * blockSize);
                    int bytesRead = fileInputStream.read(buffer);
                    // 传输读到的字节数 （Considering whether it's necessary）
                    outs[serverIndex].writeInt(bytesRead);
                    // 使用输出流传输实际的文件块
                    outs[serverIndex].write(buffer, 0, bytesRead);
                    outs[serverIndex].flush();
                    fileInputStream.close();

//                    try {
//                        // 让程序暂停0.5秒（500毫秒）
//                        Thread.sleep(500);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
                for(int i = 0; i< servers.length;i++)
                {
                    FileBlockInfo blockInfo = new FileBlockInfo("", -1, "", true);
                    System.out.println(blockInfo.getFileName());
                    outs[i].writeObject(blockInfo);
                }

                // Close the connection
                closeConnections(-1);

                long endTime = System.currentTimeMillis();

                // 计算执行时间（毫秒）
                long executionTime = endTime - startTime;
                System.out.println("此次接续上传共花费: " + executionTime + " 毫秒");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // 检验所存的文件是否是完整的
    public static int fileIntegrityCheck(List<FileBlockInfo> logLines) {
        int lastIndex = logLines.size() - 1;

        if (lastIndex >= 0) {
            FileBlockInfo lastLogLine = logLines.get(lastIndex);

            if (lastLogLine.isEnd()) {
                return -1;
            } else {
                return lastLogLine.getBlockNumber();
            }
        } else {
            // 如果loglines出错
            return -2;
        }
    }

}
