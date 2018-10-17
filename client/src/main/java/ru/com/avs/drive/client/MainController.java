package ru.com.avs.drive.client;

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
import java.net.URL;
import java.nio.file.*;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private ClientService clientService;

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

        clientService = new ClientService();
    }

    private void refreshLocalFileList() {
        try {
            leftTable.getItems().clear();
            Files.newDirectoryStream(Paths.get(FOLDER)).forEach(path -> {
                try {
                    leftTable.getItems().add(new MyFile(path, path.subpath(1, path.getNameCount())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleRefreshButtonAction(ActionEvent actionEvent) {
        refreshLocalFileList();
        refreshServerFileList();
    }

    public void refreshServerFileList() {
        Request request = new Request(authData, Request.COMMANDS.LIST);
        Response answer = clientService.sendRequest(request);

        rightTable.getItems().clear();
        for (MyFile file : answer.getFiles()) {
            rightTable.getItems().add(file);
        }
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
                    deleteFile(file);

                }
            }
        }
    }

    private void deleteFile(MyFile file) {
        if (lastSelectedTable == leftTable) {
            FileService.deleteLocalFile(FOLDER + "/" + file.getName());
            refreshLocalFileList();
        } else if (lastSelectedTable == rightTable) {
            deleteServerFile(file);
            refreshServerFileList();
        }
    }

    private void deleteServerFile(MyFile file) {
        Request request = new Request(authData, Request.COMMANDS.DELETE, file);
        Response answer = clientService.sendRequest(request);
    }

    public void handleCopyButtonAction(ActionEvent actionEvent) {
        if (lastSelectedTable != null) {
            MyFile file = (MyFile) lastSelectedTable.getSelectionModel().getSelectedItem();
            if (file != null) {
                TextInputDialog dialog = new TextInputDialog(file.getName());
                dialog.setTitle("Загрузка файла");
                dialog.setHeaderText("Загрузка файла");
                dialog.setContentText("Укажите новое имя файла:");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    file.setOrigName(file.getName());
                    if (result.get().length() > 0) {
                        file.setName(result.get());
                    }
                    copyFile(file);
                }
            }
        }
    }

    private boolean confirm(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            return true;
        }
        return false;
    }

    private void copyFile(MyFile file) {
        clientService.setAuthData(authData);
        String msg = "Файл с таким именем существует. Заменить?";
        if (lastSelectedTable == leftTable) {
            if (fileNotExists(file, rightTable) || confirm(msg)) {
                clientService.copyFileToServer(file);
            }
            refreshServerFileList();
        } else if (lastSelectedTable == rightTable) {
            if (fileNotExists(file, leftTable) || confirm(msg)) {
                clientService.copyFileToLocal(file);
            }
            refreshLocalFileList();
        }
    }

    private boolean fileNotExists(MyFile file, TableView table) {
        AtomicBoolean result = new AtomicBoolean(true);
        table.getItems().forEach(o -> {
            MyFile item = (MyFile) o;
            if (item.getName().equals(file.getName())) {
                result.set(false);
            }
        });
        return result.get();
    }
}
