package com.smile.io.bio.client;

import java.io.*;
import java.net.Socket;

/**
 * @author hjw
 * @date 2021/6/1 17:35
 *
 * 客户端主要方法及启用
 */
public class ChatClient {

    private final String DEFAULT_SERVER_HOST = "127.0.0.1";
    private final int DEFAULT_PORT = 8888;
    private final String QUIT = "quit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /**
     *  发送消息给服务器
     * @param msg
     */
    public void sendMsg(String msg) throws IOException {
        if (!socket.isOutputShutdown()) {
            writer.write(msg + "\n");
            writer.flush();
        }
    }


    /**
     * 从服务器 读取信息
     * @return
     */
    public String getMsg() throws IOException {
        String msg = null;

        if (!socket.isInputShutdown()) {
            msg = reader.readLine();
        }
        return msg;
    }

    /**
     * 检查是否退出
     * @param msg
     * @return
     */
    public boolean readyToQuit(String msg) {
        return QUIT.equals(msg);
    }

    public void close() {
        if (writer != null) {
            try {
                System.out.println("关闭socket");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        try {
            //创建socket
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_PORT);

            //创建发送和接收信息的IO流
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())
            );

            //处理用户输入 - UserInputHandler
            new Thread(new UserInputHandler(this)).start();

            //读取服务器转发的消息
            String msg = null;
            while ((msg = getMsg()) != null) {
                System.out.println(msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }


    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }

}
