package zenlife.db;

import ez.DB;

public class ZenDB {

  protected static DB db;

  public ZenDB() {
    if (db == null) {
      db = new DB("localhost", "root", "", "zenlife");
    }
  }

}
