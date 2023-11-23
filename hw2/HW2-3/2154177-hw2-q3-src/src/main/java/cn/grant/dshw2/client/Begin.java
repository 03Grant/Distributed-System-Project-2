package cn.grant.dshw2.client;

public class Begin {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Begin <command>");
            return;
        }

        String command = args[0];
        switch (command) {
            // 在当前文件夹需要填写./
            case "get": // 两个参数：下载位置的路径 and 想要下载的文件名
                directLoadClient.main(args);
                break;
            case "put": // 两个参数：上传文件位置的路径 and 想要上传的文件名
                directPutClient.main(args);
                break;
            case "cget": // 两个参数：下载位置的路径 and 想要下载的文件名
                relayLoadClient.main(args);
                break;
            case "cput": // 两个参数：上传文件位置的路径 and 想要上传的文件名
                relayPutCilent.main(args);
                break;
            case "check": // 0个参数或1个参数：需要check文件名
                String[] cmd = new String[2];
                if(args.length == 1){
                    cmd[1] = "";
                }
                else{
                    cmd[1] = args[1];
                }

                checkClient.main(cmd);
                break;
            default:
                System.out.println("Invalid command: " + command);
                break;
        }
    }
}

