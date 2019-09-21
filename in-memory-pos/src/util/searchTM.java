package util;

public class searchTM {
    private String id;
    private String date;
    private String cusid;
    private String name;
    private double total;

    public searchTM(String id, String date, String cusid, String name) {
        this.id = id;
        this.date = date;
        this.cusid = cusid;
        this.name = name;

    }

    @Override
    public String toString() {
        return "searchTM{" +
                "id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", cusid='" + cusid + '\'' +
                ", name='" + name + '\'' +

                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCusid() {
        return cusid;
    }

    public void setCusid(String cusid) {
        this.cusid = cusid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
