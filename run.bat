@echo off
echo ================================================
echo  BuildRight Equipment Rental System
echo ================================================
echo.

REM Check for Maven in PATH first
where mvn >nul 2>&1
if %ERRORLEVEL% == 0 (
    echo Found Maven in PATH. Starting...
    mvn spring-boot:run
    goto :end
)

REM Try MAVEN_HOME
if defined MAVEN_HOME (
    echo Found MAVEN_HOME. Starting...
    "%MAVEN_HOME%\bin\mvn" spring-boot:run
    goto :end
)

REM Try common install locations
if exist "C:\Program Files\Maven\bin\mvn.cmd" (
    echo Found Maven at C:\Program Files\Maven. Starting...
    "C:\Program Files\Maven\bin\mvn.cmd" spring-boot:run
    goto :end
)

if exist "C:\tools\maven\bin\mvn.cmd" (
    "C:\tools\maven\bin\mvn.cmd" spring-boot:run
    goto :end
)

REM Download Maven using PowerShell
echo Maven not found. Downloading Maven 3.9.6...
powershell -Command "& { $url='https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip'; $dest='%TEMP%\maven.zip'; Invoke-WebRequest -Uri $url -OutFile $dest; Expand-Archive -Path $dest -DestinationPath '%USERPROFILE%\.buildright-maven' -Force }"

if exist "%USERPROFILE%\.buildright-maven\apache-maven-3.9.6\bin\mvn.cmd" (
    echo Maven downloaded. Starting...
    "%USERPROFILE%\.buildright-maven\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
    goto :end
)

echo.
echo ERROR: Could not find or download Maven.
echo Please install Maven from: https://maven.apache.org/download.cgi
echo OR install the "Maven for Java" extension in VS Code and run from there.
echo.
pause

:end
