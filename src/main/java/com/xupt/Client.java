package com.xupt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author maxu
 * @date 2019/4/1
 */
public class Client {
    private final String HOST = "127.0.0.1";
    private final int PORT = 9999;
    private Selector selector;
    private SocketChannel socketChannel;
    private String userName;

    public Client() throws IOException{
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
        socketChannel.configureBlocking(false);
        // 注册选择器
        socketChannel.register(selector, SelectionKey.OP_READ);
        userName = socketChannel.getLocalAddress().toString().substring(1);
    }

    public void send(String msg) throws IOException{
        if (msg.equalsIgnoreCase("bye")) {
            socketChannel.close();
            socketChannel = null;
            return;
        }
        try {
            socketChannel.write(ByteBuffer.wrap(msg.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receive() throws IOException{
        try {
            int readyChannels = selector.select();
            if (readyChannels > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        // 得到关联的通道
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        socketChannel.read(byteBuffer);
                        String msg = new String(byteBuffer.array());
                        System.out.println(msg.trim());
                    }
                    iterator.remove();
                }
            } else {
                System.out.println("没有人....");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
