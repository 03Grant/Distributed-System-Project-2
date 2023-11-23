// 文件传输时的序列化信息
package cn.grant.dshw2;
import java.io.Serializable;
import java.util.Comparator;

public class FileBlockInfo implements Serializable {
    private String fileName; // 文件名,用于区分各块属于which客户端上传的文件
    private int blockNumber; // 块号，该块在文件中的位置
    private String serverIP; // 目标地址
    private boolean endMark;

    public FileBlockInfo(String fileName, int blockNumber, String serverIP, boolean isEnd) {
        this.fileName = fileName;
        this.blockNumber = blockNumber;
        this.serverIP = serverIP;
        this.endMark = isEnd;
    }

    public String getFileName() {
        return fileName;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public String getServerAddress() {
        return serverIP;
    }
    public boolean isEnd() {
        return endMark;
    }

    public static class BlockNumberComparator implements Comparator<FileBlockInfo> {
        @Override
        public int compare(FileBlockInfo blockInfo1, FileBlockInfo blockInfo2) {
            // 比较两个 FileBlockInfo 对象的 blockNumber
            return Integer.compare(blockInfo1.getBlockNumber(), blockInfo2.getBlockNumber());
        }
    }
}
