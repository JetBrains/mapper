filegroup(
  name = "gwt_files",
  srcs = glob([
    "src/main/java/**/*.gwt.xml",
    "src/main/java/**/*.java"
  ])
)

java_library(
  name = "jar",
  srcs = glob(["src/main/java/**/*.java"]),
  resources = [":gwt_files"],
  deps = [
    "//util/base:jar",
    "//model:jar",
    "//mapper:jar",
    "//util/geometry:jar",

    "@com_googlecode_gwtquery//jar",
    "@com_google_gwt_user//jar",
    "@com_google_gwt_elemental//jar"
  ],
  visibility = ["//visibility:public"]
)