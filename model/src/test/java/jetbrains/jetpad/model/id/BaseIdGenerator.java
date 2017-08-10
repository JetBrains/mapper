/*
 * Copyright 2012-2017 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.model.id;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
        String idString = "\"" + new MyId().getId() + "\"";
        if (i == 0) {
          copyToClipboard(idString);
        }
        writer.println(idString);
      }
    }
  }

  private static void copyToClipboard(String idString) {
    StringSelection stringSelection = new StringSelection(idString);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, null);
  }

  private static class MyId extends BaseId {}
}