package com.smile.io.bio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author hjw
 * @date 2021/6/1 18:01
 * <p>
 * 处理用户输入的消息
 */
public class UserInputHandler implements Runnable {

    private ChatClient chatClient;

    public UserInputHandler(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    @Override
    public void run() {
        try {

            //输入信息
            BufferedReader consoleReader = new BufferedReader(
                    new InputStreamReader(System.in)
            );

            while (true) {
                String inputMsg = consoleReader.readLine();
                //向服务器发送消息
                chatClient.sendMsg(inputMsg);

                //检查用户是否准备退出
                if (chatClient.readyToQuit(inputMsg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
