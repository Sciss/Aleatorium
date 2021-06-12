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
      "net.harawata"  %  "appdirs"              % deps.main.appDirs,    // finding standard directories
      "org.rogach"    %% "scallop"              % deps.main.scallop,    // command line option parsing
    ),
    buildInfoPackage := "de.sciss.aleatorium",
  )

lazy val deps = new {
  lazy val main = new {
    val appDirs   = "1.2.1"
    val fileUtil  = "1.1.5"
    val numbers   = "0.2.1"
    val pi4j      = "1.4"
    val scallop   = "4.0.2"
  }
}

def appMainClass = Some("de.sciss.aleatorium.PiRun")

lazy val assemblySettings = Seq(
  // ---- assembly ----
  test            in assembly := {},
  mainClass       in assembly := appMainClass,
  target          in assembly := baseDirectory.value,
  assemblyJarName in assembly := s"$baseNameL.jar",
  assemblyMergeStrategy in assembly := {
    case "logback.xml" => MergeStrategy.last
    case PathList("org", "xmlpull", _ @ _*)              => MergeStrategy.first
    case PathList("org", "w3c", "dom", "events", _ @ _*) => MergeStrategy.first // bloody Apache Batik
    case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.first // bloody Jackson
    case x =>
      val old = (assemblyMergeStrategy in assembly).value
      old(x)
  },
  fullClasspath in assembly := (fullClasspath in Test).value // https://github.com/sbt/sbt-assembly/issues/27
)
