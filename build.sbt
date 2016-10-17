import AssemblyKeys._

assemblySettings

name := "movieSimilarity"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  Seq(
    "org.apache.spark" % "spark-core_2.11" % "1.6.1" % "provided",
    "org.apache.spark" % "spark-sql_2.11" % "1.6.1" % "provided",
    "org.apache.spark" % "spark-mllib_2.11" % "1.6.1" % "provided"
  )
}

mergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class")
  => MergeStrategy.first
  case x => (mergeStrategy in assembly).value(x)
}