package cn.grant.dshw2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.URL;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;



public class visitHttp {

    private static String[] url_1;
    private static String[] url_2;
    private static String[] url_3;
    private static String[] url_4;
    private static String[][] urls;

    private static int[] url_1_repeat;
    private static int[] url_2_repeat;
    private static int[] url_3_repeat;
    private static int[] url_4_repeat;
    private static int[][] urls_repeat;
    private static long urltime[] = {0,0,0,0};

    public static void main(String[] args) {

        disableSSLVerification();

        int root = 4;

        url_1 = new String[(1 + 6 + 6*6 + 6*6*6)]; // 指定数组大小,三跳共有 4 *(1 + 6 + 6*6 + 6*6*6)
        url_1[0] = "http://www.tongji.edu.cn";
        //url_1[0] = "http://www.mit.edu";

        url_2 = new String[(1 + 6 + 6*6 + 6*6*6)];
        url_2[0] = "http://www.pku.edu.cn";

        url_3 = new String[(1 + 6 + 6*6 + 6*6*6)];
        url_3[0] = "http://www.sina.com.cn";

        url_4 = new String[(1 + 6 + 6*6 + 6*6*6)];
        url_4[0] = "http://www.mit.edu";

        urls = new String[][] { url_1, url_2, url_3, url_4 };

        url_1_repeat = new int[(1 + 6 + 6*6 + 6*6*6)];
        url_2_repeat = new int[(1 + 6 + 6*6 + 6*6*6)];
        url_3_repeat = new int[(1 + 6 + 6*6 + 6*6*6)];
        url_4_repeat = new int[(1 + 6 + 6*6 + 6*6*6)];
        urls_repeat = new int[][]{url_1_repeat,url_2_repeat,url_3_repeat,url_4_repeat};

                // 生成url数组
        generate(root);

        //打印结果
        for(int i = 0; i < root; i++) {
            int edge = 0;
            System.out.println("\n以 "+urls[i][0] + " 为根节点的外部url如下：");
            for(int j = 0; j < 259; j++) {
                if (urls[i][j] != null && !urls[i][j].isEmpty()) {
                    System.out.println(j + " " + urls[i][j]);
                    if(j!=0){
                        System.out.println("存在的url中，有" + (urls_repeat[i][j]+1) + "条url会链接到此");
                    }
                    edge = edge + 1;
                }
                //System.out.println("\n以 "+urls[i][0] + " 为根节点的外部url如下：");
            }
        }
        MaxValueIndex maxValueIndex = findMaxValueAndIndex(urls_repeat);

        System.out.println("\n因为搜寻的url不允许有重复，所以存在的节点入度都为 1 ");
        System.out.println("但可以到达 " + urls[maxValueIndex.rowIndex][maxValueIndex.colIndex] + " 的url数量最多为"+maxValueIndex.value+"条\n");


        //打印时间消耗：
        for(int i = 0; i < root; i ++)
        {
            System.out.println(urls[i][0] +"根节点消耗的时间为："+(urltime[i]/1000) +"秒");
        }
        // 构建数组
        int[][] G = makeGraph(root);

        for(int i = 0; i < root; i ++)
        {
            visual visualInstance = new visual(urls[i][0]);
            visualInstance.setGarray(G[i]);
            visualInstance.main(urls[i][0]);
        }

        System.out.println("Finished");

    }

    //画树形图
    private static int[][] makeGraph(int root){
        int[][] graph = new int[root][259];
        for(int i = 0; i < root; i++) {
            for(int j = 0; j < 259; j++) {
                if (urls[i][j] != null && !urls[i][j].isEmpty()) {
                    graph[i][j] = 1;
                }
                else{
                    graph[i][j] = 0;
                }
            }
        }
        return graph;
    }

    //从根目录开始进行扫描
    private static void generate(int root){
        int beginPos = 0;
        int currentPos = 1;    //用于记录url_i[j]的位置j,这决定了后面的内容要放在哪里

        for (int hop = 0; hop < 3; hop++) {
            for (int i = 0; i < root; i++) {
                //计算时间
                long begin = System.currentTimeMillis();
                scanAndGet(beginPos, currentPos, i);
                long end = System.currentTimeMillis();
                urltime[i] = urltime[i] + end - begin;
            }

            int gap = currentPos - beginPos;
            currentPos = currentPos + gap * 6;
            beginPos = beginPos + gap;
            //System.out.println("beginPos: " + beginPos + "  currentPos: "+ currentPos);
        }
    }

    //扫描已有的url，尝试从已有的url里得到更多的外部url
    private static void scanAndGet(int beginPos,int currentPos,int root){
        // 将过滤后的URL数组添加到原始数组中
        int tempPos;
        tempPos = currentPos;
        for (int i = beginPos; i < currentPos; i++) {
            String url = urls[root][i];
           //System.out.println(i + ": " + url+"  now tempPos:" + tempPos);
            if (url == null || url.isEmpty())
                continue;
            // 使用 filter 方法获取额外的 URL
            String[] filteredUrls = filter(url);
            if (filteredUrls == null){
                continue;
            }
            // 将过滤后的 URL 添加到临时数组中
            for (String filteredUrl : filteredUrls) {
                //System.out.print(tempPos + " ");
                urls[root][tempPos++] = filteredUrl;
            }
        }
    }

    // 获取网页上的url集合，并按要求进行过滤. filter会返回一些空串值，因为有的页面不可避免地不足6个url
    private static String[] filter(String url){
        try {
            Document doc = Jsoup.connect(url).get();
            Elements links = doc.select("a[href]");
            String[] urls = new String[]{"","","","","",""};
            System.out.println("URLs found on: " + url);
            // 执行过滤操作
            int i = 0; // 控制每一个url的外部url数目
            boolean flag = true;  // 检测是否有重复
            String baseUrl = getBaseUrl(url); // 获取url[0]的主机部分的最右边三个部分
            // Hosts保存在一个搜索中出现过的的主机名
            String[] Hosts = new String[]{"","","","","",""};
            for (Element link : links) {
                String absoluteUrl = link.absUrl("href");

                if(compareUrl(absoluteUrl))
                   continue;


                if (absoluteUrl.endsWith(".pdf") || absoluteUrl.endsWith(".php")) {
                    // 如果链接指向PDF文件，跳过处理
                    continue;
                }
                // System.out.println(absoluteUrl);
                if (absoluteUrl.length()>120)   // 太长的url舍弃。因为可能是一个专门的网页，没有外部的url
                    continue;

                // 获取当前链接的主机部分的最右边三个部分并比较
                String linkHost = getBaseUrl(absoluteUrl);

                // 排除掉一些网站，因为需要登录导致登录界面没有任何外部url
                if (linkHost.isEmpty()||bannedWebCheck(linkHost)){
                    continue;
                }

                if (!linkHost.endsWith(baseUrl) || baseUrl.equals("#")) { // 检查最右边三个部分是否相同，若不相同，则放入待选项
                    // 此处先比较之前已有的的主机名
                    for(String Host : Hosts){
                        if (linkHost.equals(Host)){
                            flag = false;
                            break;
                        }
                    }
                    if(flag) {
                        // 将主机名字写入
                        urls[i] = absoluteUrl;
                        Hosts[i] = linkHost;
                        i++;
                    }
                }
                flag = true;
                if (i >= 6){
                    break;   //只选择符合条件的6个出来
                }
            }
            return urls;
        } catch (Exception e) {
            //e.printStackTrace();
            return null;
        }
    }

    private static boolean compareUrl(String url){
        for(int i = 0;i < 4;i++)
        {
            for(int j = 0;j < 1 + 6 + 6*6 + 6*6*6;j++)
            {
                if(urls[i][j] == null || urls[i][j].isEmpty()){
                    continue;
                }else{
                    // 如果之前有这样的url了
                    if(url.replace("/","").equals(urls[i][j].replace("/","")) && !url.isEmpty())
                    {
                        urls_repeat[i][j] = urls_repeat[i][j]+1;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // 禁用掉一些网站，不尝试抓取它们的内容，因为需要登录，导致无法抓取到任何外部url
    private static boolean bannedWebCheck(String url){
        if (url.equals("miit.gov.cn"))
            return true;
        if (url.equals("beijing.gov.cn"))
            return true;
        //if (url.equals("twitter.com"))
        //    return true;
        if (url.equals("www.facebook.com"))
            return true;
        if (url.equals("itrust.org.cn"))
            return true;
        //if (url.equals("people.com.cn"))
          //  return true;
        //if (url.equals("weibo.com"))
        //   return true;
        if (url.equals("miibeian.gov.cn"))
            return true;
        return false;
    }

    // 输入一个url，获取其主机地址的最右边三位（如无三位则认为该域名很宽泛，可以重复使用）
    private static String getBaseUrl(String url) {
        try {
            URL parsedUrl = new URL(url);
            String[] parts = parsedUrl.getHost().split("\\.");

            //
            //if (parts.length == 3)
             //   return "#";

            // 获取主机部分的最右边三个部分
            if (parts.length > 3) {
                return parts[parts.length - 3] + "." + parts[parts.length - 2] + "." + parts[parts.length - 1];
            } else {
                return parsedUrl.getHost();
            }
        } catch (Exception e) {
            // e.printStackTrace();
            return "";
        }
    }

    public static MaxValueIndex findMaxValueAndIndex(int[][] matrix) {
        int max = Integer.MIN_VALUE; // 初始化为最小整数值
        int rowIndex = -1;
        int colIndex = -1;

        // 遍历二维数组
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] > max) {
                    max = matrix[i][j]; // 找到更大的值，更新最大值
                    rowIndex = i; // 更新行索引
                    colIndex = j; // 更新列索引
                }
            }
        }

        return new MaxValueIndex(max, rowIndex, colIndex);
    }

    public static class MaxValueIndex {
        public int value;
        public int rowIndex;
        public int colIndex;

        public MaxValueIndex(int value, int rowIndex, int colIndex) {
            this.value = value;
            this.rowIndex = rowIndex;
            this.colIndex = colIndex;
        }
    }
    // 非功能性函数：禁用一些认证的协议
    private static void disableSSLVerification() {
        try {
            TrustManager[] trustAllCertificates = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



