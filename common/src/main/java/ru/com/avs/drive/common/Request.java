package ru.com.avs.drive.common;

import java.io.Serializable;
import java.util.Map;

public class Request implements Serializable {
    private String login;
    private String password;
    private COMMANDS command;
    private MyFile file;

    public enum COMMANDS {LIST, SAVE, DELETE, GET};

    public Request(Map<String, String> authData, COMMANDS cmd) {
        login = authData.get("login");
        password = authData.get("password");
        command = cmd;
    }

    public Request(Map<String, String> authData, COMMANDS cmd, MyFile file) {
        login = authData.get("login");
        password = authData.get("password");
        command = cmd;
        this.file = file;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public COMMANDS getCommand() {
        return command;
    }

    public MyFile getFile() {
        return file;
    }
}
