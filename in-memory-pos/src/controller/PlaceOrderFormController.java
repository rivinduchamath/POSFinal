package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import db.DBConnection;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import util.OrderDetailTM;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceOrderFormController {
    public JFXTextField txtDescription;
    public JFXTextField txtCustomerName;
    public JFXTextField txtQtyOnHand;
    public JFXButton btnSave;
    public TableView<OrderDetailTM> tblOrderDetails;
    public JFXTextField txtUnitPrice;
    public JFXComboBox<String> cmbCustomerId;
    public JFXComboBox<String> cmbItemCode;
    public JFXTextField txtQty;
    public Label lblTotal;
    public JFXButton btnPlaceOrder;
    public AnchorPane root;
    public Label lblId;
    public Label lblDate;
    public Label balence;
    public JFXTextField cash;
    PreparedStatement pstmitem;
    private PreparedStatement pstmForQuery;
    private PreparedStatement pstmForQuery1;
    private PreparedStatement pstmallData;
    private PreparedStatement pstmInsert;
    private PreparedStatement pstmInsert1;
    private PreparedStatement pstmForQueryOT;

    public void initialize() throws SQLException, ClassNotFoundException {

        //Disable
        cmbItemCode.setDisable(true);
        txtQty.setDisable(true);
        txtUnitPrice.setDisable(true);
        txtCustomerName.setDisable(true);
        txtDescription.setDisable(true);
        txtQtyOnHand.setDisable(true);
        //Mapping Table data
        tblOrderDetails.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblOrderDetails.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblOrderDetails.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qty"));
        tblOrderDetails.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tblOrderDetails.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("total"));
        tblOrderDetails.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("btnDelete"));

        Class.forName("com.mysql.jdbc.Driver");

        //Connection singleton pattern
        Connection connection = null;
        try {
            connection = DBConnection.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //All Id Data for customer combo box
        pstmForQuery = connection.prepareStatement("SELECT id FROM customer");

        // All data from OrderDetail table
        pstmallData = connection.prepareStatement("SELECT * FROM orderx");

        // All data from Item table
        pstmForQueryOT = connection.prepareStatement("SELECT * FROM item");

        // All data from Item table
        pstmitem = connection.prepareStatement("UPDATE item set qtyOnHand = ? where code=?");

        //Insert Data to the Orderx Table
        pstmInsert = connection.prepareStatement("INSERT INTO orderx VALUES (?,?,?)");

        //Insert Data to the orderdetail Table
        pstmInsert1 = connection.prepareStatement("INSERT INTO orderdetail VALUES (?,?,?,?,?)");

        //load All Id Data for customer combo Box
        cmbCustomerId.getItems().clear();
        ResultSet rst = pstmForQuery.executeQuery();
        while (rst.next()) {
            String ids = rst.getString("id");
            ObservableList<String> members = cmbCustomerId.getItems();
            members.add(ids);
        }

        //load All ItemCode Data to combo Box
        pstmForQuery1 = connection.prepareStatement("SELECT code FROM item");
        ResultSet rst1 = pstmForQuery1.executeQuery();
        while (rst1.next()) {
            String ids = rst1.getString("code");
            ObservableList<String> members = cmbItemCode.getItems();
            members.add(ids);
        }

        //customer combo select and relevant name
        Connection finalConnection = connection;
        cmbCustomerId.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                enablePlaceOrderButton();
                String sql = "SELECT Name FROM customer WHERE ID = ?";
                PreparedStatement pst = null;
                try {
                    pst = finalConnection.prepareStatement(sql);

                    pst.setString(1, newValue.toString());

                    ResultSet resultSet = pst.executeQuery();
                    while (resultSet.next()) {
                        String name = resultSet.getString(1);
                        txtCustomerName.setText(name);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        //Item combo select and set relevant description, qty On hand,unite price
        Connection finalConnection1 = connection;
        cmbItemCode.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                enablePlaceOrderButton();
                String sql = "SELECT description,qtyOnHand,unitePrice FROM item WHERE code = ?";

                try {
                    PreparedStatement pst = finalConnection1.prepareStatement(sql);
                    pst.setString(1, newValue.toString());

                    ResultSet resultSet = pst.executeQuery();
                    while (resultSet.next()) {
                        String des = resultSet.getString(1);
                        String des1 = resultSet.getString(2);
                        String des3 = resultSet.getString(3);

                        txtDescription.setText(des);
                        txtQtyOnHand.setText(des1);
                        txtUnitPrice.setText(des3);
                        txtQty.requestFocus();
                        txtQty.setEditable(true);
                        btnSave.setDisable(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        //Table click event
        tblOrderDetails.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<OrderDetailTM>() {
            @Override
            public void changed(ObservableValue<? extends OrderDetailTM> observable, OrderDetailTM oldValue, OrderDetailTM newValue) {

                OrderDetailTM selectedOrderDetail = tblOrderDetails.getSelectionModel().getSelectedItem();
                if (selectedOrderDetail == null) {
                    btnSave.setText("Add");
                    return;
                } else {

                }
                cmbItemCode.getSelectionModel().select(selectedOrderDetail.getCode());
                txtQty.setText(selectedOrderDetail.getQty() + "");
                int x = Integer.parseInt(txtQtyOnHand.getText());
                txtQtyOnHand.setText(String.valueOf(selectedOrderDetail.getQty() + x));

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        txtQty.requestFocus();
                        txtQty.selectAll();

                    }
                });
                btnSave.setText("Update");
            }
        });

        reset();
    }


    private void reset() {
        lblDate.setText(LocalDate.now() + "");

        btnPlaceOrder.setDisable(true);
        btnSave.setDisable(true);
        txtCustomerName.setEditable(false);
        txtCustomerName.clear();
        txtDescription.setEditable(false);
        txtUnitPrice.setEditable(false);
        txtQtyOnHand.setEditable(false);
        txtQty.setEditable(false);
        tblOrderDetails.getItems().clear();
        lblTotal.setText("Total : 0.00");

        int maxOrderId = 1;
        ResultSet rst;
        try {
            rst = pstmallData.executeQuery();

            while (rst.next()) {
                maxOrderId++;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (maxOrderId < 10) {
            lblId.setText("OD00" + maxOrderId);
        } else if (maxOrderId < 100) {
            lblId.setText("OD0" + maxOrderId);
        } else {
            lblId.setText("OD" + maxOrderId);
        }
    }

    //Add New Button On Action
    public void btnAddNew_OnAction(ActionEvent actionEvent) throws Exception {
        tblOrderDetails.getItems().clear();
        txtQtyOnHand.clear();
        txtQty.clear();
        txtDescription.clear();
        txtUnitPrice.clear();
        txtCustomerName.clear();
        //txtCustomerName.setDisable(false);
        txtQty.setDisable(false);



        cmbCustomerId.setDisable(false);
        cmbItemCode.setDisable(false);

        reset();
    }

    //Add And set data to the table
    public void btnAdd_OnAction(ActionEvent actionEvent) throws SQLException {

        int qtyOnHand = Integer.parseInt(txtQtyOnHand.getText());
        double unitPrice = Double.parseDouble(txtUnitPrice.getText());


        String regex = "[+]?[0-9][0-9]*";
        Pattern p = Pattern.compile(regex);
        String q = (txtQty.getText());
        int qty = 0;

        Matcher m = p.matcher(q);

        if (m.find() && m.group().equals(q)) {
            qty = Integer.parseInt(q);
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    q + " is not a valid integer",
                    ButtonType.OK);
            alert.showAndWait();
            txtQty.requestFocus();
            return;
        }


        if (qty <= 0 || qty > qtyOnHand) {
            new Alert(Alert.AlertType.ERROR, "Invalid Qty", ButtonType.OK).show();
            txtQty.requestFocus();
            txtQty.selectAll();
            return;
        }//End Validations

        ResultSet rst = pstmForQuery.executeQuery();//SELECT id FROM customer
        String selectedItemCode = cmbItemCode.getSelectionModel().getSelectedItem();
        ObservableList<OrderDetailTM> details = tblOrderDetails.getItems();
        boolean isExists = false;
        for (OrderDetailTM detail : tblOrderDetails.getItems()) {
            //Get Table Items
            if (detail.getCode().equals(selectedItemCode)) { //Check  new Add Already Added To The Table
                isExists = true;

                if (btnSave.getText().equals("Update")) {
                    detail.setQty(qty);
                    detail.setQty(detail.getQty());
                    txtQtyOnHand.setText((qtyOnHand - qty) + "");

                } else {
                    detail.setQty(detail.getQty() + qty);
                    txtQtyOnHand.setText((qtyOnHand - qty) + "");
                    calculateTotal();
                }
                detail.setTotal(detail.getQty() * detail.getUnitPrice());
                tblOrderDetails.refresh();
                lblTotal.setText(detail.getTotal() + "");
                btnSave.setText("Add");
                calculateTotal();
                tblOrderDetails.getSelectionModel().isEmpty();
                break;
            }
        }
        if (!isExists) {
            JFXButton btnDelete = new JFXButton("Delete");
            OrderDetailTM detailTM = new OrderDetailTM(cmbItemCode.getSelectionModel().getSelectedItem(),
                    txtDescription.getText(),
                    qty,
                    unitPrice,
                    qty * unitPrice,
                    btnDelete
            );
            txtQtyOnHand.setText((qtyOnHand - qty) + "");
            calculateTotal();

            btnDelete.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    tblOrderDetails.getItems().remove(detailTM);
                    calculateTotal();
                    enablePlaceOrderButton();
                    cmbItemCode.requestFocus();
                    tblOrderDetails.getSelectionModel().clearSelection();
                }
            });
            details.add(detailTM);

        }

        calculateTotal();

        enablePlaceOrderButton();
        cmbItemCode.requestFocus();
        tblOrderDetails.getSelectionModel().isEmpty();
        tblOrderDetails.getSelectionModel().clearSelection();

    }

    private void updateQty(String selectedItemCode, int qty) throws SQLException {
        ResultSet rst = null;
        rst = pstmForQueryOT.executeQuery();
        while (rst.next()) {
            if (rst.getString("code").equals(selectedItemCode)) {
                int qtyonhand = rst.getInt("qtyOnHand");
                int newqty = qtyonhand - qty;
                pstmitem.setInt(1, newqty);
                pstmitem.setString(2, selectedItemCode);
                int i = pstmitem.executeUpdate();
                if (i > 0) {

                }
                break;
            }
        }
    }

    public void calculateTotal() {
        ObservableList<OrderDetailTM> details = tblOrderDetails.getItems();

        double total = 0;
        for (OrderDetailTM detail : details) {// While all Order items Price
            total += detail.getTotal();
        }

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);

        lblTotal.setText("Total : " + nf.format(total));
    }

    //btnplace order onaction
    public void btnPlaceOrder_OnAction(ActionEvent actionEvent) throws Exception {
        ObservableList<OrderDetailTM> details = tblOrderDetails.getItems();

        //Set Values to the Orderx Database
        pstmInsert.setString(1, lblId.getText());
        pstmInsert.setString(2, cmbCustomerId.getSelectionModel().getSelectedItem());
        pstmInsert.setDate(3, Date.valueOf(lblDate.getText()));


        if (pstmInsert.executeUpdate() > 0) {

            //Insert data to the OrderDetails Database
            for (OrderDetailTM items : details) { // While all Order items
                pstmInsert1.setString(1, lblId.getText());
                pstmInsert1.setString(2, items.getCode());
                pstmInsert1.setDate(3, Date.valueOf(lblDate.getText()));
                pstmInsert1.setDouble(4, items.getUnitPrice());
                pstmInsert1.setInt(5, items.getQty());

                if (pstmInsert1.executeUpdate() > 0) {
                    updateQty(items.getCode(), items.getQty());
                }
            }
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(this.getClass().getResourceAsStream("/report/mainForm2.jasper"));
            Map<String, Object> params = new HashMap<>();
            params.put("id", lblId.getText());
            params.put("total", lblTotal.getText());
            params.put("Cash", cash.getText());
            params.put("change", balence.getText());
            params.put("date", lblDate.getText());

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, DBConnection.getConnection());
            JasperViewer.viewReport(jasperPrint, false);
            reset();

            txtDescription.clear();
        } else {
            new Alert(Alert.AlertType.ERROR, "Failed to save").show();
            lblId.requestFocus();
        }


    }


    //image icon Home
    @FXML
    private void navigateToHome(MouseEvent event) throws IOException {
        URL resource = this.getClass().getResource("/view/MainForm.fxml");
        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }


    public void txtQty_OnAction(ActionEvent actionEvent) {

    }

    private void enablePlaceOrderButton() {
        String selectedCustomer = cmbCustomerId.getSelectionModel().getSelectedItem();
        int size = tblOrderDetails.getItems().size();
        if (selectedCustomer == null || size == 0) {
            btnPlaceOrder.setDisable(true);
        } else {
            btnPlaceOrder.setDisable(false);
        }
    }


    public void combocusId(ActionEvent actionEvent) {
        btnSave.setText("Add");
        cmbItemCode.setDisable(false);
    }

    public void comboItemId(ActionEvent actionEvent) {
        btnSave.setText("Add");
        txtQty.setDisable(false);
    }

    public void btnReport_OnAction(ActionEvent actionEvent) throws Exception {
    }

    public void navigate(MouseEvent event) throws IOException {

        if (event.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) event.getSource();

            Parent root1 = null;

            switch (icon.getId()) {
                case "cus":
                    root1 = FXMLLoader.load(this.getClass().getResource("/view/ManageCustomerForm.fxml"));
                    break;
                case "place":
                    root1 = FXMLLoader.load(this.getClass().getResource("/view/ManageItemForm.fxml"));
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

    public void cashOnAction(ActionEvent actionEvent) {
        ObservableList<OrderDetailTM> details = tblOrderDetails.getItems();

        double total = 0;
        for (OrderDetailTM detail : details) {// While all Order items Price
            total += detail.getTotal();
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);
        double x = Double.parseDouble(cash.getText());
        total = x - total;
        balence.setText("Balance : " + nf.format(total));
    }
}//End Of Class PlaceOrderForm Controller