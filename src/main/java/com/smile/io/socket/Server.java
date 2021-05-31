package com.smile.io.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author hjw
 * @date 2021/5/31 14:14
 * <p>
 * 服务端
 */
public class Server {

    public static void main(String[] args) {
        //退出常量
        final String QUIT = "quit";

        final int DEFAULT_PORT = 8888;
        ServerSocket serverSocket = null;

        try {
            //绑定监听端口
            serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口：" + DEFAULT_PORT);

            while (true) {
                //等待客户端链接 - accept()方法是阻塞的
                Socket socket = serverSocket.accept();
                System.out.println("客户端[" + socket.getPort() + "] 已连接");

                //服务端读取客户端发来的信息
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                //服务端发送数据
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())
                );

                //读取客户端发送来的消息 - 循环读取，客户端没退出之前 都是会有消息发送来的
                String msg = null;
                while (null != (msg = reader.readLine())) {
                    //打印客户端发送的消息
                    System.out.println("客户端[" + socket.getPort() + "]：" + msg);
                    //回复客户端发送的消息
                    writer.write("服务器：" + msg + "\n");
                    writer.flush();

                    //查看客户端是否退出
                    if (QUIT.equals(msg)) {
                        System.out.println("客户端[" + socket.getPort() + "]已断开");
                        break;
                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                    System.out.println("服务端关闭");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
