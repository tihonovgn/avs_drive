package ru.com.avs.drive.common;

import java.io.Serializable;
import java.nio.file.Path;

public class MyFile implements Serializable {

    private String name;
    private String type;
    private long size;

    public MyFile(Path fileName, boolean directory, long size) {
        this.name = fileName.getFileName().toString();
        this.type = directory ? "директория" : "файл";
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public long getSize() {
        return size;
    }
}
