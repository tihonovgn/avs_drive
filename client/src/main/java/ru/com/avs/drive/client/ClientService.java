package ru.com.avs.drive.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import ru.com.avs.drive.common.FileService;
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

    public Response copyFileToServer(MyFile file) {
        Path path = Paths.get(FOLDER + "/" + file.getPath());
        Response answer;
        if (file.isDir()) {
            Request request = new Request(authData, Request.COMMANDS.SAVE, file);
            answer = sendRequest(request);
            try {
                Files.newDirectoryStream(path)
                    .forEach(p -> {
                        try {
                            MyFile f = new MyFile(p, p.subpath(1, p.getNameCount()));
                            copyFileToServer(f);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                byte[] data = Files.readAllBytes(path);
                file.setData(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Request request = new Request(authData, Request.COMMANDS.SAVE, file);
            answer = sendRequest(request);
        }
        return answer;
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

    public void moveLocal(MyFile file) throws IOException {
        FileService.move(file, FOLDER);
    }



    public Response moveOnServer(MyFile file) {
        Request request = new Request(authData, Request.COMMANDS.MOVE, file);
        return sendRequest(request);
    }
}
