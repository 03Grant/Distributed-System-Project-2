import java.io.IOException;
import java.io.RandomAccessFile;

public class Decode {
    public static void main(String[] args){
        // 记录程序开始时间（纳秒）
        long startTime = System.nanoTime();
        double[] Gp1 = null;
        double[] Gp2 = null;
        double[] Gp3 = null;
        int[] info = new int[7];// 文件头数据，7个

        try(RandomAccessFile raf = new RandomAccessFile("h2q1.dat", "r")){
            raf.seek(0); // 从头开始读取文件头，共有7个数字

            for (int i = 0; i < 7; i++) {
                int value = 0;
                for (int j = 0; j < 4; j++) {
                    int byteValue = raf.read(); // 从文件读取一个字节
                    value = (byteValue & 0xFF) << (8 * j) | value; // 将字节合并成整数
                }
                info[i] = value;
            }
            // 0是总数，1是Gp1数对个数，2是Gp1起始位置......
            // 读取Gp1数据
            raf.seek(info[2]);
            Gp1 = new double[info[1] * 2];
            for (int i = 0; i < info[1] * 2; i++) {
                long bits = 0;
                for (int j = 0; j < 8; j++) {
                    int byteValue = raf.read(); // 从文件读取一个字节
                    bits = (long) (byteValue & 0xFF) << (8 * j) | bits; // 将字节合并成 long 值
                }
                Gp1[i] = Double.longBitsToDouble(bits); // 将 long 值转换为 double （也许可以优化）
            }

            // 读取Gp2的值
            raf.seek(info[4]);
            Gp2 = new double[info[3] * 2];
            for (int i = 0; i < info[3] * 2; i++) {
                long bits = 0;
                for (int j = 0; j < 8; j++) {
                    int byteValue = raf.read(); // 从文件读取一个字节
                    bits = (long) (byteValue & 0xFF) << (8 * j) | bits; // 将字节合并成 long 值
                }
                Gp2[i] = Double.longBitsToDouble(bits); // 将 long 值转换为 double （也许可以优化）
            }

            // 读取Gp3的值
            raf.seek(info[6]);
            Gp3 = new double[info[5] * 2];
            for (int i = 0; i < info[5] * 2; i++) {
                long bits = 0;
                for (int j = 0; j < 8; j++) {
                    int byteValue = raf.read(); // 从文件读取一个字节
                    bits = (long) (byteValue & 0xFF) << (8 * j) | bits; // 将字节合并成 long 值
                }
                Gp3[i] = Double.longBitsToDouble(bits); // 将 long 值转换为 double （也许可以优化）
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }

        // 记录程序结束时间（纳秒）
        long endTime = System.nanoTime();

        // 计算程序运行时长（毫秒）
        long duration = (endTime - startTime);
        System.out.println("读取程序运行时长（纳秒）: " + duration + "  = " + duration/1000000 + "(毫秒)");
        int numberNum = 0;
        int pairNum = 0;
        if(Gp1 != null && Gp2!=null && Gp3!=null){
            System.out.println("结果如下：\n");
            System.out.println("数对总数：" + info[0]);
            System.out.println("Gp1对数：" + info[1]);
            System.out.println("Gp1详细：\n");
            for (int i = 0; i < info[1] * 2; i += 2) {
                double d1 = Gp1[i];
                double d2 = Gp1[i + 1];
                if(d1>0.5 && d2>0.5){
                    numberNum = numberNum+2;
                    pairNum = pairNum+1;
                }else if(d1>0.5||d2>0.5){
                    numberNum = numberNum+1;
                }



                System.out.println("d1: " + d1 + ", d2: " + d2);
            }
            System.out.println("Gp2对数：" + info[3]);
            System.out.println("Gp2详细：\n");
            for (int i = 0; i < info[3] * 2; i += 2) {
                double d1 = Gp2[i];
                double d2 = Gp2[i + 1];

                if(d1>0.5 && d2>0.5){
                    numberNum = numberNum+2;
                    pairNum = pairNum+1;
                }else if(d1>0.5||d2>0.5){
                    numberNum = numberNum+1;
                }

                System.out.println("d1: " + d1 + ", d2: " + d2);
            }
            System.out.println("Gp3对数：" + info[5]);
            System.out.println("Gp3详细：\n");
            for (int i = 0; i < info[5] * 2; i += 2) {
                double d1 = Gp3[i];
                double d2 = Gp3[i + 1];

                if(d1>0.5 && d2>0.5){
                    numberNum = numberNum+2;
                    pairNum = pairNum+1;
                }else if(d1>0.5||d2>0.5){
                    numberNum = numberNum+1;
                }

                System.out.println("d1: " + d1 + ", d2: " + d2);
            }
            System.out.println("大于0.5的数字个数为：" + numberNum);
            System.out.println("大于0.5的数对个数为：" + pairNum);

        }
    }
}
