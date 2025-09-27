package se233.inverted.chapter3.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import se233.inverted.chapter3.Launcher;
import se233.inverted.chapter3.model.FileFreq;
import se233.inverted.chapter3.model.PdfDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;


public class MainViewController {
    LinkedHashMap<String, List<FileFreq>> uniqueSets;
    @FXML
    private ListView<String> inputListView;
    @FXML
    private Button startButton;
    @FXML
    private ListView listView;
    private List<String> filePathList = new ArrayList<>();
    @FXML
    private void handleCloseAction() {
        Platform.exit();
    }
    @FXML
    private Map<String, String> displayToWordMap = new HashMap<>();


    @FXML
    public void initialize() {
        inputListView.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            final boolean isAccepted = db.getFiles().get(0).getName().toLowerCase().endsWith(".pdf");
            if (db.hasFiles() && isAccepted) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        inputListView.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    filePathList.add(file.getAbsolutePath());
                    inputListView.getItems().add(file.getName());
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

        startButton.setOnAction(event -> {
            Parent bgRoot = Launcher.primaryStage.getScene().getRoot();
            Task<Void> processTask = new Task<Void>() {
                @Override
                public Void call() throws IOException {
                    ProgressIndicator pi = new ProgressIndicator();
                    VBox box = new VBox(pi);
                    box.setAlignment(Pos.CENTER);
                    Launcher.primaryStage.getScene().setRoot(box);

                    ExecutorService executor = Executors.newFixedThreadPool(4);
                    final ExecutorCompletionService<Map<String, FileFreq>> completionService = new ExecutorCompletionService<>(executor);

                    List<String> inputListViewItems = filePathList;
                    int total_files = inputListViewItems.size();
                    Map<String, FileFreq>[] wordMap = new Map[total_files];

                    for (int i = 0; i < total_files; i++) {
                        try {
                            String filePath = inputListViewItems.get(i);
                            PdfDocument p = new PdfDocument(filePath);
                            completionService.submit(new WordCountMapTask(p));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                    for (int i = 0; i < total_files; i++) {
                        try {
                            Future<Map<String, FileFreq>> future = completionService.take();
                            wordMap[i] = future.get();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        WordCountReduceTask merger = new WordCountReduceTask(wordMap);
                        Future<LinkedHashMap<String, List<FileFreq>>> future = executor.submit(merger);
                        uniqueSets = future.get();
                        for (Map.Entry<String, List<FileFreq>> entry : uniqueSets.entrySet()) {
                            String word = entry.getKey();
                            List<FileFreq> freqs = entry.getValue();
                            int total = freqs.stream().mapToInt(FileFreq::getFreq).sum();
                            String display = word + " (" + total + ")";
                            listView.getItems().add(display);
                            displayToWordMap.put(display, word);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        executor.shutdown();
                    }
                    return null;
                }
            };

            processTask.setOnSucceeded(e -> {
                Launcher.primaryStage.getScene().setRoot(bgRoot);
            });

            Thread thread = new Thread(processTask);
            thread.setDaemon(true);
            thread.start();
        });

        listView.setOnMouseClicked(event -> {

            String displayTerm = (String) listView.getSelectionModel().getSelectedItem();
            if (displayTerm == null) return;

            String selectedTerm = displayToWordMap.get(displayTerm);
            if (selectedTerm == null) return;

            List<FileFreq> listOfLinks = uniqueSets.get(selectedTerm);

            if (listOfLinks == null || listOfLinks.isEmpty()) return;

            ListView<FileFreq> popupListView = new ListView<>();
            LinkedHashMap<FileFreq, String> lookupTable = new LinkedHashMap<>();

            for (FileFreq fileFreq : listOfLinks) {
                lookupTable.put(fileFreq, fileFreq.getPath());
                popupListView.getItems().add(fileFreq);
            }

            popupListView.setPrefWidth(Region.USE_COMPUTED_SIZE);
            popupListView.setPrefHeight(popupListView.getItems().size() * 40);

            popupListView.setOnMouseClicked(innerEvent -> {
                FileFreq selected = popupListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Launcher.hs.showDocument("file:///" + lookupTable.get(selected));
                    popupListView.getScene().getWindow().hide();
                }
            });

            Popup popup = new Popup();
            popup.setAutoHide(true);
            popup.getContent().add(popupListView);
            popup.show(Launcher.primaryStage);

            Platform.runLater(() -> {
                popupListView.requestFocus();
                Scene scene = popupListView.getScene();
                if (scene != null) {
                    scene.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
                        if (keyEvent.getCode() == KeyCode.ESCAPE) {
                            popup.hide();
                            keyEvent.consume();
                        }
                    });
                }
            });
        });



    }
}
