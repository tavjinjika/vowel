package vowel.apk.callActivity;

public class Contactlist {

  private String name;

  Contactlist(String name) {
    this.name = name;
  }

  @Override

//set the format for the listview child item
  public String toString() {

    // TODO Auto-generated method stub

    return this.name;


  }

}