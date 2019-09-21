package controller;

import com.jfoenix.controls.JFXTextField;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
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
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import util.ItemTM;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ManageItemFormController implements Initializable {

    public JFXTextField txtCode;
    public JFXTextField txtDescription;
    public JFXTextField txtQtyOnHand;
    public TableView<ItemTM> tblItems;
    public JFXTextField txtUnitPrice;
    public ImageView issue;
    public ImageView place;
    public ImageView cus;
    Connection connection;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnDelete;
    @FXML
    private AnchorPane root;
    private PreparedStatement pstmallData;
    private PreparedStatement pstmallDatax;
    private PreparedStatement pstmUpdateData;
    private PreparedStatement pstmForQuery1;
    private PreparedStatement pstmDelete;
    private PreparedStatement pstmInsert;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //Mapping table
        tblItems.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("code"));
        tblItems.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tblItems.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("qtyOnHand"));
        tblItems.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        try {
            Class.forName("com.mysql.jdbc.Driver");

            //Connection singleton pattern
            Connection connection = DBConnection.getConnection();
            //All Data
            pstmallData = connection.prepareStatement("SELECT * FROM item");
            //All code
            pstmallDatax = connection.prepareStatement("SELECT code FROM item ORDER BY code desc LIMIT 1");
            //Update Data
            pstmUpdateData = connection.prepareStatement("UPDATE item SET description = ?, qtyOnHand =? , unitePrice = ? where code = ?");

            PreparedStatement pstm = connection.prepareStatement("UPDATE item SET description = ?, qtyOnHand =? , unitePrice = ? where code = ?");
            //Delete Data
            pstmDelete = connection.prepareStatement("DELETE FROM item where  code =?");
            //Insert All Data
            pstmInsert = connection.prepareStatement("INSERT INTO item VALUES (?,?,?,?)");
            //Calling load All Method to load all Items
            loadAllItems();

        } catch (Exception e) {
            e.printStackTrace();
        }

        txtCode.setDisable(true);
        txtDescription.setDisable(true);
        txtQtyOnHand.setDisable(true);
        txtUnitPrice.setDisable(true);
        btnDelete.setDisable(true);
        btnSave.setDisable(true);


        tblItems.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ItemTM>() {
            @Override
            public void changed(ObservableValue<? extends ItemTM> observable, ItemTM oldValue, ItemTM newValue) {
                ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();

                if (selectedItem == null) {
                    btnSave.setText("Save");
                    btnDelete.setDisable(true);
                    return;
                }

                btnSave.setText("Update");
                btnSave.setDisable(false);
                btnDelete.setDisable(false);
                txtDescription.setDisable(false);
                txtQtyOnHand.setDisable(false);
                txtUnitPrice.setDisable(false);
                txtCode.setText(selectedItem.getCode());
                txtDescription.setText(selectedItem.getDescription());
                txtQtyOnHand.setText(selectedItem.getQtyOnHand() + "");
                txtUnitPrice.setText(selectedItem.getUnitPrice() + "");

            }
        });
    }

    //Load All Method for load all items to the table
    private void loadAllItems() throws SQLException {
        tblItems.getItems().clear();

        //Getting all data from Database
        ResultSet rst = pstmallData.executeQuery();

        while (rst.next()) {
            String code = rst.getString("code");
            String description = rst.getString("description");
            int qtyOnHand = rst.getInt("qtyOnHand");
            double unitPrice = rst.getDouble("unitePrice");


            ObservableList<ItemTM> items = tblItems.getItems();
            ItemTM i = new ItemTM(code, description, qtyOnHand, unitPrice);
            items.add(i);

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
        String des = txtDescription.getText();
        String des1 = "";
        if (Pattern.matches("[a-zA-Z]{2,20}", des)) {
            des1 = des;
        }
        if (!Pattern.matches("[a-zA-Z]{2,20}", des)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Please Enter Characters for Description",
                    ButtonType.OK);
            alert.showAndWait();
            txtDescription.requestFocus();
            return;
        }

        String regex = "[+]?[0-9][0-9]*";
        Pattern p = Pattern.compile(regex);
        String qty = (txtQtyOnHand.getText());
        Matcher m = p.matcher(qty);
        if (m.find() && m.group().equals(qty)) {
            Integer.parseInt(qty);
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    qty + " is not a valid integer",
                    ButtonType.OK);
            alert.showAndWait();
            txtQtyOnHand.requestFocus();
            return;
        }
        String unitep = txtUnitPrice.getText();
        String reg = "^[1-9]\\d{0,7}(?:\\.\\d{1,4})?|\\.\\d{1,4}$";
        boolean result = false;
        Pattern pa= Pattern.compile(reg);


        result = pa.matcher(unitep).matches();
        if (result) {
            Double.parseDouble((unitep));
        } else{
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                unitep + " is not a valid Number",
                ButtonType.OK);
        alert.showAndWait();
        txtUnitPrice.requestFocus();
        return;
    }
        //If Button Text equal to  sava
        if (btnSave.getText().equals("Save")) {


            //Insert Data to the item Database
            pstmInsert.setString(1, txtCode.getText());
            pstmInsert.setString(2, txtDescription.getText());
            pstmInsert.setInt(3, Integer.parseInt(txtQtyOnHand.getText()));
            pstmInsert.setDouble(4, Double.parseDouble(txtUnitPrice.getText()));

            if (pstmInsert.executeUpdate() > 0) {
                loadAllItems();
                txtCode.clear();
                txtDescription.clear();
                txtQtyOnHand.clear();
                txtUnitPrice.clear();
                txtCode.clear();
                txtCode.requestFocus();
                txtCode.selectAll();
                cid();
                txtDescription.requestFocus();
            } else {
                new Alert(Alert.AlertType.ERROR, "Failed to save").show();
                txtCode.requestFocus();
                txtCode.selectAll();
            }
        }
        //If Select any cell in the table
        else {
            ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();
            pstmUpdateData.setString(4, txtCode.getText());
            pstmUpdateData.setString(1, txtDescription.getText());
            pstmUpdateData.setInt(2, Integer.parseInt(txtQtyOnHand.getText()));
            pstmUpdateData.setDouble(3, Double.parseDouble(txtUnitPrice.getText()));
            pstmUpdateData.executeUpdate();
            loadAllItems();
            tblItems.refresh();
            txtCode.clear();
            txtUnitPrice.clear();
            txtQtyOnHand.clear();
            txtDescription.clear();
            btnSave.setText("Save");
            txtDescription.requestFocus();
            cid();

        }
    }

    //Button Delete On Action
    @FXML
    private void btnDelete_OnAction(ActionEvent event) throws SQLException {

        String id = "";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you you want to delete this item?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();
        if (buttonType.get() == ButtonType.YES) {
            ItemTM selectedItem = tblItems.getSelectionModel().getSelectedItem();
            id = selectedItem.getCode();
            tblItems.getItems().remove(selectedItem);
            ResultSet rst1 = pstmallData.executeQuery();

            while (rst1.next()) {
                String code = rst1.getString("code");
                if (code.equals(id)) {
                    try {
                        pstmDelete.setString(1, code);
                        int row = pstmDelete.executeUpdate();
                        tblItems.refresh();
                        if (row > 0) {
                            loadAllItems();
                        }
                    } catch (Exception e) {
                        Alert alert2 = new Alert(Alert.AlertType.CONFIRMATION,
                                "Cannot delete or update a parent row: a foreign key constraint fails ",
                                ButtonType.OK);
                        alert2.showAndWait();
                        loadAllItems();
                        btnSave.setText("Update");
                        return;
                    }
                    btnSave.setText("Update");
                    return;
                }
            }//end while

        }//end alert
        txtCode.clear();
        txtUnitPrice.clear();
        txtQtyOnHand.clear();
        txtDescription.clear();
        cid();
    }

    void cid() {
        try {
            ResultSet rst = pstmallDatax.executeQuery();
            if (rst.next()) {
                String s = rst.getString(1);
                String[] sp = s.split(":");
                int te = Integer.parseInt(sp[1]);
                te++;


                String code = "";
                if (te < 10) {
                    code = "I:00" + te;
                } else if (te < 100) {
                    code = "I:0" + te;
                } else {
                    code = "I:" + te;
                }
                txtCode.setText(code);
            } else {
                txtCode.setText("I:001");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Click New Item Add Button

    @FXML
    private void btnAddNew_OnAction(ActionEvent actionEvent) {
        txtCode.clear();
        txtDescription.clear();
        txtQtyOnHand.clear();
        txtUnitPrice.clear();
        tblItems.getSelectionModel().clearSelection();
        txtDescription.setDisable(false);
        txtQtyOnHand.setDisable(false);
        txtUnitPrice.setDisable(false);
        txtDescription.requestFocus();
        btnSave.setDisable(false);

        // Generate a new Item id

        cid();
    }

    public void btnreport(ActionEvent event) throws Exception {

        JasperDesign load = JRXmlLoader.load(ManageItemFormController.class.getResourceAsStream("/report/Blank_A4.jrxml"));
        JasperReport jasperReport = JasperCompileManager.compileReport(load);
        Map<String, Object> params = new HashMap<>();
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, DBConnection.getConnection());
        JasperViewer.viewReport(jasperPrint, false);

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

    public void txtDescriptionOnAction(ActionEvent actionEvent) {
        txtQtyOnHand.requestFocus();
    }

    public void txtQtyOnHandOnAction(ActionEvent actionEvent) {
        txtUnitPrice.requestFocus();
    }

    public void txtUnitPriceOnAction(ActionEvent actionEvent) {
        btnSave.requestFocus();
    }
}
