load("@io_bazel_rules_gwt//gwt:gwt.bzl", "gwt_application")

java_library(
  name = "jar",
  srcs = glob(["src/main/java/**/*.java"]),
  deps = [
    "//mapper:jar",
    "//model:jar",
    "//gwt-util:jar",
    "//util/base:jar",

    "@com_google_gwt_user//jar",
    "@com_googlecode_gwtquery//jar"
  ],
  visibility = ["//visibility:public"]
)

gwt_application(
  name = "gwt_app",
  srcs = glob(["src/main/java/**/*.java"]),
  resources = glob(["src/main/java/**/*.gwt.xml", "src/main/java/**/*.ui.xml"]),
  modules = ["jetbrains.jetpad.samples.todo.Todo"],
  deps = [
    "//mapper:jar",
    "//model:jar",
    "//gwt-util:jar",
    "//util/base:jar",

    "@com_google_gwt_user//jar",
    "@com_googlecode_gwtquery//jar"
  ],

)