def jetpad_module(deps, suite):
  native.filegroup(
    name = "gwt_files",
    srcs = native.glob([
      "src/main/java/**/*.gwt.xml",
      "src/main/java/**/*.java"
    ])
  )

  native.java_library(
    name = "jar",
    srcs = native.glob(["src/main/java/**/*.java"]),
    resources = [":gwt_files"],
    deps = deps,
    visibility = ["//visibility:public"]
  )

  native.java_library(
    name = "tests",
    srcs = native.glob(["src/test/java/**/*.java"]),
    deps = [
      ":jar",

      "@junit//jar",
      "@org_mockito//jar",
      "//util/test:jar",
    ] + deps,
  )

  native.java_test(
    name = "run_tests",
    test_class = suite,
    size = "small",
    runtime_deps = [
      ":tests"
    ]
  )