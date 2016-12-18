http_archive(
  name = "io_bazel_rules_gwt",
  url = "https://github.com/bazelbuild/rules_gwt/archive/0.1.0.tar.gz",
  sha256 = "7be73daf0a4d90dc5a90561e8e9eec0056e69dd24b099dd253da129a7e8f952f",
  strip_prefix = "rules_gwt-0.1.0",
)
load("@io_bazel_rules_gwt//gwt:gwt.bzl", "gwt_repositories")
gwt_repositories()

maven_jar(
  name = "com_google_guava",
  artifact = "com.google.guava:guava:20.0-rc1"
)

maven_jar(
  name = "com_google_guava_gwt",
  artifact = "com.google.guava:guava-gwt:20.0-rc1"
)

maven_jar(
  name = "com_google_gwt_user",
  artifact = "com.google.gwt:gwt-user:2.8.0"
)

maven_jar(
  name = "com_google_code_findbugs_annotations",
  artifact = "com.google.code.findbugs:jsr305:2.0.1"
)

maven_jar(
  name = "com_googlecode_gwtquery",
  artifact = "com.googlecode.gwtquery:gwtquery:1.5-beta1",
)

maven_jar(
  name = "com_google_gwt_elemental",
  artifact = "com.google.gwt:gwt-elemental:2.8.0"
)

maven_jar(
  name = "junit",
  artifact = "junit:junit:4.11"
)

maven_jar(
  name = "org_mockito",
  artifact = "org.mockito:mockito-all:1.9.5"
)

maven_jar(
  name = "com_google_errorprone",
  artifact = "com.google.errorprone:error_prone_annotations:2.0.2"
)

maven_jar(
  name = "com_google_j2obj_annotations",
  artifact = "com.google.j2objc:j2objc-annotations:1.1"
)

maven_jar(
  name = "com_google_code_gson",
  artifact = "com.google.code.gson:gson:2.8.0"
)