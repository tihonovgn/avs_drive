package ru.com.avs.drive.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import ru.com.avs.drive.common.MyFile;
import ru.com.avs.drive.common.Request;
import ru.com.avs.drive.common.Response;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class ClientService {

    private final String FOLDER = "client_folder";

    private Map<String, String> authData;

    public void copyFileToServer(MyFile file) {
        Path path = Paths.get(FOLDER + "/" + file.getOrigName());
        try {
            byte[] data = Files.readAllBytes(path);
            file.setData(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Request request = new Request(authData, Request.COMMANDS.SAVE, file);
        Response answer = sendRequest(request);
    }

    public void copyFileToLocal(MyFile file, boolean rename) {
        if (file.isDir()) {
            Path dir = Paths.get(FOLDER + "/" + file.getPath());
            if (!Files.exists(dir)) {
                try {
                    Files.createDirectory(dir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Request request = new Request(authData, Request.COMMANDS.LIST, file);
            Response answer = sendRequest(request);
            for (MyFile f : answer.getFiles()) {
                copyFileToLocal(f, false);
            }
        } else {
            Request request = new Request(authData, Request.COMMANDS.GET, file);
            Response answer = sendRequest(request);

            String path = FOLDER + "/";
            MyFile currentFile = answer.getFiles().get(0);
            if (rename) {
                path += currentFile.getName();
            } else {
                path += currentFile.getPath();
            }
            try {
                Files.write(Paths.get(path), answer.getFiles().get(0).getData(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setAuthData(Map<String, String> authData) {
        this.authData = authData;
    }

    public Response sendRequest(Request request) {
        ObjectEncoderOutputStream oeos = null;
        ObjectDecoderInputStream odis = null;

        try (Socket socket = new Socket("localhost", 8189)) {
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            oeos.writeObject(request);
            oeos.flush();
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            return (Response) odis.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (oeos != null) {
                    oeos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (odis != null) {
                    odis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void move(MyFile file) {
        Path pathFrom = Paths.get(FOLDER + "/" + file.getOrigName());
        Path pathTo = Paths.get(FOLDER + "/" + file.getName());
        try {
            Files.move(pathFrom, pathTo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
