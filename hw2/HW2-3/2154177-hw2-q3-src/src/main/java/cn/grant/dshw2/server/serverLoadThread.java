package cn.grant.dshw2.server;

import cn.grant.dshw2.FileBlockInfo;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class serverLoadThread extends Thread {
    private String serverIP;
    private final int serverPort;
    private String filePath;
    private String fileName;

    public serverLoadThread(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(serverIP, serverPort));
            System.out.println("Server is listening on " + serverIP + ":" + serverPort);
            filePath = storageSetting.serverStorageMap.get(serverIP);
            List<FileBlockInfo> logLines = null;
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                // 接收客户端请求
                // 分为1.put  2.request  3.load
                //System.out.println("Test:" + serverSocket.isClosed());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                String request = (String) in.readObject();


                if (request.startsWith("request_")) {
                    // Request连接关闭
                    logLines = serve_request(request,socket);
                    in.close();
                } else if (request.startsWith("download_")) {
                    serve_download(request,socket);
                    in.close();
                }else if(request.startsWith("put_")){
                    serve_put(socket,in);
                    in.close();
                }else if(request.startsWith("check_")){

                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private List<FileBlockInfo> serve_request(String request,Socket socket){
        List<FileBlockInfo> req_logLines = null;
        try{
            //获取想要下载的文件名信息
            fileName = request.substring("request_".length());
            // 读取日志文件内容，将文件头信息作为一个FileBlockInfo类实例 添加到logLines中
            req_logLines = readLogFile(filePath, fileName);

            // 发送日志文件内容给客户端
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            if (req_logLines.isEmpty()) {
                // 如果没有相关信息，发送一个提示给客户端
                FileBlockInfo info = new FileBlockInfo("", -3, "", false);
                out.writeObject(info);
                out.flush();
            }
            else{
                for (FileBlockInfo logLine : req_logLines) {
                    out.writeObject(logLine);
                    out.flush();
                }
                // 发送一个空字符串表示日志文件传输结束
                out.writeObject(null);
                out.flush();
            }

            // Request连接关闭
            out.close();
            socket.close();
            System.out.println("Client disconnected");

        }catch(Exception e){
            e.printStackTrace();
        }
        return req_logLines;
    }

    private void serve_download(String request,Socket socket){
        String downLoadName = request.substring("download_".length());
        String fileAddress = this.filePath+downLoadName;
        try {
            // 打开文件并获取文件的总字节数
            File file = new File(fileAddress);
            long fileSize = file.length();

            // 创建输入流以读取文件内容
            FileInputStream fileInputStream = new FileInputStream(file);

            // 创建输出流以发送数据给客户端
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            // 发送文件的总字节数给客户端
            out.writeLong(fileSize);
            out.flush();

            // 创建缓冲区
            byte[] buffer = new byte[1024];
            int bytesRead;

            // 从文件读取数据并发送给客户端
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }

            // 关闭输入流和输出流
            fileInputStream.close();
            out.close();
            // 关闭 socket
            socket.close();
            // logLine需要清空

            System.out.println("File sent to client");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void serve_put(Socket socket, ObjectInputStream in){
        // Continue receiving file blocks until an end marker is received
       try {
           while (true) {
               try {
                   // Receive file information
                   FileBlockInfo blockInfo = (FileBlockInfo) in.readObject();
                   if (blockInfo == null || (blockInfo.isEnd() && blockInfo.getBlockNumber() == -1 && blockInfo.getFileName().isEmpty())) {
                       // End marker received, exit the loop
                       break;
                   }
                   String fileName = blockInfo.getFileName();
                   int blockNumber = blockInfo.getBlockNumber();

                   //接收日志信息，先打印
                   System.out.println("Received the info of " + blockNumber + " block of" +
                           " file:" + fileName + " on IP:" + this.serverIP);


                   // Receive file data
                   int bytesRead = in.readInt();
                   byte[] buffer = new byte[bytesRead];
                   in.readFully(buffer, 0, bytesRead);

                   // Write file data to disk
                   FileOutputStream fileOutputStream = new FileOutputStream(filePath + fileName + "_" + blockNumber);
                   fileOutputStream.write(buffer);
                   fileOutputStream.close();
                   // 写完之后再记录日志
                   logReceivedInfo(fileName, blockNumber, this.serverIP, blockInfo.isEnd());
                   System.out.println("Received block " + blockNumber + " of file " + fileName);

               } catch (Exception e) {
                   // e.printStackTrace();
               }
           }
        // Close the socket after all blocks are received
           socket.close();
       }catch (Exception e){
           e.printStackTrace();
       }
        System.out.println("Client:" +this.serverIP +  " disconnected");
    }

    private void logReceivedInfo(String fileName, int blockNumber, String serverIP,boolean endInfo) {
        // 每行信息为 源文件名 块号 存储的服务器IP 是否为末尾块
        String logMessage = fileName + " "+ blockNumber + " " + serverIP + " " + endInfo;
        try {
            File logFile = new File(filePath+"serverlog.log");
            FileWriter writer = new FileWriter(logFile, true);
            writer.write(logMessage + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private List<FileBlockInfo> readLogFile(String filePath, String fileName) {
        List<FileBlockInfo> logEntries = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath + "serverlog.log"));
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
}
