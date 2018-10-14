package ru.com.avs.drive.common;

import java.io.Serializable;
import java.nio.file.Path;

public class MyFile implements Serializable {

    private String name;
    private String type;
    private long size;
    private byte[] data;
    private String origName;

    public MyFile(Path fileName, boolean directory, long size) {
        this.name = fileName.getFileName().toString();
        this.type = directory ? "директория" : "файл";
        this.size = size;
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
}
