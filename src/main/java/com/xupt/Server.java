package com.xupt;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @author maxu
 * @date 2019/4/1
 */
public class Server {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    /** 服务器端口 **/
    private static final int PORT = 9999;

    public Server() {
        try {
            selector = Selector.open();
            /** 打开监听通道 **/
            serverSocketChannel = ServerSocketChannel.open();
            /** 绑定端口 **/
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            /** 配置非阻塞模式 **/
            serverSocketChannel.configureBlocking(false);
            /** 将选择器绑定到监听通道上 **/
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            System.out.println("服务器启动...");
            while (true) {
                int count = selector.select(); // 获取就绪channel
                if (count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        // 监听accept
                        if (key.isAcceptable()) {
                            SocketChannel socket = serverSocketChannel.accept();
                            socket.configureBlocking(false);
                            // 注册到选择器上并监听read
                            System.out.println(socket.getRemoteAddress().toString().substring(1)+"上线了...");
                            socket.register(selector, SelectionKey.OP_READ);
                            // 将此对应的channel设置为accept，接着其它客户端请求
                            key.interestOps(SelectionKey.OP_ACCEPT);
                        }
                        if (key.isReadable()) {
                            read(key);// 读取客户端发来的数据
                        }
                        // 删除当前的key防止重复处理
                       iterator.remove();
                    }
                } else {
                    System.out.println("还没有...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        SocketChannel channel = null;
        try {
            // 得到关联的通道
            channel = (SocketChannel) key.channel();
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            int count = channel.read(byteBuffer);
            if (count > 0) {
                String msg = new String(byteBuffer.array());
                System.out.println(msg);
                key.interestOps(SelectionKey.OP_READ);
                // 发送广播
                BroadCast(channel, msg);
            }
            byteBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                key.cancel(); // 取消注册
                System.out.println(channel.getRemoteAddress().toString().substring(1)+"当前客户端关下线了");
                // 关闭通道
                channel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void BroadCast(SocketChannel channel, String msg) throws IOException {
        for (SelectionKey key : selector.keys()) {
            Channel targetChannel = key.channel();
            // 排除自身
            if (targetChannel instanceof SocketChannel && targetChannel != channel) {
                SocketChannel dest = (SocketChannel) targetChannel;
                // 把数据放入缓冲区中
                ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
                // 向通道中写数据
                dest.write(byteBuffer);
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
