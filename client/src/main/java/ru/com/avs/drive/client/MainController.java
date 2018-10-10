package ru.com.avs.drive.client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ru.com.avs.drive.common.FileService;
import ru.com.avs.drive.common.MyFile;
import ru.com.avs.drive.common.Request;
import ru.com.avs.drive.common.Response;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    TableColumn leftTableName;

    @FXML
    TableColumn leftTableIsDir;

    @FXML
    TableColumn leftTableSize;

    @FXML
    TableView leftTable;

    @FXML
    TableColumn rightTableName;

    @FXML
    TableColumn rightTableIsDir;

    @FXML
    TableColumn rightTableSize;

    @FXML
    TableView rightTable;

    private Map<String, String> authData;
    TableView lastSelectedTable;

    private final String FOLDER = "client_folder";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        leftTableName.setCellValueFactory(new PropertyValueFactory<>("name"));
        leftTableIsDir.setCellValueFactory(new PropertyValueFactory<>("type"));
        leftTableSize.setCellValueFactory(new PropertyValueFactory<>("size"));

        rightTableName.setCellValueFactory(new PropertyValueFactory<>("name"));
        rightTableIsDir.setCellValueFactory(new PropertyValueFactory<>("type"));
        rightTableSize.setCellValueFactory(new PropertyValueFactory<>("size"));

        refreshLocalFileList();

        leftTable.focusedProperty().addListener((observable, oldValue, newValue) -> {
            lastSelectedTable = leftTable;
        });

        rightTable.focusedProperty().addListener((observable, oldValue, newValue) -> {
            lastSelectedTable = rightTable;
        });
    }

    private void refreshLocalFileList() {
        try {
            leftTable.getItems().clear();
            Files.newDirectoryStream(Paths.get(FOLDER)).forEach(path -> {
                try {
                    leftTable.getItems().add(new MyFile(path.getFileName(), Files.isDirectory(path), Files.size(path)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleRefreshButtonAction(ActionEvent actionEvent) {
        refreshServerFileList();
    }

    public void refreshServerFileList() {
        Request request = new Request(authData, Request.COMMANDS.LIST);
        Response answer = sendRequest(request);

        rightTable.getItems().clear();
        for (MyFile file : answer.getFiles()) {
            rightTable.getItems().add(file);
        }
    }

    private Response sendRequest(Request request) {
        ObjectEncoderOutputStream oeos = null;
        ObjectDecoderInputStream odis = null;

        try (Socket socket = new Socket("localhost", 8189)) {
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            oeos.writeObject(request);
            oeos.flush();
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            return (Response) odis.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (oeos != null) {
                    oeos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (odis != null) {
                    odis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setAuthData(Map<String, String> authData) {
        this.authData = authData;
    }

    public void handleDeleteButtonAction(ActionEvent actionEvent) {
        if (lastSelectedTable != null) {
            MyFile file = (MyFile) lastSelectedTable.getSelectionModel().getSelectedItem();
            if (file != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + file.getName() + " ?", ButtonType.YES, ButtonType.NO);
                alert.showAndWait();

                if (alert.getResult() == ButtonType.YES) {
                    deleteFile(file.getName());

                }
            }
        }
    }

    private void deleteFile(String name) {
        if (lastSelectedTable == leftTable) {
            FileService.deleteLocalFile(FOLDER + "/" + name);
            refreshLocalFileList();
        } else if (lastSelectedTable == rightTable) {
            deleteServerFile(name);
            refreshServerFileList();
        }
    }

    private void deleteServerFile(String name) {
        Map<String, String> args = new HashMap<>();
        args.put("filename", name);
        Request request = new Request(authData, Request.COMMANDS.DELETE, args);
        Response answer = sendRequest(request);
    }
}
