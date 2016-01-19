package jetbrains.jetpad.model.id;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class BaseIdGenerator {
  private static final int DEFAULT_NUM_IDS = 5;
  private static final String USAGE = "usage: num_ids file_to_save_in";

  public static void main(String[] args) {
    int numIds;
    if (args.length >= 1) {
      try {
        numIds = Integer.parseInt(args[0]);
      } catch (Exception e) {
        System.out.println(USAGE);
        return;
      }
    } else {
      numIds = DEFAULT_NUM_IDS;
    }

    OutputStream out;
    if (args.length >= 2) {
      try {
        out = new FileOutputStream(args[1]);
      } catch (Exception e) {
        System.out.println(USAGE);
        return;
      }
    } else {
      out = System.out;
    }

    try (PrintWriter writer = new PrintWriter(out)) {
      for (int i = 0; i < numIds; i++) {
        writer.println("\"" + new MyId().getId() + "\"");
      }
    }
  }

  private static class MyId extends BaseId {}
}
