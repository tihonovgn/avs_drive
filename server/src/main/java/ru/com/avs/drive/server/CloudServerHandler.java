package ru.com.avs.drive.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.com.avs.drive.common.FileService;
import ru.com.avs.drive.common.MyFile;
import ru.com.avs.drive.common.Request;
import ru.com.avs.drive.common.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CloudServerHandler extends ChannelInboundHandlerAdapter {

    private String username;
    private String folder = "server_folder/";

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object cmd) throws Exception {
        try {
            if (cmd == null)
                return;
            System.out.println(cmd.getClass());
            Response answer;
            if (cmd instanceof Request) {
                Request request = (Request) cmd;
                if (authenticate(request.getLogin(), request.getPassword())) {
                    username = request.getLogin();
                    switch (request.getCommand()) {
                        case LIST:
                            List<MyFile> filesList = getFilesList();
                            answer = new Response(Response.RESULTS.OK, filesList);
                            break;
                        case DELETE:
                            String fileName = getUserFolder() + "/" + ((Request) cmd).getArgs().get("filename");
                            FileService.deleteLocalFile(fileName);
                            answer = new Response(Response.RESULTS.OK);
                            break;
                        default:
                            answer = new Response(Response.RESULTS.ERROR, "Wrong command!");
                    }
                    ctx.write(answer);
                } else {
                    answer = new Response(Response.RESULTS.ERROR, "Authentication failed!");
                    ctx.write(answer);
                }
            } else {
                System.out.printf("Server received wrong object!");
                return;
            }
        } finally {
            ReferenceCountUtil.release(cmd);
        }
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

    private boolean authenticate(String login, String password) {
        return login.equals("admin") && password.equals("admin");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
