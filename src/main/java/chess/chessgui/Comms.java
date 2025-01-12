package chess.chessgui;

import com.google.protobuf.GeneratedMessageV3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import protocols.Common.*;

public class Comms {

    static DataOutputStream outputStream;
    static DataInputStream inputStream;

    static Socket socket;
    public static void setupClient() {
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

    static void resetClient() {
        if (socket == null || socket.isClosed()) {
            setupClient();
            return;
        }
        try {
            socket.close();
            inputStream.close();
            outputStream.close();
            setupClient();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized static boolean send(MessageID id, GeneratedMessageV3 message) {
        if (socket.isClosed() || outputStream == null) return false;
        try {
            outputStream.writeInt(message.getSerializedSize());
            outputStream.writeInt(id.getNumber());
            message.writeTo(outputStream);

            int result = inputStream.readInt();
//            System.out.println("GOT RESULT = " + result + " FROM SERVER");
            return result != -1;
        } catch (SocketException ex) {
            resetClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface IProtoHandler {
        void processResponse(byte[] bytes);
    }
    public synchronized static void send(MessageID id, GeneratedMessageV3 message, IProtoHandler handler) {
//        System.out.println("SENDING " + id);
        if (!send(id, message)) return;
        readResponse(handler);
    }
    public synchronized static void send_async(MessageID id, GeneratedMessageV3 message, IProtoHandler handler) {
//        System.out.println("SENDING " + id);
        new Thread(() -> {
            if (!send(id, message)) return;
            readResponse(handler);
        }).start();
    }

    static void readResponse(IProtoHandler handler) {
       if (socket.isClosed() || inputStream == null) return;
       try {
           int msgSize = inputStream.readInt();
           if (msgSize == -1) return;
           int msgId = inputStream.readInt();
//           System.out.println("READ MSG SIZE = " + msgSize);
           byte[] bytes = new byte[msgSize];
           inputStream.readFully(bytes);
           handler.processResponse(bytes);

       } catch (IOException e) {
           e.printStackTrace();
       }
    }

    public void send(byte[] message) {
        if (outputStream == null) return;
        try {
            outputStream.write(message.length);
            outputStream.write(message);
            System.out.println("BYTES = " + Arrays.toString(message));
//            outputStream.write(message.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
