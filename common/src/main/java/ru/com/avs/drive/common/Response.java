package ru.com.avs.drive.common;

import java.io.Serializable;
import java.util.List;

public class Response  implements Serializable {
    private RESULTS result;
    private String message;
    private List<MyFile> files;

    public Response(RESULTS result, String message) {
        this.result = result;
        this.message = message;
    }

    public Response(RESULTS result, List<MyFile> filesList) {
        this.result = result;
        this.files = filesList;
    }

    public Response(RESULTS result) {
        this.result = result;
    }

    public enum RESULTS {OK, ERROR};

    public RESULTS getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public List<MyFile> getFiles() {
        return files;
    }
}
