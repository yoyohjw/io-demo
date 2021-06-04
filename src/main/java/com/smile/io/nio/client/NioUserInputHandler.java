package com.smile.io.nio.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author hjw
 * @date 2021/6/1 18:01
 * <p>
 * 处理用户输入的消息
 */
public class NioUserInputHandler implements Runnable {

    private NioChatClient nioChatClient;

    public NioUserInputHandler(NioChatClient nioChatClient) {
        this.nioChatClient = nioChatClient;
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
                nioChatClient.send(inputMsg);

                //检查用户是否准备退出
                if (nioChatClient.readyToQuit(inputMsg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
