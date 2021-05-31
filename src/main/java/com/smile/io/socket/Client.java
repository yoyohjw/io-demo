package com.smile.io.socket;

import java.io.*;
import java.net.Socket;

/**
 * @author hjw
 * @date 2021/5/31 16:01
 *
 * 客户端
 *
 */
public class Client {

    public static void main(String[] args) {
        //退出常量
        final String QUIT = "quit";
        //地址
        final String DEFAULT_SERVER_HOST = "127.0.0.1";
        //端口
        final int DEFAULT_PORT = 8888;
        Socket socket = null;

        try {
            //创建Socket
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_PORT);

            //客户端读取服务端发来的信息
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            //向服务端发送消息
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );

            //输入信息
            BufferedReader consoleReader = new BufferedReader(
                    new InputStreamReader(System.in)
            );
            while (true) {
                String inputMsg = consoleReader.readLine();

                //发送消息给服务端
                writer.write(inputMsg + "\n");
                writer.flush();

                //读取服务端返回的消息
                String readerMsg = reader.readLine();
                if (null != readerMsg) {
                    System.out.println(readerMsg);
                }

                if (QUIT.equals(inputMsg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            /**
             * 这里关闭 socket还有一种方式 就是关闭 writer， 因为关闭了writer相当于关闭了socket
             */

            if (null != socket) {
                try {
                    socket.close();
                    System.out.println("客户端关闭");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


}
