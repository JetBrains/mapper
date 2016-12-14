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
  deps = ["@com_google_guava//jar"],
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
  ]
)

java_test(
  name = "run_tests",
  test_class = "jetbrains.jetpad.geometry.AllTests",
  size = "small",
  runtime_deps = [
    ":tests"
  ]
)