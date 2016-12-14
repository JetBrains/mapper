java_library(
  name = "jar",
  srcs = glob(["src/main/java/**/*.java"]),
  deps = [
    "//util/base:jar"
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
  ]
)

java_test(
  name = "run_tests",
  test_class = "jetbrains.jetpad.values.AllTests",
  size = "small",
  runtime_deps = [
    ":tests"
  ]
)