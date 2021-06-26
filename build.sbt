lazy val baseName       = "Aleatorium"
lazy val baseNameL      = baseName.toLowerCase
lazy val projectVersion = "0.3.0-SNAPSHOT"

lazy val buildInfoSettings = Seq(
  // ---- build info ----
  buildInfoKeys := Seq(name, organization, version, scalaVersion, description,
    BuildInfoKey.map(homepage) { case (k, opt)           => k -> opt.get },
    BuildInfoKey.map(licenses) { case (_, Seq((lic, _))) => "license" -> lic }
  ),
  buildInfoOptions += BuildInfoOption.BuildTime
)

lazy val commonSettings = Seq(
  version      := projectVersion,
  homepage     := Some(url(s"https://git.iem.at/sciss/$baseName")),
  scalaVersion := "2.13.6",
  licenses     := Seq("AGPL v3+" -> url("http://www.gnu.org/licenses/agpl-3.0.txt")),
)

lazy val root = project.in(file("."))
  .aggregate(common, alpha, beta)
  .settings(commonSettings)
  .settings(
    name := baseName,
    description  := "An installation piece",
  )

lazy val common = project.in(file("common"))
  .enablePlugins(BuildInfoPlugin)
  .settings(commonSettings)
  .settings(buildInfoSettings)
  .settings(
    name := s"$baseName-common",
    description := "Common code",
    libraryDependencies ++= Seq(
      "com.pi4j"      %  "pi4j-core"            % deps.common.pi4j,       // GPIO control
      "de.sciss"      %% "fileutil"             % deps.common.fileUtil,   // utility functions
      "de.sciss"      %% "model"                % deps.common.model,      // events
      "de.sciss"      %% "numbers"              % deps.common.numbers,    // numeric utilities
      "de.sciss"      %% "swingplus"            % deps.common.swingPlus,  // user interface
      "net.harawata"  %  "appdirs"              % deps.common.appDirs,    // finding standard directories
      "org.rogach"    %% "scallop"              % deps.common.scallop,    // command line option parsing
    ),
    buildInfoPackage := "de.sciss.aleatorium",
  )

lazy val alpha = project.in(file("alpha"))
  .dependsOn(common)
  .settings(commonSettings)
  .settings(assemblySettings)
  .settings(
    name := s"$baseName-alpha",
    description := "Top robot",
    assembly / mainClass       := Some("de.sciss.aleatorium.Alpha"),
    assembly / assemblyJarName := s"$baseNameL-alpha.jar",
  )

lazy val beta = project.in(file("beta"))
  .dependsOn(common)
  .settings(commonSettings)
  .settings(assemblySettings)
  .settings(
    name := s"$baseName-beta",
    description := "Bottom robot",
    assembly / mainClass       := Some("de.sciss.aleatorium.Beta"),
    assembly / assemblyJarName := s"$baseNameL-beta.jar",
    libraryDependencies ++= Seq(
      "de.sciss" %% "scalacollider"   % deps.beta.scalaCollider,  // sound
      "de.sciss" %% "scalaosc"        % deps.beta.scalaOSC,       // to LED process
      "org.apache.logging.log4j" % "log4j-api"  % deps.beta.log4j,      // needed by rpi-ws28x-java
      "org.apache.logging.log4j" % "log4j-core" % deps.beta.log4j,      // needed by rpi-ws28x-java
    ),
  )

lazy val deps = new {
  val common = new {
    val appDirs   = "1.2.1"
    val fileUtil  = "1.1.5"
    val model     = "0.3.5"
    val numbers   = "0.2.1"
    val pi4j      = "1.4"
    val scallop   = "4.0.2"
    val swingPlus = "0.5.0"
  }
  val beta = new {
    val scalaCollider = "2.6.4"
    val scalaOSC  = "1.3.1"
    val log4j     = "2.10.0"
  }
}

lazy val assemblySettings = Seq(
  // ---- assembly ----
  assembly / test            := {},
  assembly / target          := baseDirectory.value,
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
