@REM Maven Wrapper for Windows
@ECHO OFF
SET MAVEN_VERSION=3.9.6
SET MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
SET MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd

IF NOT EXIST "%MVN_CMD%" (
  ECHO Downloading Maven %MAVEN_VERSION%...
  powershell -Command "Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%TEMP%\maven.zip'"
  powershell -Command "Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists\'"
)

CALL "%MVN_CMD%" %*
