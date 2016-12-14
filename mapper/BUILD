java_library(
  name = "jar",
  srcs = glob(["src/main/java/**/*.java"]),
  deps = [
    "//util/base:jar",
    "//model:jar",

    "@com_google_guava//jar"
  ],
  visibility = ["//visibility:public"]
)

java_library(
  name = "tests",
  srcs = glob(["src/test/java/**/*.java"]),
  deps = [
    ":jar",
    "//model:jar",
    "//util/base:jar",
    "//util/test:jar",

    "@com_google_guava//jar",
    "@junit//jar",
    "@org_mockito//jar"
  ]
)

java_test(
  name = "run_tests",
  test_class = "jetbrains.jetpad.mapper.AllTests",
  size = "small",
  runtime_deps = [
    ":tests"
  ]
)