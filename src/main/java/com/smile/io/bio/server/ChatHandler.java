package com.smile.io.bio.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @author hjw
 * @date 2021/6/1 16:52
 *
 * 服务端单独的处理线程
 *
 */
public class ChatHandler implements Runnable {

    private ChatServer chatServer;
    private Socket socket;

    public ChatHandler(ChatServer chatServer, Socket socket) {
        this.chatServer = chatServer;
        this.socket = socket;
    }


    @Override
    public void run() {
        try {
            //添加新上线用户
            chatServer.addClient(socket);

            //读取用户发送的消息
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            String msg = null;
            while((msg = bufferedReader.readLine()) != null) {
                System.out.println("客户端[" + socket.getPort() + "]:" + msg);

                //将消息转发给 其它在线用户
                chatServer.forwardMsg(socket, msg + "\n");

                //检查用户是否准备退出
                if (chatServer.readyToQuit(msg)) {
                    break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //下线
                chatServer.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
