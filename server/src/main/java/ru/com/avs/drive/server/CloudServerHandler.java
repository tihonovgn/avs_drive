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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object cmd) throws Exception {
        try {
            if (cmd == null)
                return;
            Response answer;
            if (cmd instanceof Request) {
                Request request = (Request) cmd;
                if (authenticate(request.getLogin(), request.getPassword())) {
                    ServerService serverService = new ServerService(request.getLogin());
                    switch (request.getCommand()) {
                        case LIST:
                            String path = request.getFile() != null ? request.getFile().getPath() : null;
                            answer = serverService.getFileListResponse(path);
                            break;
                        case GET:
                            answer = serverService.getFileResponse(request);
                            break;
                        case DELETE:
                            answer = serverService.getFileDeleteResponse(request.getFile().getName());
                            break;
                        case SAVE:
                            answer = serverService.getFileSaveResponse(request);
                            break;
                        case MOVE:
                            answer = serverService.getFileMoveResponse(request);
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
