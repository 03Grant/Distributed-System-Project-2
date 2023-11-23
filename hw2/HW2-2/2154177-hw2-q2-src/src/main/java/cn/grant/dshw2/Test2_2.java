package cn.grant.dshw2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;

public class Test2_2 {
    public static void main(String[] args) {
        //String[] urls = {
        //        "https://space.bilibili.com/481701670?from=search&seid=3712209867347899427",
               // "http://www.pku.cn",
               // "http://www.sina.com.cn",
               // "http://www.mit.edu",
       // };
        disableSSLVerification();
        String h = "https://docs.google.com/spreadsheets/d/1cLM8I9dTApkIndoBRHtpu9PSp4wwSoYnAoCP4-mWxXU/edit#gid=0";
        //String[] urls = filter(h);


        //for (String url : urls) {
        //    System.out.println(url);
       // }
        try {
            Document doc = Jsoup.connect(h).get();
            Elements links = doc.select("a[href]");

            System.out.println("URLs found on: " + h);
            for (Element link : links) {
                System.out.println(link.absUrl("href"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

                // System.out.println(absoluteUrl);
                if (absoluteUrl.length()>2200)   // 太长的url舍弃。因为可能是一个专门的网页，没有外部的url
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
                            //flag = false;
                            //break;
                        }
                    }

                    // 将主机名字写入
                    urls[i] = absoluteUrl;
                    Hosts[i] = linkHost;
                    i++;

                    // 此处比较全局的主机名
                    // TODO
                }

                flag = true;
                if (i >= 6){
                    break;   //只选择符合条件的6个出来
                }
            }
            return urls;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private static boolean bannedWebCheck(String url){

        return false;
    }

    // 输入一个url，获取其主机地址的最右边三位（如无三位则认为该域名很宽泛，可以重复使用）
    private static String getBaseUrl(String url) {
        try {
            URL parsedUrl = new URL(url);
            String[] parts = parsedUrl.getHost().split("\\.");

            //
            if (parts.length <= 3)
                return "#";

            // 获取主机部分的最右边三个部分
            if (parts.length > 3) {
                return parts[parts.length - 3] + "." + parts[parts.length - 2] + "." + parts[parts.length - 1];
            } else {
                return parsedUrl.getHost();
            }
        } catch (MalformedURLException e) {
            // e.printStackTrace();
            return "";
        }
    }
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

