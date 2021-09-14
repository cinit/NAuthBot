package cc.ioctl.nauthbot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting bot...");
        PrivateKeys.init();
        //System.out.printf("Token is \"%s\"\n", PrivateKeys.API_KEY);
        System.out.printf("Token length %d\n", PrivateKeys.API_KEY.length());
        System.out.printf("Token hash %d\n", PrivateKeys.API_KEY.hashCode());
        //System.exit(0);
        ApiContextInitializer.init();
        DefaultBotOptions opt = ApiContext.getInstance(DefaultBotOptions.class);
        {
            DefaultBotOptions.ProxyType proto = null;
            String protoName = null;
            String addr = null;
            int port = 0;
            boolean useProxy = false;
            for (String arg : args) {
                if (arg.startsWith("--proxy=")) {
                    useProxy = true;
                    String expr = arg.substring(8);
                    switch (protoName = expr.split("://")[0].toLowerCase()) {
                        case "http":
                            proto = DefaultBotOptions.ProxyType.HTTP;
                            break;
                        case "socks4":
                            proto = DefaultBotOptions.ProxyType.SOCKS4;
                            break;
                        case "socks5":
                            proto = DefaultBotOptions.ProxyType.SOCKS5;
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown proxy protocol: " + protoName);
                    }
                    addr = expr.split("://")[1].split(":")[0];
                    port = Integer.parseInt(expr.split("://")[1].split(":")[1]);

                }
            }
            if (!useProxy) {
                System.out.println("Proxy not enabled, use \"--proxy=proto://addr:port\" to enable it");
            } else {
                System.out.printf("Using proxy %s://%s:%d\n", protoName, addr, port);
                opt.setProxyType(proto);
                opt.setProxyHost(addr);
                opt.setProxyPort(port);
            }
        }
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        BotSession nauthSession;
        opt.setAllowedUpdates(Arrays.asList("message", "inline_query", "chosen_inline_result", "callback_query"));
        try {
            nauthSession = telegramBotsApi.registerBot(new NAuthBot(opt));
            Scanner scanner = new Scanner(System.in);
            String line;
            while ((line = scanner.nextLine()) != null) {
                if ((line.startsWith("exit") || line.startsWith("stop"))) {
                    break;
                }
            }
            System.out.println("Shutting down...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                        System.err.println("Took too long to stop, killed.");
                        System.exit(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            nauthSession.stop();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        System.out.println("Stopped.");
        System.exit(0);
    }
}
