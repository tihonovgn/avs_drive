package ru.com.avs.drive.common;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class MyFile implements Serializable {

    private String name;
    private String type;
    private long size;
    private byte[] data;
    private String origName;
    private boolean isDir;
    private String path;

    public MyFile(Path file, Path path) throws IOException {
        this.name = file.getFileName().toString();
        boolean directory = Files.isDirectory(file);
        this.type = directory ? "директория" : "файл";
        this.size = Files.size(file);
        this.isDir = directory;
        this.path = path.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getOrigName() {
        return origName;
    }

    public void setOrigName(String origName) {
        this.origName = origName;
    }

    public boolean isDir() {
        return isDir;
    }

    public String getPath() {
        return path;
    }
}
