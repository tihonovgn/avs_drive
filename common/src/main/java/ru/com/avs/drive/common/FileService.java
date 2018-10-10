package ru.com.avs.drive.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class FileService {

    public static void deleteLocalFile(String name) {
        try {
            Path path = Paths.get(name);
            if (Files.isDirectory(path)) {
                deleteDir(path);
            } else {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteDir(Path path) throws IOException {
        java.nio.file.Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                //.peek(System.out::println)
                .forEach(File::delete);
    }
}
