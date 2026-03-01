#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт сборки мода NoCurtItem в JAR.
При отсутствии Gradle Wrapper — скачивает и создаёт его, затем запускает сборку.
"""

import os
import sys
import subprocess
import shutil
import glob
import zipfile
import urllib.request

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
GRADLE_VERSION = "7.4.2"
WRAPPER_JAR_URL = "https://github.com/gradle/gradle/raw/v{}/gradle/wrapper/gradle-wrapper.jar".format(GRADLE_VERSION)
DISTRIBUTION_ZIP_URL = "https://services.gradle.org/distributions/gradle-{}-bin.zip".format(GRADLE_VERSION)

GRADLEW_BAT = r'''@rem Gradle startup script for Windows
@if "%DEBUG%" == "" @echo off
if "%OS%"=="Windows_NT" setlocal
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi
set DEFAULT_JVM_OPTS=-Dfile.encoding=UTF-8 "-Xmx64m" "-Xms64m"
if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto fail
:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
if exist "%JAVA_EXE%" goto execute
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto fail
:execute
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
if "%ERRORLEVEL%"=="0" goto mainEnd
:fail
exit /b 1
:mainEnd
if "%OS%"=="Windows_NT" endlocal
:omega
'''

GRADLEW_UNIX = r'''#!/bin/sh
APP_HOME="$( cd "$( dirname "$0" )" && pwd )"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
DEFAULT_JVM_OPTS='-Dfile.encoding=UTF-8 "-Xmx64m" "-Xms64m"'
if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi
exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS "-Dorg.gradle.appname=$(basename "$0")" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
'''


def download_wrapper_jar():
    """Скачать gradle-wrapper.jar (сначала с GitHub, при ошибке — из архива Gradle)."""
    wrapper_dir = os.path.join(SCRIPT_DIR, "gradle", "wrapper")
    os.makedirs(wrapper_dir, exist_ok=True)
    jar_path = os.path.join(wrapper_dir, "gradle-wrapper.jar")

    if os.path.isfile(jar_path):
        return jar_path

    print("Скачивание Gradle Wrapper...")
    try:
        req = urllib.request.Request(WRAPPER_JAR_URL, headers={"User-Agent": "Python"})
        with urllib.request.urlopen(req, timeout=30) as resp:
            data = resp.read()
        if len(data) < 10000:  # подозрительно маленький (возможно HTML ошибка)
            raise IOError("Downloaded file too small")
        with open(jar_path, "wb") as f:
            f.write(data)
        print("Gradle Wrapper установлен (с GitHub).")
        return jar_path
    except Exception as e:
        print("Не удалось скачать с GitHub:", e)
        print("Скачивание полного архива Gradle (это может занять минуту)...")

    zip_path = os.path.join(SCRIPT_DIR, "gradle-{}-bin.zip".format(GRADLE_VERSION))
    try:
        urllib.request.urlretrieve(DISTRIBUTION_ZIP_URL, zip_path)
        with zipfile.ZipFile(zip_path, "r") as zf:
            name = "gradle-{}/gradle/wrapper/gradle-wrapper.jar".format(GRADLE_VERSION)
            if name in zf.namelist():
                with zf.open(name) as src:
                    with open(jar_path, "wb") as dst:
                        dst.write(src.read())
                print("Gradle Wrapper установлен (из архива).")
            else:
                raise IOError("gradle-wrapper.jar not found in zip")
    finally:
        if os.path.isfile(zip_path):
            try:
                os.remove(zip_path)
            except OSError:
                pass
    return jar_path


def setup_wrapper():
    """Создать gradlew/gradlew.bat и при необходимости скачать gradle-wrapper.jar."""
    if os.path.isfile(os.path.join(SCRIPT_DIR, "gradlew.bat")) and \
       os.path.isfile(os.path.join(SCRIPT_DIR, "gradle", "wrapper", "gradle-wrapper.jar")):
        return

    download_wrapper_jar()

    with open(os.path.join(SCRIPT_DIR, "gradlew.bat"), "w", newline="\r\n", encoding="utf-8") as f:
        f.write(GRADLEW_BAT)

    gradlew_path = os.path.join(SCRIPT_DIR, "gradlew")
    with open(gradlew_path, "w", encoding="utf-8") as f:
        f.write(GRADLEW_UNIX)
    try:
        os.chmod(gradlew_path, 0o755)
    except OSError:
        pass

    print("Gradle Wrapper создан.")


def main():
    os.chdir(SCRIPT_DIR)
    is_windows = sys.platform == "win32"

    gradlew_file = "gradlew.bat" if is_windows else "gradlew"
    if not os.path.isfile(os.path.join(SCRIPT_DIR, gradlew_file)) or \
       not os.path.isfile(os.path.join(SCRIPT_DIR, "gradle", "wrapper", "gradle-wrapper.jar")):
        try:
            setup_wrapper()
        except Exception as e:
            print("Ошибка установки Gradle Wrapper:", e, file=sys.stderr)
            print("Установите Gradle (https://gradle.org/install/) и выполните в папке проекта: gradle wrapper", file=sys.stderr)
            sys.exit(1)

    gradle_cmd = "gradlew.bat" if is_windows else "./gradlew"
    print("Запуск сборки мода...")
    try:
        subprocess.run(
            [gradle_cmd, "build", "--no-daemon"],
            check=True,
            shell=is_windows,
            cwd=SCRIPT_DIR,
        )
    except subprocess.CalledProcessError as e:
        print("Ошибка сборки:", e, file=sys.stderr)
        sys.exit(1)

    libs_dir = os.path.join(SCRIPT_DIR, "build", "libs")
    jars = [j for j in glob.glob(os.path.join(libs_dir, "*.jar")) if "-sources" not in os.path.basename(j)]

    if not jars:
        print("JAR не найден в build/libs", file=sys.stderr)
        sys.exit(1)

    out_dir = os.path.join(SCRIPT_DIR, "build", "output")
    os.makedirs(out_dir, exist_ok=True)
    for jar in jars:
        dest = os.path.join(out_dir, os.path.basename(jar))
        shutil.copy2(jar, dest)
        print("Собран JAR:", os.path.abspath(dest))
    print("Готово. Мод можно скопировать в .minecraft/mods/")


if __name__ == "__main__":
    main()
