java_library(
  name = "jar",
  srcs = glob(["src/main/java/**/*.java"]),
  deps = [
    "@com_google_guava//jar",
    "@com_google_gwt_user//jar"
  ],
  visibility = ["//visibility:public"]
)

java_library(
  name = "tests",
  srcs = glob(["src/test/java/**/*.java"]),
  deps = [
    ":jar",
    "//util/test:jar",

    "@junit//jar",
    "@org_mockito//jar"
  ],
  visibility = ["//visibility:public"]
)