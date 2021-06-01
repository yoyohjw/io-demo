package com.smile.io.bio.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hjw
 * @date 2021/6/1 16:07
 *
 * 服务端主要方法及启动
 *
 */
public class ChatServer {

    private final int DEFAULT_PORT = 8888;
    private final String QUIT = "quit";

    private ServerSocket serverSocket;
    /**保存在线客户相关消息**/
    private Map<Integer, Writer> connectedClient;

    public ChatServer(){
        connectedClient = new HashMap<>();
    }

    /**
     * 添加客户端到客户MAP
     * @param socket
     *
     *
     * 这里只做演示，为了保障线程安全使用性能较差的synchronized - 实际开发可以使用Lock, ConcurrentHashMap保证安全
     */
    public synchronized void addClient(Socket socket) throws IOException {
        if (null != socket) {
            //以端口为key
            int port = socket.getPort();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );

            connectedClient.put(port, writer);
            System.out.println("客户端[" + port + "]连接到服务器");
        }
    }


    /**
     * 用户是否准备退出
     * @param msg
     * @return
     */
    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }


    /**
     * 客户端下线
     * @param socket
     *
     * 这里只做演示，为了保障线程安全使用性能较差的synchronized - 实际开发可以使用Lock, ConcurrentHashMap保证安全
     */
    public synchronized void removeClient(Socket socket) throws IOException {
        if (null != socket) {
            int port = socket.getPort();
            if (connectedClient.containsKey(port)) {
                //需要关闭输出流
                connectedClient.get(port).close();
            }
            connectedClient.remove(port);
            System.out.println("客户端[" + port + "]从服务器断开连接");

        }
    }


    /**
     * 转发消息给其它在线客户端
     * @param socket
     * @param msg
     *
     *
     */
    public synchronized void forwardMsg(Socket socket, String msg) throws IOException {
        int msgPort = socket.getPort();
        //遍历map - 不包括发送消息的socket
        for (int port : connectedClient.keySet()) {
            if (port != msgPort) {
                //转发
                Writer writer = connectedClient.get(port);
                writer.write("客户端[" + port + "]:" + msg);
                writer.flush();
            }
        }

    }

    /**
     * 启动服务端
     * @throws IOException
     */
    public void start() {
        try {
            //绑定监听端口
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("服务器启动，监听端口：" + DEFAULT_PORT);

            while (true) {
                //等待客户端连接
                Socket socket = serverSocket.accept();
                //创建handler的线程
                new Thread(new ChatHandler(this, socket)).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭serverSocket
            close(serverSocket);
        }

    }

    /**
     * 关闭serverSocket
     * @param serverSocket
     */
    public void close(ServerSocket serverSocket) {
        if (null != serverSocket) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }


}
