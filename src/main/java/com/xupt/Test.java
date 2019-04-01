package com.xupt;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author maxu
 * @date 2019/4/1
 */
public class Test {
    public static void main(String[] args) throws IOException {
        final Client client = new Client();

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        client.receive();
                        Thread.sleep(3000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String msg = scanner.nextLine();
            client.send(msg);
        }
    }
}
