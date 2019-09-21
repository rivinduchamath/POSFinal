package db;
import java.sql.*;

public class DBConnection {
    private static Connection connection;
    public static Connection getConnection() throws Exception{
        if (null==connection){
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/pos?" +
                    "createDatabaseIfNotExist=true&allowMultiQueries=true",
                    "root",
                    "1234");
            PreparedStatement pstm = connection.prepareStatement("SHOW TABLES");
            ResultSet resultSet = pstm.executeQuery();
            if (!resultSet.next()){
                String sql = "\n" +
                        "create table customer" +
                        "(ID varchar(20) not null primary key," +
                        "Name varchar(30) null," +
                        "Address varchar(30) null)" +
                        "ENGINE=InnoDB DEFAULT CHARSET=latin1;" +
                        "create table item" +
                        "(code varchar(20) not null primary key," +
                        "description varchar(40)   null," +
                        "qtyOnHand   int(20) null," +
                        " unitePrice  double(20, 2) null)" +
                        "ENGINE=InnoDB DEFAULT CHARSET=latin1;"+
                        "\n" +
                        "create table orderx(" +
                        "Orderxid  varchar(19) not null primary key," +
                        "cusid varchar(20) null," +
                        "Orderdate date null," +
                        "constraint orderx_ibfk_1 foreign key(cusid)references customer " +
                        "(ID));create index cusid on orderx (cusid)" +
                        "ENGINE=InnoDB DEFAULT CHARSET=latin1;"+
                        "\n" +
                        "\n" +
                        "create table orderdetail(" +
                        "oderid varchar(30) default '' " +
                        "not null,ItemCodeF  varchar(30) " +
                        "default '' not null, Orderdate  date null," +
                        "unitePrice double(20, 2) null,qty int(10) null," +
                        "primary key (oderid, ItemCodeF),constraint orderdetail_ibfk_1 " +
                        "foreign key (oderid) references orderx (Orderxid)," +
                        "constraint orderdetail_ibfk_2 foreign key (ItemCodeF) " +
                        "references item (code));create index ItemCodeF on orderdetail " +
                        "(ItemCodeF)" +
                        "ENGINE=InnoDB DEFAULT CHARSET=latin1;";
                pstm = connection.prepareStatement(sql);
                pstm.execute();
            }
        }
        return connection;
    }
}