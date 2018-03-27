import sbt.Keys.sourceDirectory

// coverageMinimum := 50
// coverageFailOnMinimum := true
// coverageExcludedPackages := "<empty>;xyz.*;.*abc.*;aaa\\.bbb\\..*"
// javaOptions in Test ++= Seq("-Xmx12g", "-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n")

EclipseKeys.eclipseOutput := Some("target/scala-2.11/classes")  // share the binary folder with sbt
fork in Test := true  // this is needed for the ClassLoader to work under `sbt test`

lazy val commonSettings = Seq(

version := "0.0.1",
scalaVersion := "2.11.8",
EclipseKeys.withSource := true,
// parallelExecution in test := false,
test in assembly := {},
assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}
) ++ packAutoSettings

lazy val project = Project(
id = "vsf-commons", 
base = file(".")).settings(commonSettings).settings(
name := "vsf-commons",

libraryDependencies ++= Seq(

  "org.apache.spark" % "spark-sql_2.11" % "2.0.2",
  "org.apache.spark" % "spark-core_2.11" % "2.0.2",
  "org.apache.spark" % "spark-mllib_2.11" %"2.0.2",
  // "org.scalanlp" %% "breeze-natives" % "0.12",
  // "org.apache.spark" % "spark-graphx_2.11" %"2.0.2",
  
//  "org.apache.httpcomponents" % "httpclient" % "4.5.1",
//  "org.skife.com.typesafe.config" % "typesafe-config" % "0.3.0",
//  "com.google.code.gson" % "gson" % "1.7.1",
//  "org.apache.hadoop" % "hadoop-azure" % "2.7.3",
//  "org.apache.commons" % "commons-math3" % "3.6.1",

//  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

//  "org.scalikejdbc" %% "scalikejdbc" % "3.0.1",
//  "org.postgresql" % "postgresql" % "42.1.1",
  
  //*********** test only ****************
  "org.mockito" % "mockito-core" % "1.8.5" % "test",
  "junit" % "junit" % "4.10" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test")
//  "com.holdenkarau" %% "spark-testing-base" % "2.2.0_0.7.2" % "test"
)
