package com.smile.io.nio.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author hjw
 * @date 2021/6/1 17:35
 * <p>
 * 客户端主要方法及启用
 */
public class NioChatClient {

    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;


    private String host;
    private int port;
    /**
     * 客户端通道
     **/
    private SocketChannel client;
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");


    public NioChatClient() {
        this(DEFAULT_SERVER_HOST, DEFAULT_PORT);
    }

    public NioChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void handles(SelectionKey key) throws IOException {
        //连接就绪 - connect事件
        if (key.isConnectable()) {
            SocketChannel client = (SocketChannel) key.channel();
            //判断连接是否建立完成
            if (client.isConnectionPending()) {
                client.finishConnect();
                //处理用户输入信息
                new Thread(new NioUserInputHandler(this)).start();
            }
            client.register(selector, SelectionKey.OP_READ);
        }
        //read事件 - 服务器转发消息
        if (key.isReadable()) {
            //获取消息
            SocketChannel client = (SocketChannel) key.channel();
            String msg = receive(client);
            if (msg.isEmpty()) {
                //服务器异常
                close(selector);
            } else {
                System.out.println(msg);
            }
        }

    }

    /**
     * 接收消息
     * @param client
     * @return
     * @throws IOException
     */
    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while (client.read(rBuffer) > 0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    /**
     * 发送消息
     * @param msg
     */
    public void send(String msg) throws IOException {
        if (msg.isEmpty()) {
            return;
        }

        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();

        while (wBuffer.hasRemaining()) {
            client.write(wBuffer);
        }

        //检查用户是否准备退出
        if (readyToQuit(msg)) {
            close(selector);
        }

    }

    /**
     * 检查是否退出
     *
     * @param msg
     * @return
     */
    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            client = SocketChannel.open();
            //改为非阻塞
            client.configureBlocking(false);

            selector = Selector.open();
            //注册关于connect事件的 监测
            client.register(selector, SelectionKey.OP_CONNECT);
            //向服务端发起连接请求
            client.connect(new InetSocketAddress(host, port));

            while (true) {
                //当select()方法有返回时，说明有事件触发了
                selector.select();
                //在selector上监听到的触发的所有事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    handles(key);
                }
                selectionKeys.clear();
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClosedSelectorException e) {
            //不处理
        } finally {
            close(selector);
        }
    }


    public static void main(String[] args) {
        NioChatClient nioChatClient = new NioChatClient();
        nioChatClient.start();
    }

}
