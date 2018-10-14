package ru.com.avs.drive.server;

import ru.com.avs.drive.common.FileService;
import ru.com.avs.drive.common.MyFile;
import ru.com.avs.drive.common.Request;
import ru.com.avs.drive.common.Response;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerService {

    private String username;
    private String folder = "server_folder/";

    public ServerService(String login) {
        username = login;
    }

    public Response getFileListResponse() {
        List<MyFile> filesList = getFilesList();
        return new Response(Response.RESULTS.OK, filesList);
    }

    private List<MyFile> getFilesList() {
        List<MyFile> fileList = new ArrayList<>();
        try {
            Path dir = Paths.get(getUserFolder());
            if (!Files.exists(dir)) {
                Files.createDirectory(dir);
            }
            Files.newDirectoryStream(dir)
                    .forEach(path -> {
                        try {
                            fileList.add(new MyFile(path.getFileName(), Files.isDirectory(path), Files.size(path)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    private String getUserFolder() {
        return folder + "/" + username;
    }

    public Response getFileDeleteResponse(String filename) {
        String fileName = getUserFolder() + "/" + filename;
        FileService.deleteLocalFile(fileName);
        return new Response(Response.RESULTS.OK);
    }

    public Response getFileSaveResponse(Request request) {
        String path = getUserFolder() + "/" + request.getFile().getName();
        try {
            Files.write(Paths.get(path), request.getFile().getData(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Response(Response.RESULTS.ERROR, "Произошла ошибка");
    }

    public Response getFileResponse(Request request) {
        MyFile file = request.getFile();
        Path path = Paths.get(getUserFolder() + "/" + file.getOrigName());
        try {
            byte[] data = Files.readAllBytes(path);
            file.setData(data);
        } catch (IOException e) {
            e.printStackTrace();
            return new Response(Response.RESULTS.ERROR, "Произошла ошибка");
        }
        List<MyFile> fileList = new ArrayList<>();
        fileList.add(file);
        return new Response(Response.RESULTS.OK, fileList);
    }
}
