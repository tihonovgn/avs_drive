package ru.com.avs.drive.common;

import java.io.Serializable;
import java.util.Map;

public class Request implements Serializable {
    private Map<String, String> args;
    private String login;
    private String password;
    private COMMANDS command;

    public Request(Map<String, String> authData, COMMANDS cmd, Map<String, String> args) {
        login = authData.get("login");
        password = authData.get("password");
        command = cmd;
        this.args = args;
    }

    public enum COMMANDS {LIST, SAVE, DELETE};

    public Request(Map<String, String> authData, COMMANDS cmd) {
        login = authData.get("login");
        password = authData.get("password");
        command = cmd;
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

    public Map<String, String> getArgs() {
        return args;
    }
}
