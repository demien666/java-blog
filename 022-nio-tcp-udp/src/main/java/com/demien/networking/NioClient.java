package com.demien.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;


public class NioClient implements Runnable {

    private Selector selector;
    private BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));

    public static void main(String[] args) {
        NioClient clientThread = new NioClient();
        Thread thread = new Thread(clientThread);
        thread.start();
    }

        @Override
        public void run() {
            SocketChannel channel;
            try {
                selector = Selector.open();
                channel = SocketChannel.open();
                channel.configureBlocking(false);

                channel.register(selector, SelectionKey.OP_CONNECT);
                channel.connect(new InetSocketAddress(NioServer.ADDRESS, NioServer.PORT));

                System.out.println("Starting....");

                while (!Thread.interrupted()){

                    selector.select(1000);

                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                    while (keys.hasNext()){
                        SelectionKey key = keys.next();
                        keys.remove();

                        if (!key.isValid()) continue;

                        if (key.isConnectable()){
                            System.out.println("I am connected to the server");
                            connect(key);
                        }
                        if (key.isWritable()){
                            write(key);
                        }
                        if (key.isReadable()){
                            read(key);
                        }
                    }
                }
            } catch (Exception e1) {
                   e1.printStackTrace();
            } finally {
                close();
            }
        }

        private void close(){
            try {
                selector.close();
            } catch (IOException e) {
                  e.printStackTrace();
            }
        }

        private void read (SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer readBuffer = ByteBuffer.allocate(1000);
            readBuffer.clear();
            int length;
            try{
                length = channel.read(readBuffer);
            } catch (IOException e){
                System.out.println("Reading problem, closing connection");
                key.cancel();
                channel.close();
                return;
            }
            if (length == -1){
                System.out.println("Nothing was read from server");
                channel.close();
                key.cancel();
                return;
            }
            readBuffer.flip();
            byte[] buff = new byte[1024];
            readBuffer.get(buff, 0, length);
            System.out.println("Received: " + NioServer.extractString(buff));
            key.interestOps(SelectionKey.OP_WRITE);
        }

        private void write(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            String userInput = stdIn.readLine();
                channel.write(ByteBuffer.wrap(userInput.getBytes()));
            // lets get ready to read.
            key.interestOps(SelectionKey.OP_READ);
        }

        private void connect(SelectionKey key) throws IOException {
            SocketChannel channel = (SocketChannel) key.channel();
            if (channel.isConnectionPending()){
                channel.finishConnect();
            }
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
        }


}