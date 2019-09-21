package controller;

import db.DBConnection;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import util.CustomerTM;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;


public class ManageCustomerFormController implements Initializable {

    public ImageView store;
    public ImageView place;
    public ImageView issue;
    Connection connection;
    private PreparedStatement pstmallData;
    private PreparedStatement pstmUpdateData;
    private PreparedStatement pstmDelete;
    private PreparedStatement pstmInsert;
    private PreparedStatement pstmallDatax;

    @FXML
    private Button btnSave;
    @FXML
    private Button btnDelete;
    @FXML
    private AnchorPane root;
    @FXML
    private TextField txtCustomerId;
    @FXML
    private TextField txtCustomerName;
    @FXML
    private TextField txtCustomerAddress;
    @FXML
    private TableView<CustomerTM> tblCustomers;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnSave.setDisable(true);
        txtCustomerId.setDisable(true);
        txtCustomerAddress.setDisable(true);
        txtCustomerName.setDisable(true);
        btnDelete.setDisable(true);


        //Mapping table
        tblCustomers.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblCustomers.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblCustomers.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));

        //Disable


        //Make Class Object
        try {
            Class.forName("com.mysql.jdbc.Driver");

            //Connection singleton pattern
            Connection connection = DBConnection.getConnection();
            //All Data
            pstmallData = connection.prepareStatement("SELECT * FROM Customer");
            //Update Data
            pstmUpdateData = connection.prepareStatement("UPDATE Customer SET Name = ?, Address =? where ID = ?");
            //Delete Data
            pstmDelete = connection.prepareStatement("DELETE FROM customer where  ID =?");
            //All code
            pstmallDatax = connection.prepareStatement("SELECT ID FROM customer ORDER BY ID desc LIMIT 1");
            //Insert All Data
            pstmInsert = connection.prepareStatement("INSERT INTO Customer VALUES (?,?,?)");
            //Calling load All Method for load table all customer data
            loadAllCustomers();

        } catch (Exception e) {
            e.printStackTrace();
        }

        tblCustomers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<CustomerTM>() {
            @Override
            public void changed(ObservableValue<? extends CustomerTM> observable, CustomerTM oldValue, CustomerTM newValue) {
                CustomerTM selectedItem = tblCustomers.getSelectionModel().getSelectedItem();

                if (selectedItem == null) {
                    btnSave.setText("Save");
                    btnDelete.setDisable(true);
                    return;
                }

                btnSave.setText("Update");
                btnSave.setDisable(false);
                btnDelete.setDisable(false);
                txtCustomerName.setDisable(false);
                txtCustomerAddress.setDisable(false);
                txtCustomerId.setText(selectedItem.getId());
                txtCustomerName.setText(selectedItem.getName());
                txtCustomerAddress.setText(selectedItem.getAddress());
            }
        });

    }

    //Load All Method Definition to load all customers to the table
    private void loadAllCustomers() throws SQLException {
        tblCustomers.getItems().clear();

        ResultSet rst = pstmallData.executeQuery();

        ObservableList<CustomerTM> customers = tblCustomers.getItems();

        while (rst.next()) {
            String id = rst.getString("id");
            String name = rst.getString("name");
            String address = rst.getString("address");

            CustomerTM customer = new CustomerTM(id, name, address);
            customers.add(customer);


        }

    }

    // Home icon navigate to home
    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/view/MainForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    //Save button to add customer to database
    @FXML
    private void btnSave_OnAction(ActionEvent event) throws SQLException {


        String name = txtCustomerName.getText();
        String name1 = "";
        if (Pattern.matches("^[A-Za-z][A-Za-z. ]+", name)) {
            name1 = name;
        }
        if (!Pattern.matches("^[A-Za-z][A-Za-z. ]+", name)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Please Enter Characters for name",
                    ButtonType.OK);
            alert.showAndWait();
            txtCustomerName.requestFocus();
            return;
        }


        String address = txtCustomerAddress.getText();
        String address1 = "";

        if (Pattern.matches("^[A-Za-z0-9]+[A-Za-z0-9., ]+", address)) {
            address1 = address;
        }
        if (!Pattern.matches("^[A-Za-z0-9]+[A-Za-z0-9., ]+", address)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Please Enter Characters for address",
                    ButtonType.OK);
            alert.showAndWait();
            txtCustomerAddress.requestFocus();
            return;
        }

        //Add customer to database

        if (btnSave.getText().equals("Save")) {
            pstmInsert.setString(1, txtCustomerId.getText());
            pstmInsert.setString(2, txtCustomerName.getText());
            pstmInsert.setString(3, txtCustomerAddress.getText());

            if (pstmInsert.executeUpdate() > 0) {
                loadAllCustomers();
                txtCustomerName.clear();
                txtCustomerAddress.clear();
                txtCustomerId.clear();
                txtCustomerId.requestFocus();
                txtCustomerId.selectAll();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to save").show();
                txtCustomerId.requestFocus();
                txtCustomerId.selectAll();
            }

            number();
        }
        //If click a Table cell
        else {
            CustomerTM selectedItem = tblCustomers.getSelectionModel().getSelectedItem();
            pstmUpdateData.setString(1, txtCustomerName.getText());
            pstmUpdateData.setString(2, txtCustomerAddress.getText());
            pstmUpdateData.setString(3, txtCustomerId.getText());

            pstmUpdateData.executeUpdate();
            loadAllCustomers();
            tblCustomers.refresh();
            txtCustomerId.clear();
            txtCustomerName.clear();
            txtCustomerAddress.clear();
            btnSave.setText("Save");
            number();
        }
    }

    void number() {
        try {
            ResultSet rst1 = pstmallDatax.executeQuery();
            if (rst1.next()) {
                String s = rst1.getString(1);
                String[] sp = s.split(":");
                int te = Integer.parseInt(sp[1]);
                te++;


                String code = "";
                if (te < 10) {
                    code = "C:00" + te;
                } else if (te < 100) {
                    code = "C:0" + te;
                } else {
                    code = "C:" + te;
                }
                txtCustomerId.setText(code);
            } else {
                txtCustomerId.setText("C:001");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Button Delete

    @FXML
    private void btnDelete_OnAction(ActionEvent event) throws SQLException {
        String id = "";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure whether you want to delete this customer?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.get() == ButtonType.YES) {
            CustomerTM selectedItem = tblCustomers.getSelectionModel().getSelectedItem();
            tblCustomers.getItems().remove(selectedItem);
            id = selectedItem.getId();

            //Delete customer from Database
            ResultSet rst = pstmallData.executeQuery();

            while (rst.next()) {
                String ida = rst.getString("ID");
                if (ida.equals(id)) {

                    try {
                        pstmDelete.setString(1, ida);
                        int row = pstmDelete.executeUpdate();
                        tblCustomers.refresh();
                        if (row > 0) {
                            loadAllCustomers();
                        }
                    } catch (Exception e) {
                        Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION,
                                "Cannot delete or update a parent row: a foreign key constraint fails ",
                                ButtonType.OK);
                        alert2.showAndWait();
                        loadAllCustomers();
                        btnSave.setText("Update");
                        return;
                    }
                    //btnSave.setDisable(true);
                    btnSave.setText("Update");
                    return;
                }
            }

        }


    }


    //Button Add new Customer On Action
    @FXML
    private void btnAddNew_OnAction(ActionEvent actionEvent) throws SQLException {
        txtCustomerId.clear();
        txtCustomerName.clear();
        txtCustomerAddress.clear();
        tblCustomers.getSelectionModel().clearSelection();
        txtCustomerName.setDisable(false);
        txtCustomerAddress.setDisable(false);
        txtCustomerName.requestFocus();
        btnSave.setDisable(false);
        btnSave.setText("Save");
        number();

    }

    public void reportOnAction(ActionEvent actionEvent) throws Exception {
        JasperDesign load = JRXmlLoader.load(ManageCustomerFormController.class.getResourceAsStream("/report/HelloJasper.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(load);
        Map<String, Object> params = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params,new JRBeanCollectionDataSource(tblCustomers.getItems()));
        JasperViewer.viewReport(jasperPrint, false);
    }

    public void navigate(MouseEvent event) throws IOException {

        if (event.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) event.getSource();

            Parent root1 = null;

            switch (icon.getId()) {
                case "store":
                    root1 = FXMLLoader.load(this.getClass().getResource("/view/ManageItemForm.fxml"));
                    break;
                case "place":
                    root1 = FXMLLoader.load(this.getClass().getResource("/view/PlaceOrderForm.fxml"));
                    break;
                case "issue":
                    root1 = FXMLLoader.load(this.getClass().getResource("/view/search.fxml"));
                    break;
            }

            if (root1 != null) {
                Scene subScene = new Scene(root1);
                Stage primaryStage = (Stage) this.root.getScene().getWindow();
                primaryStage.setScene(subScene);
                primaryStage.centerOnScreen();

                TranslateTransition tt = new TranslateTransition(Duration.millis(350), subScene.getRoot());
                tt.setFromX(-subScene.getWidth());
                tt.setToX(0);
                tt.play();

            }
        }
    }

    public void enter(MouseEvent event) {
        if (event.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) event.getSource();
            ScaleTransition scaleT = new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1.2);
            scaleT.setToY(1.2);
            scaleT.play();

            DropShadow glow = new DropShadow();
            glow.setColor(Color.GREEN);
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(60);
            icon.setEffect(glow);
        }
    }

    public void exit(MouseEvent event) {
        if (event.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) event.getSource();
            ScaleTransition scaleT = new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1);
            scaleT.setToY(1);
            scaleT.play();
            icon.setEffect(null);
        }
    }

    public void txtcustomerName(ActionEvent actionEvent) {
        txtCustomerAddress.requestFocus();
    }

    public void txtCustomerAddress(ActionEvent actionEvent) {
        btnSave.requestFocus();
    }
}
