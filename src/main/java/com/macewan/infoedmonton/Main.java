package com.macewan.infoedmonton;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main extends Application {

    private static WebView webViewProp, webViewCov;
    private TableView<PropertyAssessment> table = new TableView<>();
    private ObservableList<PropertyAssessment> obsListProperty;
    private Label labelSource, labelPropertyAssesment, labelAccNum, labelAddress, labelNeighborhood,
            labelAssessmentClass, labelValRange, labelTable;
    private ComboBox<String> comboBoxSource, comboBoxAssesmentClass;
    private Button buttonRead, buttonSearch, buttonReset;
    private TextField textFieldAccNum, textFieldAddress, textFieldNeighborhood, textFieldMinVal, textFieldMaxVal;
    private Separator separator1, separator2;
    private WebEngine webEngineProp;
    private CsvPropertyAssessmentDAO csvPropertyAssessmentDAO;
    private ApiPropertyAssessmentDAO apiPropertyAssessmentDAO;
    private Task<Void> task;
    private Thread thread;

    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        /* Property Assessment Tab */
        GridPane gridPaneProperty = new GridPane();
        gridPaneProperty.setPadding(new Insets(20, 20, 20, 20));
        gridPaneProperty.setHgap(10);
        gridPaneProperty.setVgap(5);

        // Data source section
        labelSource = new Label("Select Data Source");
        labelSource.setFont(Font.font("Calibri", FontWeight.BOLD, 15));
        comboBoxSource = new ComboBox<>(FXCollections.observableArrayList("CSV File", "Edmonton's Open Data Portal"));
        buttonRead = new Button("Read Data");
        separator1 = new Separator();
        separator2 = new Separator();

        // Find property assessment section
        labelPropertyAssesment = new Label("Find Property Assessment");
        labelPropertyAssesment.setFont(Font.font("Calibri", FontWeight.BOLD, 15));
        labelAccNum = new Label("Account number:");
        textFieldAccNum = new TextField();
        labelNeighborhood = new Label("Neighborhood:");
        textFieldNeighborhood = new TextField();
        labelAddress = new Label("Address (#Suite #House Street)");
        textFieldAddress = new TextField();
        labelAssessmentClass = new Label("Assessment class:");
        comboBoxAssesmentClass = new ComboBox<>();
        labelValRange = new Label("Assessment value range");
        textFieldMinVal = new TextField();
        textFieldMinVal.setPromptText("Min Value");
        textFieldMaxVal = new TextField();
        textFieldMaxVal.setPromptText("Max Value");
        buttonSearch = new Button("Search");
        buttonReset = new Button("Reset");

        // UI set up
        VBox vBoxMinSearch = new VBox(10, textFieldMinVal, buttonSearch);
        VBox vBoxMaxReset = new VBox(10, textFieldMaxVal, buttonReset);
        HBox HBoxMinMaxSearchReset = new HBox(10, vBoxMinSearch, vBoxMaxReset);
        buttonSearch.prefWidthProperty().bind(vBoxMinSearch.widthProperty().multiply(1));
        buttonReset.prefWidthProperty().bind(vBoxMaxReset.widthProperty().multiply(1));

        VBox vBoxDataInput = new VBox(10, labelSource, comboBoxSource, buttonRead, separator1, labelPropertyAssesment,
                labelAccNum, textFieldAccNum, labelAddress, textFieldAddress, labelNeighborhood, textFieldNeighborhood,
                labelAssessmentClass, comboBoxAssesmentClass, labelValRange, HBoxMinMaxSearchReset, separator2);
        vBoxDataInput.setStyle("-fx-border-color: #bbbbbb");
        vBoxDataInput.setPadding(new Insets(10, 10, 10, 10));
        vBoxDataInput.prefWidthProperty().bind(gridPaneProperty.widthProperty().multiply(0.125));
        buttonRead.prefWidthProperty().bind(vBoxDataInput.widthProperty().multiply(1));
        comboBoxSource.prefWidthProperty().bind(vBoxDataInput.widthProperty().multiply(1));
        comboBoxAssesmentClass.prefWidthProperty().bind(vBoxDataInput.widthProperty().multiply(1));
        HBoxMinMaxSearchReset.prefWidthProperty().bind(vBoxDataInput.widthProperty().multiply(1));

        // Table Section
        labelTable = new Label("Edmonton Property Assessments 2021");
        labelTable.setFont(Font.font("Calibri", FontWeight.BOLD, 15));
        table = setUpTable();
        table.prefWidthProperty().bind(gridPaneProperty.widthProperty().multiply(0.55));
        table.prefHeightProperty().bind(gridPaneProperty.heightProperty().multiply(1));

        gridPaneProperty.add(vBoxDataInput, 0, 0, 1, 2);
        gridPaneProperty.add(labelTable, 1, 0);
        gridPaneProperty.add(table, 1, 1);

        // WebView/Map Section
        webViewProp = new WebView();
        webEngineProp = webViewProp.getEngine();
        //change here
        webEngineProp.load(getClass().getResource("html/GoogleMap.html").toExternalForm());
        webViewProp.prefWidthProperty().bind(gridPaneProperty.widthProperty().multiply(0.325));
        webViewProp.prefHeightProperty().bind(gridPaneProperty.heightProperty().multiply(1));
        gridPaneProperty.add(webViewProp, 2, 1);

        Tab tabEdmPropAssesment = new Tab("Property Assessment", gridPaneProperty);
        tabEdmPropAssesment.setStyle("-fx-pref-width: 130");
        tabPane.getTabs().add(tabEdmPropAssesment);

        /* COVID 19 Tab */
        // Map by health area
        webViewCov = new WebView();
        webViewCov.getEngine().load(getClass().getResource("html/COV.html").toExternalForm());

        Tab tabCOVID = new Tab("Daily COVID-19 Info", webViewCov);
        tabCOVID.setStyle("-fx-pref-width: 130");
        tabPane.getTabs().add(tabCOVID);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Event Handler
        setUpEventHandler();

        // Scene UI
        Scene scene = new Scene(tabPane, 1800, 800);
        stage.setTitle("Edmonton Statistics 2021");
        stage.setScene(scene);
        stage.show();
    }

    private void setUpEventHandler() {
        // read from source
        buttonRead.setOnAction(event -> {
            table.getItems().clear();
            if (comboBoxSource.getSelectionModel().getSelectedItem() == null) {
                alertNoSource();
            } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("CSV File")) {
                try {
                    csvPropertyAssessmentDAO = new CsvPropertyAssessmentDAO();
                    obsListProperty.addAll(csvPropertyAssessmentDAO.getAll(0, 0));
                    comboBoxAssesmentClass.setItems(FXCollections.observableList(new ArrayList<>(csvPropertyAssessmentDAO.getMapByAssessmentClass().keySet())));
                } catch (IOException exception) {
                    alertNoCSV();
                }
            } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("Edmonton's Open Data Portal")) {
                task = new Task<>() {
                    @Override
                    protected Void call() {
                        try {
                            int limit = 1000;
                            int offset = 0;
                            apiPropertyAssessmentDAO = new ApiPropertyAssessmentDAO();
                            List<PropertyAssessment> returnList = apiPropertyAssessmentDAO.getAll(limit, offset);
                            comboBoxAssesmentClass.setItems(FXCollections.observableList(new ArrayList<>(apiPropertyAssessmentDAO.getAssessmentClassSet())));
                            while (returnList.size() > 0) {
                                if (isCancelled()) break;
                                List<PropertyAssessment> finalReturnList = returnList;
                                Platform.runLater(() -> {
                                    // JavaFX Scene must be updated in Platform.runLater()
                                    buttonRead.setDisable(true);
                                    // We add subsets of data to the table (1000 rows at a time)
                                    obsListProperty.addAll(finalReturnList);
                                });

                                // Retrieve the next batch
                                offset += limit;
                                returnList = apiPropertyAssessmentDAO.getAll(limit, offset);
                            }

                            Platform.runLater(() -> buttonRead.setDisable(false));
                        } catch (IOException | InterruptedException exception) {
                            alertConnectionFailure();
                        }
                        return null;
                    }
                };
                thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        });

        // search
        buttonSearch.setOnAction(event -> {
            if (task != null && task.isRunning()) {
                task.cancel();
                buttonRead.setDisable(false);
            }
            table.getItems().clear();

            // account number provided
            if (!textFieldAccNum.getText().isEmpty()) {
                searchByAccountNum();
            }
            // neighborhood provided
            else if (!textFieldNeighborhood.getText().isEmpty()
                    && textFieldAccNum.getText().isEmpty()) {
                searchByNeighborhood();
            }
            // assessment class provided
            else if ((comboBoxAssesmentClass.getSelectionModel().getSelectedItem() != null)
                    && textFieldNeighborhood.getText().isEmpty()
                    && textFieldAccNum.getText().isEmpty()) {
                searchByAssessmentClass();
            }
            // address provided
            else if ((!textFieldAddress.getText().isEmpty())
                    && (comboBoxAssesmentClass.getSelectionModel().getSelectedItem() == null)
                    && textFieldNeighborhood.getText().isEmpty()
                    && textFieldAccNum.getText().isEmpty()) {
                searchByAddress();

            } else if (!textFieldMinVal.getText().isEmpty() || !textFieldMaxVal.getText().isEmpty()
                    && textFieldAddress.getText().isEmpty()
                    && (comboBoxAssesmentClass.getSelectionModel().getSelectedItem() == null)
                    && textFieldNeighborhood.getText().isEmpty()
                    && textFieldAccNum.getText().isEmpty()) {
                searchByMinMax();
            }
        });
        //reset
        buttonReset.setOnAction(event -> {
            final WebEngine engine = webViewProp.getEngine();
            engine.setJavaScriptEnabled(true);
            engine.executeScript("initMap()");
            table.getItems().clear();
            textFieldAccNum.clear();
            textFieldAddress.clear();
            textFieldMaxVal.clear();
            textFieldMaxVal.clear();
            textFieldNeighborhood.clear();
            comboBoxAssesmentClass.getItems().clear();
            if (task != null && task.isRunning()) {
                task.cancel();
                buttonRead.setDisable(false);
            }
        });

        //click on a property to locate it on google map
        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                PropertyAssessment propertyAssessment = table.getSelectionModel().getSelectedItem();
                if (propertyAssessment != null) {
                    locateProperty(propertyAssessment);
                }
            }
        });
    }

    private void searchByMinMax() {
        if (comboBoxSource.getSelectionModel().getSelectedItem() == null) {
            alertNoSource();
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("CSV File")) {
            try {
                int min = 0, max = 0;
                if (!textFieldMinVal.getText().isEmpty()) {
                    min = Integer.parseInt(textFieldMinVal.getText());
                } else {
                    min = csvPropertyAssessmentDAO.getMin();
                }
                if (!textFieldMaxVal.getText().isEmpty()) {
                    max = Integer.parseInt(textFieldMaxVal.getText());
                } else {
                    max = csvPropertyAssessmentDAO.getMin();
                }

                for (PropertyAssessment propertyAssessment : csvPropertyAssessmentDAO.getAll(0, 0)) {
                    if (propertyAssessment.getAssessedValue() >= min
                            && propertyAssessment.getAssessedValue() <= max) {
                        obsListProperty.add(propertyAssessment);
                    }
                }
                if (obsListProperty.isEmpty()) {
                    alertNoResult();
                }
            } catch (NullPointerException exception) {
                alertNoSource();
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            }
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("Edmonton's Open Data Portal")) {
            try {
                Integer min = null;
                Integer max = null;
                if (!textFieldMinVal.getText().isEmpty())
                    min = Integer.parseInt(textFieldMinVal.getText());
                if (!textFieldMaxVal.getText().isEmpty())
                    max = Integer.parseInt(textFieldMaxVal.getText());
                obsListProperty.addAll(apiPropertyAssessmentDAO.getByMinMax(min, max));
                if (obsListProperty.isEmpty()) {
                    alertNoResult();
                }
            } catch (NullPointerException exception) {
                alertNoSource();
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            } catch (IOException | InterruptedException exception) {
                alertConnectionFailure();
            }
        }
        if (!obsListProperty.isEmpty()) {
            locateProperties(obsListProperty);
        }
    }

    private void searchByAddress() {
        if (comboBoxSource.getSelectionModel().getSelectedItem() == null) {
            alertNoSource();
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("CSV File")) {
            try {
                for (PropertyAssessment propertyAssessment : csvPropertyAssessmentDAO.getAll(0, 0)) {
                    if (propertyAssessment.getHouseInfo().toString().contains(textFieldAddress.getText().toUpperCase())
                            && (textFieldMinVal.getText().isEmpty() || propertyAssessment.getAssessedValue() >= Integer.parseInt(textFieldMinVal.getText()))
                            && (textFieldMaxVal.getText().isEmpty() || propertyAssessment.getAssessedValue() <= Integer.parseInt(textFieldMaxVal.getText()))) {
                        obsListProperty.add(propertyAssessment);
                    }
                }
                if (obsListProperty.isEmpty()) {
                    alertNoResult();
                }
            } catch (NullPointerException exception) {
                alertNoSource();
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            }
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("Edmonton's Open Data Portal")) {
            try {
                Integer min = null;
                Integer max = null;
                if (!textFieldMinVal.getText().isEmpty())
                    min = Integer.parseInt(textFieldMinVal.getText());
                if (!textFieldMaxVal.getText().isEmpty())
                    max = Integer.parseInt(textFieldMaxVal.getText());
                obsListProperty.addAll(apiPropertyAssessmentDAO.getByAddress(textFieldAddress.getText(), min, max));
                if (obsListProperty.isEmpty()) {
                    alertNoResult();
                }
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            } catch (NullPointerException exception) {
                alertNoSource();
            } catch (IOException | InterruptedException exception) {
                alertConnectionFailure();
            }
        }
        if (!obsListProperty.isEmpty()) {
            locateProperties(obsListProperty);
        }
    }

    private void locateProperties(ObservableList<PropertyAssessment> e) {
        if (!e.isEmpty()) {
            List<String> passingObj = new ArrayList<>();
            int tempLen = 10;
            if (e.size() < 10)
                tempLen = e.size();
            for (int i = 0; i < tempLen; i++)
                passingObj.add(e.get(i).getAssessedValue() + "|" + e.get(i).getAccountNum() + "|" + e.get(i).getHouseInfo().toString() +
                        "|" + e.get(i).getPoint().getLatitude() + "|" + e.get(i).getPoint().getLongitude());
            webEngineProp.load(getClass().getResource("html/GoogleMap.html").toExternalForm());
            webEngineProp.setJavaScriptEnabled(true);
            webEngineProp.getLoadWorker().stateProperty().addListener(
                    (ov, oldState, newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            webEngineProp.executeScript("showProperty('" + passingObj + "')");
                        }
                    }
            );
        }

    }

    private void searchByAssessmentClass() {
        if (comboBoxSource.getSelectionModel().getSelectedItem() == null) {
            alertNoSource();
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("CSV File")) {
            try {
                for (PropertyAssessment propertyAssessment : csvPropertyAssessmentDAO.getMapByAssessmentClass().get(comboBoxAssesmentClass.getSelectionModel().getSelectedItem()).getPropertyList()) {
                    if ((textFieldAddress.getText().isEmpty() || propertyAssessment.getHouseInfo().toString().contains(textFieldAddress.getText().toUpperCase()))
                            && (textFieldMinVal.getText().isEmpty() || propertyAssessment.getAssessedValue() >= Integer.parseInt(textFieldMinVal.getText()))
                            && (textFieldMaxVal.getText().isEmpty() || propertyAssessment.getAssessedValue() <= Integer.parseInt(textFieldMaxVal.getText()))) {
                        obsListProperty.add(propertyAssessment);
                    }
                }
                if (obsListProperty.isEmpty()) {
                    alertNoResult();
                }
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            } catch (NullPointerException exception) {
                alertNoSource();
            }
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("Edmonton's Open Data Portal")) {
            try {
                if (textFieldAddress.getText().isEmpty()
                        && textFieldMinVal.getText().isEmpty() && textFieldMaxVal.getText().isEmpty()) {
                    obsListProperty.addAll(apiPropertyAssessmentDAO.getByAssessmentClass(comboBoxAssesmentClass.getSelectionModel().getSelectedItem()));
                } else {
                    Integer min = null;
                    Integer max = null;
                    if (!textFieldMinVal.getText().isEmpty())
                        min = Integer.parseInt(textFieldMinVal.getText());
                    if (!textFieldMaxVal.getText().isEmpty())
                        max = Integer.parseInt(textFieldMaxVal.getText());
                    obsListProperty.addAll(apiPropertyAssessmentDAO.getByAssessmentClass(comboBoxAssesmentClass.getSelectionModel().getSelectedItem(),
                            textFieldAddress.getText(), min, max));
                }
                if (obsListProperty.isEmpty()) {
                    alertNoResult();
                }
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            } catch (NullPointerException exception) {
                alertNoSource();
            } catch (IOException | InterruptedException exception) {
                alertConnectionFailure();
            }
        }
    }

    private void searchByNeighborhood() {
        if (comboBoxSource.getSelectionModel().getSelectedItem() == null) {
            alertNoSource();
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("CSV File")) {
            try {
                Set<String> neighborhoodName = csvPropertyAssessmentDAO.getMapByNeighborhood().keySet();
                for (String elem : neighborhoodName) {
                    if (elem.contains(textFieldNeighborhood.getText().toUpperCase())) {
                        for (PropertyAssessment propertyAssessment : csvPropertyAssessmentDAO.getByNeighbourhood(elem)) {
                            if ((textFieldAddress.getText().isEmpty() || propertyAssessment.getHouseInfo().toString().contains(textFieldAddress.getText().toUpperCase()))
                                    && (comboBoxAssesmentClass.getSelectionModel().getSelectedItem() == null || propertyAssessment.getAssessmentClassList().toString().contains(comboBoxAssesmentClass.getSelectionModel().getSelectedItem()))
                                    && (textFieldMinVal.getText().isEmpty() || propertyAssessment.getAssessedValue() >= Integer.parseInt(textFieldMinVal.getText()))
                                    && (textFieldMaxVal.getText().isEmpty() || propertyAssessment.getAssessedValue() <= Integer.parseInt(textFieldMaxVal.getText()))) {
                                obsListProperty.add(propertyAssessment);
                            }
                        }
                    }
                }
                if (obsListProperty.isEmpty()) {
                    alertNoResult();
                }
            } catch (NullPointerException exception) {
                alertNoSource();
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            }
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("Edmonton's Open Data Portal")) {
            try {
                if (textFieldAddress.getText().isEmpty()
                        && comboBoxAssesmentClass.getSelectionModel().getSelectedItem() == null
                        && textFieldMinVal.getText().isEmpty() && textFieldMaxVal.getText().isEmpty()) {
                    obsListProperty.addAll(apiPropertyAssessmentDAO.getByNeighbourhood(textFieldNeighborhood.getText().toUpperCase()));
                } else {
                    Integer min = null;
                    Integer max = null;
                    if (!textFieldMinVal.getText().isEmpty())
                        min = Integer.parseInt(textFieldMinVal.getText());
                    if (!textFieldMaxVal.getText().isEmpty())
                        max = Integer.parseInt(textFieldMaxVal.getText());
                    obsListProperty.addAll(apiPropertyAssessmentDAO.getByNeighbourhood(textFieldNeighborhood.getText().toUpperCase(),
                            textFieldAddress.getText(),
                            comboBoxAssesmentClass.getSelectionModel().getSelectedItem(),
                            min, max));
                }
                if (obsListProperty.isEmpty()) {
                    alertNoResult();
                }
            } catch (NullPointerException exception) {
                alertNoSource();
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            } catch (IOException | InterruptedException exception) {
                alertConnectionFailure();
            }
        }
        if (!obsListProperty.isEmpty()) {
            List<Point> listCoor = new ArrayList<>();
            List<PropertyAssessment.PointLocation> tempList = new ArrayList<>();
            for (int i = 0; i < obsListProperty.size(); i++) {
                listCoor.add(new Point(obsListProperty.get(i).getPoint().latitude, obsListProperty.get(i).getPoint().longitude));
            }
            List<Point> polygonShape = ConvexHull.makeHull(listCoor);
            for (int i = 0; i < polygonShape.size(); i++) {
                tempList.add(new PropertyAssessment.PointLocation(polygonShape.get(i).x, polygonShape.get(i).y));
            }
            webEngineProp.load(getClass().getResource("html/GoogleMap.html").toExternalForm());
            webEngineProp.setJavaScriptEnabled(true);
            webEngineProp.getLoadWorker().stateProperty().addListener(
                    (ov, oldState, newState) -> {
                        if (newState == Worker.State.SUCCEEDED) {
                            webEngineProp.executeScript("createPoly('" + tempList + "')");
                        }
                    }
            );
        }
    }


    private void searchByAccountNum() {
        PropertyAssessment propertyAssessment = null;
        if (comboBoxSource.getSelectionModel().getSelectedItem() == null) {
            alertNoSource();
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("CSV File")) {
            try {
                if (csvPropertyAssessmentDAO.getMapByAccountNum().containsKey(textFieldAccNum.getText())) {
                    propertyAssessment = csvPropertyAssessmentDAO.getMapByAccountNum().get(textFieldAccNum.getText());
                    obsListProperty.add(propertyAssessment);
                    if (obsListProperty.isEmpty()) {
                        alertNoResult();
                    }
                }
            } catch (NullPointerException exception) {
                alertNoSource();
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            }
        } else if (comboBoxSource.getSelectionModel().getSelectedItem().equals("Edmonton's Open Data Portal")) {
            try {
                propertyAssessment = apiPropertyAssessmentDAO.getByAccountNumber(Integer.parseInt(textFieldAccNum.getText()));
                if (propertyAssessment != null) {
                    obsListProperty.add(apiPropertyAssessmentDAO.getByAccountNumber(Integer.parseInt(textFieldAccNum.getText())));
                }
                if (obsListProperty.isEmpty()) {
                    alertNoResult();
                }
            } catch (NullPointerException exception) {
                alertNoSource();
            } catch (NumberFormatException exception) {
                alertInvalidNum();
            } catch (IOException | InterruptedException exception) {
                alertConnectionFailure();
            }
        }
        if (propertyAssessment != null) {
            locateProperty(propertyAssessment);
        }
    }

    private void locateProperty(PropertyAssessment e) {
        PropertyAssessment.PointLocation temp = e.getPoint();
        webEngineProp.load(getClass().getResource("html/GoogleMap.html").toExternalForm());
        webEngineProp.setJavaScriptEnabled(true);
        PropertyAssessment finalPropertyAssessment = e;
        webEngineProp.getLoadWorker().stateProperty().addListener(
                (ov, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        webEngineProp.executeScript("findAddress('" + temp.latitude + "','" +
                                temp.longitude + "','" +
                                finalPropertyAssessment.getAccountNum() + "','" +
                                finalPropertyAssessment.getHouseInfo() + "','" +
                                finalPropertyAssessment.getAssessedValue() + "')");
                    }
                }
        );

    }

    public void alertInvalidNum() {
        Alert alertInvalidNum = new Alert(Alert.AlertType.ERROR);
        alertInvalidNum.setTitle("Invalid Input Value");
        alertInvalidNum.setHeaderText(null);
        alertInvalidNum.setContentText("Please enter valid number.");
        alertInvalidNum.show();
    }

    public void alertNoSource() {
        Alert alertNoSource = new Alert(Alert.AlertType.ERROR);
        alertNoSource.setTitle("Missing Source");
        alertNoSource.setHeaderText(null);
        alertNoSource.setContentText("Failed to read data from source. Check if a source has been selected and data has been read successfully.");
        alertNoSource.show();
    }

    public void alertNoResult() {
        Alert alertNoResult = new Alert(Alert.AlertType.INFORMATION);
        alertNoResult.setTitle("Search Results");
        alertNoResult.setHeaderText(null);
        alertNoResult.setContentText("No search results found.");
        alertNoResult.show();
    }

    private void alertConnectionFailure() {
        Alert alertConnectionFailure = new Alert(Alert.AlertType.ERROR);
        alertConnectionFailure.setTitle("Connection Failure");
        alertConnectionFailure.setHeaderText(null);
        alertConnectionFailure.setContentText("The application could not connect to the server or the connection has been interrupted.");
        alertConnectionFailure.show();
    }

    private void alertNoCSV() {
        Alert alertConnectionFailure = new Alert(Alert.AlertType.ERROR);
        alertConnectionFailure.setTitle("Failure reading CSV");
        alertConnectionFailure.setHeaderText(null);
        alertConnectionFailure.setContentText("Could not read from the CSV file. Please re-check the file.");
        alertConnectionFailure.show();
    }

    public TableView<PropertyAssessment> setUpTable() {
        table = new TableView<>();
        obsListProperty = FXCollections.observableArrayList();
        table.setItems(obsListProperty);

        TableColumn<PropertyAssessment, String> colAccNum = new TableColumn<>("Account");
        colAccNum.setCellValueFactory(new PropertyValueFactory<>("accountNum"));
        colAccNum.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        table.getColumns().add(colAccNum);

        TableColumn<PropertyAssessment, String> colAddress = new TableColumn<>("Address");
        colAddress.setCellValueFactory(new PropertyValueFactory<>("houseInfo"));
        colAddress.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        table.getColumns().add(colAddress);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        TableColumn<PropertyAssessment, Integer> colAssessedVal = new TableColumn<>("Assessed Value");
        colAssessedVal.setCellValueFactory(new PropertyValueFactory<>("assessedValue"));
        colAssessedVal.prefWidthProperty().bind(table.widthProperty().multiply(0.1));
        colAssessedVal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Integer integer, boolean empty) {
                super.updateItem(integer, empty);
                currencyFormat.setMaximumFractionDigits(0);
                setText(empty ? "" : currencyFormat.format(integer));
            }
        });
        table.getColumns().add(colAssessedVal);

        TableColumn<PropertyAssessment, String> colAssessmentClass = new TableColumn<>("Assessment Class");
        colAssessmentClass.setCellValueFactory(new PropertyValueFactory<>("assessmentClassList"));
        colAssessmentClass.prefWidthProperty().bind(table.widthProperty().multiply(0.15));
        table.getColumns().add(colAssessmentClass);

        TableColumn<PropertyAssessment, String> colNeighborhood = new TableColumn<>("Neighborhood");
        colNeighborhood.setCellValueFactory(new PropertyValueFactory<>("neighborhood"));
        colNeighborhood.prefWidthProperty().bind(table.widthProperty().multiply(0.2));
        table.getColumns().add(colNeighborhood);

        TableColumn<PropertyAssessment, String> colPointLocation = new TableColumn<>("(Latitude, Longitude)");
        colPointLocation.setCellValueFactory(new PropertyValueFactory<>("point"));
        colPointLocation.prefWidthProperty().bind(table.widthProperty().multiply(0.25));
        table.getColumns().add(colPointLocation);
        return table;
    }
}
