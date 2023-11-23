package cn.grant.dshw2.client;
import cn.grant.dshw2.FileBlockInfo;
import cn.grant.dshw2.IPsetting;

import java.io.*;
import java.net.*;
import java.util.*;

public class directPutClient extends MainClient{

    public directPutClient(String Path,String Name) {
        super(Path,Name); // 调用父类构造函数
    }
    public static void main(String[] args) {

        directPutClient dpc = new directPutClient(args[1],args[2]);
        dpc.putFile();
    }

    public void putFile(){
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
                    int serverChoice = rand.nextInt(3);
                    int serverIndex = serverChoice % servers.length;

                    String thisServerIP = serverHosts[serverIndex];
                    String outFileName = fileName;


                    FileBlockInfo blockInfo = new FileBlockInfo(outFileName, i + 1, thisServerIP, i == numBlocks-1);
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
                System.out.println("此次上传共花费: " + executionTime + " 毫秒");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

