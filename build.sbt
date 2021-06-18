lazy val baseName       = "Aleatorium"
lazy val baseNameL      = baseName.toLowerCase
lazy val projectVersion = "0.1.0-SNAPSHOT"

lazy val buildInfoSettings = Seq(
  // ---- build info ----
  buildInfoKeys := Seq(name, organization, version, scalaVersion, description,
    BuildInfoKey.map(homepage) { case (k, opt)           => k -> opt.get },
    BuildInfoKey.map(licenses) { case (_, Seq((lic, _))) => "license" -> lic }
  ),
  buildInfoOptions += BuildInfoOption.BuildTime
)

lazy val root = project.in(file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(buildInfoSettings)
  .settings(assemblySettings)
  .settings(
    name         := baseName,
    description  := "An installation piece",
    version      := projectVersion,
    homepage     := Some(url(s"https://git.iem.at/sciss/$baseName")),
    licenses     := Seq("AGPL v3+" -> url("http://www.gnu.org/licenses/agpl-3.0.txt")),
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "com.pi4j"      %  "pi4j-core"            % deps.main.pi4j,       // GPIO control
      "de.sciss"      %% "fileutil"             % deps.main.fileUtil,   // utility functions
      "de.sciss"      %% "numbers"              % deps.main.numbers,    // numeric utilities
      "de.sciss"      %% "swingplus"            % deps.main.swingPlus,  // user interface
      "net.harawata"  %  "appdirs"              % deps.main.appDirs,    // finding standard directories
      "org.rogach"    %% "scallop"              % deps.main.scallop,    // command line option parsing
      "org.apache.logging.log4j" % "log4j-api"  % deps.main.log4j,      // needed by rpi-ws28x-java
      "org.apache.logging.log4j" % "log4j-core" % deps.main.log4j,      // needed by rpi-ws28x-java

    ),
    buildInfoPackage := "de.sciss.aleatorium",
  )

lazy val deps = new {
  lazy val main = new {
    val appDirs   = "1.2.1"
    val fileUtil  = "1.1.5"
    val log4j     = "2.10.0"
    val numbers   = "0.2.1"
    val pi4j      = "1.4"
    val scallop   = "4.0.2"
    val swingPlus = "0.5.0"
  }
}

def appMainClass = Some("de.sciss.aleatorium.PiRun")

lazy val assemblySettings = Seq(
  // ---- assembly ----
  assembly / test            := {},
  assembly / mainClass       := appMainClass,
  assembly / target          := baseDirectory.value,
  assembly / assemblyJarName := s"$baseNameL.jar",
  assembly / assemblyMergeStrategy := {
    case "logback.xml" => MergeStrategy.last
    case PathList("org", "xmlpull", _ @ _*)              => MergeStrategy.first
    case PathList("org", "w3c", "dom", "events", _ @ _*) => MergeStrategy.first // bloody Apache Batik
    case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.first // bloody Jackson
    case x =>
      val old = (assembly / assemblyMergeStrategy).value
      old(x)
  },
  assembly / fullClasspath := (Test / fullClasspath).value // https://github.com/sbt/sbt-assembly/issues/27
)
