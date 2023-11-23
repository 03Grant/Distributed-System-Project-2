import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class Code {
    public static void main(String[] args) {
        // 记录程序开始时间（纳秒）
        long startTime = System.nanoTime();

        // 设置种子
        Random random = new Random(244);
        double d1 = 0.0;
        double d2 = 0.0;
        final double possibility = 0.91;
        final int reservation = 1600;   // 一个分区装载100个数对
        // 可以调整的参数： reservation  possibility  种子seed

        final int headGp1 = 32;
        final int headGp2 = headGp1 + reservation;
        final int headGp3 = headGp2 + reservation;


        try (RandomAccessFile raf = new RandomAccessFile("h2q1.dat", "rw")) {
            // 分区起始位置,需要按照右侧数字写入文件头
            int partition1Start = headGp1;
            int partition2Start = headGp2;
            int partition3Start = headGp3;
            int partitionStart;
            // 数对数量信息，需要写入文件头
            int pairs_num;
            int pairs_group1;
            int pairs_group2;
            int pairs_group3;
            int i = 0;

            while (d1 <= possibility || d2 <= possibility) {
                // 生成两个不同的0.0到1.0之间的double数值对
                d1 = random.nextDouble();
                d2 = random.nextDouble();

                // 决定分区
                partitionStart = partition1Start;
                if (d1 > 0.72 && d2 > 0.72) {
                    partitionStart = partition3Start;
                    partition3Start = partition3Start + 16;
                } else if (d1 > 0.46 && d2 > 0.46) {
                    partitionStart = partition2Start;
                    partition2Start = partition2Start+16;
                } else
                {
                    partition1Start = partition1Start + 16;
                }


                // 将文件指针定位,转换d1和d2为字节数组
                raf.seek(partitionStart);
                byte[] d1Bytes = DoubletoByteArray(d1);
                raf.write(d1Bytes);

                raf.seek(partitionStart + 8);
                byte[] d2Bytes = DoubletoByteArray(d2);
                raf.write(d2Bytes);

                //System.out.println("d1: " + d1 + ", d2: " + d2 + "times :" + i);
                i = i+1;

            }
            pairs_group1 = (partition1Start - headGp1) / 16;
            pairs_group2 = (partition2Start - headGp2) / 16;
            pairs_group3 = (partition3Start - headGp3) / 16;
            pairs_num = pairs_group1 + pairs_group2 + pairs_group3;

            raf.seek(0);
            byte[] pgn = InttoByteArray(pairs_num);
            raf.write(pgn);

            raf.seek(4); // 指定不同的偏移量
            byte[] pg1 = InttoByteArray(pairs_group1);
            raf.write(pg1);

            raf.seek(8); // 指定不同的偏移量
            byte[] pos1 = InttoByteArray(32);
            raf.write(pos1);

            raf.seek(12); // 指定不同的偏移量
            byte[] pg2 = InttoByteArray(pairs_group2);
            raf.write(pg2);

            raf.seek(16); // 指定不同的偏移量
            byte[] pos2 = InttoByteArray(1632);
            raf.write(pos2);

            raf.seek(20); // 指定不同的偏移量
            byte[] pg3 = InttoByteArray(pairs_group3);
            raf.write(pg3);

            raf.seek(24); // 指定不同的偏移量
            byte[] pos3 = InttoByteArray(3232);
            raf.write(pos3);

        } catch (IOException e) {
            e.printStackTrace();
        }


        // 记录程序结束时间（纳秒）
        long endTime = System.nanoTime();

        // 计算程序运行时长（毫秒）
        long duration = (endTime - startTime);
        System.out.println("保存程序运行时长（纳秒）: " + duration + "  = " + duration/1000000 + "(毫秒)");
    }

    // 辅助方法：将double转换为字节数组
    private static byte[] DoubletoByteArray(double value) {
        long longBits = Double.doubleToLongBits(value);
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) ((longBits >> (8 * i)) & 0xFF);
        }
        return bytes;
    }

    private static byte[] InttoByteArray(int value) {
        byte[] bytes = new byte[4]; // 整数有4个字节
        for (int i = 0; i < 4; i++) {
            bytes[i] = (byte) (value >> (i * 8)); // 将每个字节从整数中提取
        }
        return bytes;
    }

}
