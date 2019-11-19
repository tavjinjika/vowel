package vowel.apk.notificationActivityy;

public class MessagePOJO {

  private String name;
  private String content;
  private String time;

  MessagePOJO(String name, String content, String time) {
    this.name = name;
    this.content = content;
    this.time = time;
  }

//Format the text for listview object

  String getNameU() {
    return name;
  }

  String getTime() {
    return time;
  }


  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}