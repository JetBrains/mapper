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
    "//util/geometry:jar",
    "//util/base:jar",
    "@com_google_guava//jar"
  ],
  visibility = ["//visibility:public"]
)

java_library(
  name = "tests",
  srcs = glob(["src/test/java/**/*.java"]),
  deps = [
    ":jar",
    "//util/base:jar",
    "//util/test:jar",
    "//util/geometry:jar",

    "@junit//jar",
    "@org_mockito//jar"
  ]
)

java_test(
  name = "run_tests",
  test_class = "jetbrains.jetpad.model.AllTests",
  size = "small",
  runtime_deps = [
    ":tests"
  ]
)