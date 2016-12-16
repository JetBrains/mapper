def jetpad_module(deps = [], suite = None):
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


  if suite != None:
    native.java_library(
      name = "tests",
      srcs = native.glob(["src/test/java/**/*.java"]),
      deps = [
        ":jar",

        "//util/test:jar",
        "//lib:junit",
        "//lib:mockito"
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