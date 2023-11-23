package cn.grant.dshw2.server;
import cn.grant.dshw2.IPsetting;

import java.util.HashMap;
import java.util.Map;
public class storageSetting {
    public static Map<String, String> serverStorageMap = new HashMap<>();

    static {
        // 添加服务器IP与文件夹名字的映射
        serverStorageMap.put(IPsetting.IP1, "Ware/fileWare1/");
        serverStorageMap.put(IPsetting.IP2, "Ware/fileWare2/");
        serverStorageMap.put(IPsetting.IP3, "Ware/fileWare3/");
    }
}
