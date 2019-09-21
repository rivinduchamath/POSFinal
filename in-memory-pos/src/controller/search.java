package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import db.DBConnection;
import db.OrderDetail;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import util.OrderDetailTM;
import util.searchTM;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class search {
    public TableView<searchTM> tblser;
    public JFXTextField txts;
    public AnchorPane root2;
    public ImageView place;
    public ImageView ite;
    public ImageView cus;
    ObservableList<searchTM> orders = FXCollections.observableArrayList();
    public ImageView ww;
    PreparedStatement pstmSearch;
    PreparedStatement pstmForSearch;
    Connection connection;

    public void initialize() throws SQLException {

        //Mapping Table Data
        tblser.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblser.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
        tblser.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("cusid"));
        tblser.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("name"));

        try {
            try {
                connection = DBConnection.getConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }


            pstmSearch = connection.prepareStatement("\n" +
                            "SELECT DISTINCT orderx.Orderxid," +
                    "orderx.Orderdate,ID,Name FROM orderx " +
                    "INNER JOIN orderdetail ON orderdetail.oderid = orderx.Orderxid" +
                    " INNER JOIN customer ON orderx.cusid = customer.ID;\n");


            pstmForSearch = connection.prepareStatement("" +
                    "SELECT DISTINCT orderx.Orderxid,orderx.Orderdate," +
                    "customer.ID,Name FROM orderx " +
                    "INNER JOIN orderdetail ON orderdetail.oderid = orderx.Orderxid" +
                    " INNER JOIN customer ON orderx.cusid = customer.ID" +
                    " WHERE orderx.Orderxid LIKE ? " +
                    "OR orderx.Orderdate LIKE ? OR orderx.cusid LIKE ? OR Name LIKE ? ");

            ResultSet rst = pstmSearch.executeQuery();
            while (rst.next()) {

                orders.add(new searchTM(rst.getString("Orderxid"),
                        rst.getString("Orderdate"),
                        rst.getString("ID"),
                        rst.getString("Name")

                ));
            }
            tblser.setItems(orders);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        txts.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                tblser.getItems().clear();
                try {
                    pstmForSearch.setString(1, "%" + txts.getText() + "%");
                    pstmForSearch.setString(2, "%" + txts.getText() + "%");
                    pstmForSearch.setString(3, "%" + txts.getText() + "%");
                    pstmForSearch.setString(4, "%" + txts.getText() + "%");

                    ResultSet rst = pstmForSearch.executeQuery();
                    ObservableList<searchTM> customers = tblser.getItems();

                    while (rst.next()) {

                        String orderId = rst.getString("Orderxid");
                        String orderdate = rst.getString("Orderdate");
                        String custId = rst.getString("ID");
                        String cusName = rst.getString("Name");

                        searchTM searchDetails = new searchTM(orderId, orderdate, custId, cusName);
                        customers.add(searchDetails);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void clickeve(MouseEvent mouseEvent) throws IOException, SQLException {
        if (mouseEvent.getClickCount() == 2) {
            String orderID = tblser.getSelectionModel().getSelectedItem().getId();
            FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/view/PlaceOrderForm.fxml"));
            Parent root = fxmlLoader.load();
            Scene newScene = new Scene(root);
            Stage primaryStage = (Stage) tblser.getScene().getWindow();
            primaryStage.setScene(newScene);
            PlaceOrderFormController poController = fxmlLoader.getController();
            poController.lblId.setText(orderID);
            poController.cmbCustomerId.getItems().removeAll();

            PreparedStatement pstmload = connection.prepareStatement("SELECT orderdetail.ItemCodeF,item.description,orderdetail.qty,orderdetail.unitePrice,orderx.Orderxid,ID,Name" +
                    " FROM (orderdetail INNER JOIN orderx ON orderdetail.oderid = orderx .Orderxid " +
                    "INNER JOIN item ON orderdetail.ItemCodeF = item.code INNER JOIN customer ON orderx.cusid = customer.ID) WHERE orderdetail.oderid=?");
            pstmload.setString(1, poController.lblId.getText());

            ResultSet rs = pstmload.executeQuery();
            while (rs.next()) {
                double qty = Double.parseDouble(rs.getString("qty"));
                double price = Double.parseDouble(rs.getString("unitePrice"));
                poController.txtCustomerName.setText(rs.getString("Name"));
                JFXButton btnDelete = new JFXButton("Delete");
                OrderDetailTM detailTM = new OrderDetailTM(rs.getString("ItemCodeF"),
                        rs.getString("description"),
                        rs.getInt("qty"),
                        rs.getDouble("unitePrice"),
                        qty * price,
                        btnDelete
                );
                ObservableList<OrderDetailTM> details = poController.tblOrderDetails.getItems();
                details.add(detailTM);
                poController.calculateTotal();
                poController.tblOrderDetails.setDisable(true);
                poController.cmbCustomerId.setDisable(true);
                poController.txtDescription.setDisable(true);
                poController.txtQty.setDisable(true);
                poController.txtQtyOnHand.setDisable(true);
                poController.cmbItemCode.setDisable(true);
                poController.txtUnitPrice.setDisable(true);
                poController.txtCustomerName.setDisable(true);
            }
        }
    }


    public void searchon(KeyEvent keyEvent) {
    }


    public void navigate(MouseEvent event) throws IOException {

        if (event.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) event.getSource();

            Parent root1 = null;

            switch (icon.getId()) {
                case "ww":
                    root1 = FXMLLoader.load(this.getClass().getResource("/view/MainForm.fxml"));
                    break;
                case "cus":
                    root1 = FXMLLoader.load(this.getClass().getResource("/view/ManageCustomerForm.fxml"));
                    break;
                case "ite":
                    root1 = FXMLLoader.load(this.getClass().getResource("/view/ManageItemForm.fxml"));
                    break;
                case "place":
                    root1 = FXMLLoader.load(this.getClass().getResource("/view/PlaceOrderForm.fxml"));
                    break;
            }

            if (root1 != null) {
                Scene subScene = new Scene(root1);
                Stage primaryStage = (Stage) this.root2.getScene().getWindow();
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

    public void btnreport(ActionEvent actionEvent) throws Exception {

        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(this.getClass().getResourceAsStream("/report/newPOs.jasper"));
        Map<String, Object> params = new HashMap<>();
        params.put("Parameter1", txts.getText());
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, DBConnection.getConnection());
        JasperViewer.viewReport(jasperPrint, false);}
}
