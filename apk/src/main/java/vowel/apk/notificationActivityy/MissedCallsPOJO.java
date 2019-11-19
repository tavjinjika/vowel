package vowel.apk.notificationActivityy;

class MissedCallsPOJO {

  private String name;
  private String time;

  MissedCallsPOJO(String name, String time) {
    this.name = name;
    this.time = time;
  }
//format the listview object
  String getNameU() {
    return name;
  }


  String getTime() {
    return time;
  }


}
