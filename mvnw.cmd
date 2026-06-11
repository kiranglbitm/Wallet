@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Wrapper startup script for Windows
@REM ----------------------------------------------------------------------------

@echo off
setlocal

SET MAVEN_WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar
SET MAVEN_WRAPPER_PROPERTIES=.mvn\wrapper\maven-wrapper.properties

FOR /F "tokens=2 delims==" %%G IN ('findstr /i distributionUrl "%MAVEN_WRAPPER_PROPERTIES%"') DO SET DISTRIBUTION_URL=%%G

FOR %%F IN ("%DISTRIBUTION_URL%") DO SET MAVEN_ZIP_NAME=%%~nF
SET MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\%MAVEN_ZIP_NAME%

IF NOT EXIST "%MAVEN_HOME%" (
    echo Downloading Maven...
    mkdir "%MAVEN_HOME%"
    powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%MAVEN_HOME%\apache-maven.zip'"
    powershell -Command "Expand-Archive -Path '%MAVEN_HOME%\apache-maven.zip' -DestinationPath '%MAVEN_HOME%'"
    del "%MAVEN_HOME%\apache-maven.zip"
)

FOR /R "%MAVEN_HOME%" %%F IN (mvn.cmd) DO SET MAVEN_BIN=%%F

"%MAVEN_BIN%" %*
endlocal
