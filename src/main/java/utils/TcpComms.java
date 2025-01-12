package utils;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import protocols.Common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public class TcpComms {
    DataOutputStream outputStream;
    DataInputStream inputStream;

    Thread receiveThread;

    ITcpHandler msgHandler;

    AtomicInteger messagesSent = new AtomicInteger(0);


    public TcpComms(ITcpHandler handler) {
        msgHandler = handler;
    }

    public void start() {
        setupClient();

        receiveThread = new Thread(this::startReceive);
        receiveThread.start();
    }

    void startReceive() {
        while (!socket.isClosed()) {

//            while (messagesSent.get() == 0) {
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException ignored) {}
//            }
            System.out.println("Waiting for response");
            if (!readResponse()) {
                break;
            }
//            if (readResponse()) messagesSent.decrementAndGet();
        }
        resetConnection();
    }

    Socket socket;
    public void setupClient() {
        while (socket == null || socket.isClosed()){
            try {
                socket = new Socket("127.0.0.1", 7878);
                socket.setTcpNoDelay(true);

                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());
                Thread.sleep(1000);
            } catch (IOException | InterruptedException ignored) {}
        }
    }

    void resetConnection() {
        try {
            socket.close();
            inputStream.close();
            outputStream.close();
            start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized boolean send_await(Common.MessageID id, GeneratedMessageV3 message) {
        if (socket.isClosed() || outputStream == null) return false;
        try {
            outputStream.writeInt(message.getSerializedSize());
            outputStream.writeInt(id.getNumber());
            message.writeTo(outputStream);
//            messagesSent.incrementAndGet();

            int result = inputStream.readInt();
            System.out.println("GOT RESULT = " + result + " FROM SERVER");
            if (result != -1) {
                readResponse();
            }
            return result != -1;
        } catch (SocketException ex) {
            resetConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public synchronized boolean send(Common.MessageID id, GeneratedMessageV3 message) {
        if (socket.isClosed() || outputStream == null) return false;
        try {
            outputStream.writeInt(message.getSerializedSize());
            outputStream.writeInt(id.getNumber());
            message.writeTo(outputStream);
            messagesSent.incrementAndGet();

            return true;
        } catch (SocketException ex) {
            resetConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface ITcpHandler {
        void processResponse(Common.MessageID msgId, byte[] bytes) throws InvalidProtocolBufferException;
    }

    boolean readResponse() {
        if (socket.isClosed() || inputStream == null) return false;
        try {
            int msgSize = inputStream.readInt();
            System.out.println("READ MSG SIZE = " + msgSize);
            if (msgSize == -1) return false;
//           System.out.println("READ MSG SIZE = " + msgSize);
            int msgId = inputStream.readInt();
            System.out.println("Read MSG ID = " + msgId);
            Common.MessageID mid = Common.MessageID.forNumber(msgId);
            System.out.println("Received MSG ID = " + mid);
            byte[] bytes = new byte[msgSize];
            inputStream.readFully(bytes);
            msgHandler.processResponse(mid, bytes);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    public void send(byte[] message) {
//        if (outputStream == null) return;
//        try {
//            outputStream.write(message.length);
//            outputStream.write(message);
//            System.out.println("BYTES = " + Arrays.toString(message));
////            outputStream.write(message.toByteArray());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
