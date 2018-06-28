package neon.ovis;

public class Notification {
    private String id;
    private String title;
    private String msg;
    private String date;

    public Notification(String title, String msg, String date) {
        this.title = title;
        this.msg = msg;
        this.date = date;
    }

    public Notification(String id, String title, String msg, String date) {
        this.id = id;
        this.title = title;
        this.msg = msg;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
