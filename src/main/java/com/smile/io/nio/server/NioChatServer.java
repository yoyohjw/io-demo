package com.smile.io.nio.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @author hjw
 * @date 2021/6/1 16:07
 * 服务端主要方法及启动
 */
public class NioChatServer {

    private static final int DEFAULT_PORT = 8888;
    private static final String QUIT = "quit";
    private static final int BUFFER = 1024;

    /**
     * 通道
     **/
    private ServerSocketChannel server;
    /**
     * 监视Channel
     **/
    private Selector selector;
    /**
     * 和Channel配合使用  从通道读取数据
     **/
    private ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
    /**
     * 和Channel配合使用  向通道写数据
     **/
    private ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
    private Charset charset = Charset.forName("UTF-8");
    private int port;

    public NioChatServer() {
        this(DEFAULT_PORT);
    }

    public NioChatServer(int port) {
        this.port = port;

    }


    /**
     * 用户是否准备退出
     *
     * @param msg
     * @return
     */
    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }


    private void handles(SelectionKey key) throws IOException {
        //ACCEPT事件 - 和客户端建立连接
        if (key.isAcceptable()) {
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            //返回客户端通道
            SocketChannel client = server.accept();
            //转为非阻塞式调用
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println("客户端[" + client.socket().getPort() + "]已连接");
        }
        //READ事件 - 客户端发送消息给服务器端
        if (key.isReadable()) {
            SocketChannel client = (SocketChannel) key.channel();
            String fwdMsg = receive(client);
            if (fwdMsg.isEmpty()) {
                //如果接收到的消息为空，则可能客户端异常
                key.cancel();
                //通知selector - 刷新状态
                selector.wakeup();
            } else {
                System.out.println("客户端[" + client.socket().getPort() + "]:" + fwdMsg);
                //转发消息
                forwardMessage(client, fwdMsg);

                //检查用户是否退出
                if (readyToQuit(fwdMsg)) {
                    //解除监听
                    key.cancel();
                    selector.wakeup();
                    System.out.println("客户端[" + client.socket().getPort() + "]断开连接");
                }
            }

        }
    }

    /**
     * 转发
     *
     * @param client
     * @param fwdMsg
     * @throws IOException
     */
    private void forwardMessage(SocketChannel client, String fwdMsg) throws IOException {
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            //确认key是有效的 并且是 socketChannel
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                //不是发送消息的客户端
                if (!client.equals(key.channel())) {
                    wBuffer.clear();
                    String head = "客户端[" + client.socket().getPort() + "]:";
                    wBuffer.put(charset.encode(head + fwdMsg));
                    wBuffer.flip();
                    while (wBuffer.hasRemaining()) {
                        ((SocketChannel) key.channel()).write(wBuffer);
                    }
                }
            }
        }
    }

    /**
     * 获取消息
     *
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
     * 关闭资源
     *
     * @param closeable
     */
    public void close(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 启动服务端
     */
    public void start() {
        try {
            server = ServerSocketChannel.open();
            //设置为非阻塞式调用
            server.configureBlocking(false);
            //绑定监听端口
            server.socket().bind(new InetSocketAddress(port));

            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口：" + port + "...");

            while (true) {
                //当select()方法有返回时，说明有事件触发了
                selector.select();
                //在selector上监听到的触发的所有事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                for (SelectionKey key : selectionKeys) {
                    //处理被触发的事件
                    handles(key);
                }
                selectionKeys.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(selector);
        }

    }


    public static void main(String[] args) {
        NioChatServer nioChatServer = new NioChatServer();
        nioChatServer.start();
    }


}
