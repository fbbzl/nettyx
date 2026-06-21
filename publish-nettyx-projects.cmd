@if (@CodeSection)==(@Batch) @then
@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "MAVEN_HOME=%MAVEN_HOME%"
if not defined MAVEN_HOME set "MAVEN_HOME=D:\maven\apache-maven-3.9.14"
set "MAVEN_SETTINGS=%MAVEN_SETTINGS%"
if not defined MAVEN_SETTINGS set "MAVEN_SETTINGS=D:\maven\settings.xml"
set "MAVEN_REPO=%MAVEN_REPO%"
if not defined MAVEN_REPO set "MAVEN_REPO=D:\maven\repository"

set "PROJECT_NETTYX=%PROJECT_NETTYX%"
if not defined PROJECT_NETTYX set "PROJECT_NETTYX=D:\workspace\nettyx"
set "PROJECT_STARTER=%PROJECT_STARTER%"
if not defined PROJECT_STARTER set "PROJECT_STARTER=D:\workspace\spring-boot-starter-nettyx"

set "MVN=%MAVEN_HOME%\bin\mvn.cmd"

set "NETTYX_VERSION="
set "STARTER_VERSION="
set "NETTYX_REMOTES=gitee github"
set "STARTER_REMOTES=gitee github"
set "NETTYX_RELEASE_BRANCH=release"
set "NETTYX_MAIN_BRANCH=main"
set "STARTER_PUBLISH_BRANCH=publish"
set "STARTER_MAIN_BRANCH=main"
set "DRY_RUN=0"
set "SKIP_TESTS=0"
set "SKIP_GIT_PUSH=0"
set "SKIP_DEPLOY=0"
set "NO_PAUSE=0"

:parse_args
if "%~1"=="" goto after_parse_args
set "ARG=%~1"
if /i "%ARG%"=="/dryRun" set "DRY_RUN=1" & shift & goto parse_args
if /i "%ARG%"=="-dryRun" set "DRY_RUN=1" & shift & goto parse_args
if /i "%ARG%"=="/skipTests" set "SKIP_TESTS=1" & shift & goto parse_args
if /i "%ARG%"=="-skipTests" set "SKIP_TESTS=1" & shift & goto parse_args
if /i "%ARG%"=="/skipGitPush" set "SKIP_GIT_PUSH=1" & shift & goto parse_args
if /i "%ARG%"=="-skipGitPush" set "SKIP_GIT_PUSH=1" & shift & goto parse_args
if /i "%ARG%"=="/skipDeploy" set "SKIP_DEPLOY=1" & shift & goto parse_args
if /i "%ARG%"=="-skipDeploy" set "SKIP_DEPLOY=1" & shift & goto parse_args
if /i "%ARG%"=="/noPause" set "NO_PAUSE=1" & shift & goto parse_args
if /i "%ARG%"=="-noPause" set "NO_PAUSE=1" & shift & goto parse_args
if /i "%ARG:~0,15%"=="/nettyxVersion:" set "NETTYX_VERSION=%ARG:~15%" & shift & goto parse_args
if /i "%ARG:~0,15%"=="-nettyxVersion:" set "NETTYX_VERSION=%ARG:~15%" & shift & goto parse_args
if /i "%ARG:~0,16%"=="/starterVersion:" set "STARTER_VERSION=%ARG:~16%" & shift & goto parse_args
if /i "%ARG:~0,16%"=="-starterVersion:" set "STARTER_VERSION=%ARG:~16%" & shift & goto parse_args
if /i "%ARG:~0,15%"=="/nettyxRemotes:" set "NETTYX_REMOTES=%ARG:~15%" & shift & goto parse_args
if /i "%ARG:~0,15%"=="-nettyxRemotes:" set "NETTYX_REMOTES=%ARG:~15%" & shift & goto parse_args
if /i "%ARG:~0,16%"=="/starterRemotes:" set "STARTER_REMOTES=%ARG:~16%" & shift & goto parse_args
if /i "%ARG:~0,16%"=="-starterRemotes:" set "STARTER_REMOTES=%ARG:~16%" & shift & goto parse_args
if /i "%ARG:~0,21%"=="/nettyxReleaseBranch:" set "NETTYX_RELEASE_BRANCH=%ARG:~21%" & shift & goto parse_args
if /i "%ARG:~0,21%"=="-nettyxReleaseBranch:" set "NETTYX_RELEASE_BRANCH=%ARG:~21%" & shift & goto parse_args
if /i "%ARG:~0,18%"=="/nettyxMainBranch:" set "NETTYX_MAIN_BRANCH=%ARG:~18%" & shift & goto parse_args
if /i "%ARG:~0,18%"=="-nettyxMainBranch:" set "NETTYX_MAIN_BRANCH=%ARG:~18%" & shift & goto parse_args
if /i "%ARG:~0,22%"=="/starterPublishBranch:" set "STARTER_PUBLISH_BRANCH=%ARG:~22%" & shift & goto parse_args
if /i "%ARG:~0,22%"=="-starterPublishBranch:" set "STARTER_PUBLISH_BRANCH=%ARG:~22%" & shift & goto parse_args
if /i "%ARG:~0,19%"=="/starterMainBranch:" set "STARTER_MAIN_BRANCH=%ARG:~19%" & shift & goto parse_args
if /i "%ARG:~0,19%"=="-starterMainBranch:" set "STARTER_MAIN_BRANCH=%ARG:~19%" & shift & goto parse_args
if not defined NETTYX_VERSION (
    set "NETTYX_VERSION=%~1"
    shift
    goto parse_args
)
if not defined STARTER_VERSION (
    set "STARTER_VERSION=%~1"
    shift
    goto parse_args
)
echo Unknown argument: %~1
goto fail

:after_parse_args
call :resolve_maven

call :assert_path "%MVN%" "mvn.cmd" || goto fail
call :assert_path "%MAVEN_SETTINGS%" "Maven settings.xml" || goto fail
call :assert_path "%MAVEN_REPO%" "Maven local repository" || goto fail
call :assert_path "%PROJECT_NETTYX%" "Project directory" || goto fail
call :assert_path "%PROJECT_STARTER%" "Project directory" || goto fail

call :get_current_branch "%PROJECT_NETTYX%" CURRENT_NETTYX_BRANCH || goto fail
if /i not "%CURRENT_NETTYX_BRANCH%"=="%NETTYX_RELEASE_BRANCH%" (
    echo Current nettyx branch is [%CURRENT_NETTYX_BRANCH%], expected release branch [%NETTYX_RELEASE_BRANCH%]
    goto fail
)
call :first_word "%NETTYX_REMOTES%" NETTYX_PRIMARY_REMOTE || goto fail
call :first_word "%STARTER_REMOTES%" STARTER_PRIMARY_REMOTE || goto fail

call :git_fetch "%PROJECT_NETTYX%" "%NETTYX_PRIMARY_REMOTE%" || goto fail
call :git_fetch "%PROJECT_STARTER%" "%STARTER_PRIMARY_REMOTE%" || goto fail

call :get_project_version "%PROJECT_NETTYX%" "nettyx" CURRENT_NETTYX_VERSION || goto fail
call :get_project_version "%PROJECT_STARTER%" "spring-boot-starter-nettyx" CURRENT_STARTER_VERSION || goto fail
call :get_property_version "%PROJECT_STARTER%" "nettyx.version" CURRENT_STARTER_NETTYX_VERSION || goto fail
call :assert_remotes "%PROJECT_NETTYX%" "%NETTYX_REMOTES%" || goto fail
call :assert_remotes "%PROJECT_STARTER%" "%STARTER_REMOTES%" || goto fail

if not defined NETTYX_VERSION (
    call :next_version_keep_suffix "%CURRENT_NETTYX_VERSION%" NETTYX_VERSION || goto fail
)
if not defined STARTER_VERSION (
    call :next_project_version "%PROJECT_STARTER%" "spring-boot-starter-nettyx" "%NETTYX_VERSION%" STARTER_VERSION || goto fail
)

echo.
echo ========== release plan ==========
echo nettyx:                    %CURRENT_NETTYX_VERSION% -^> %NETTYX_VERSION%
echo spring-boot-starter-nettyx: %CURRENT_STARTER_VERSION% -^> %STARTER_VERSION%
echo starter nettyx.version:     %CURRENT_STARTER_NETTYX_VERSION% -^> %NETTYX_VERSION%
echo nettyx remotes:             %NETTYX_REMOTES%
echo starter remotes:            %STARTER_REMOTES%
echo nettyx branches:            %NETTYX_RELEASE_BRANCH% -^> %NETTYX_MAIN_BRANCH%
echo starter branches:           %STARTER_PUBLISH_BRANCH% -^> %STARTER_MAIN_BRANCH%
if "%DRY_RUN%"=="1" echo DryRun is enabled. No files, git refs, or deployments will be changed.

set "OLD_MAVEN_OPTS=%MAVEN_OPTS%"
set "MAVEN_OPTS=-Xms512m -Xmx2048m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Duser.language=zh -Duser.country=CN --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
echo MAVEN_OPTS=%MAVEN_OPTS%

call :publish_nettyx || goto fail
call :publish_starter || goto fail
call :return_work_branches || goto fail

echo.
echo ========== done ==========
echo nettyx version: %NETTYX_VERSION%
echo spring-boot-starter-nettyx version: %STARTER_VERSION%
goto done

:resolve_maven
if defined MAVEN_HOME (
    if exist "%MAVEN_HOME%\bin\mvn.cmd" (
        set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
        exit /b 0
    )
)
for %%X in (mvn.cmd) do (
    set "MVN=%%~$PATH:X"
    if defined MVN exit /b 0
)
echo Cannot find mvn.cmd in MAVEN_HOME or PATH
exit /b 1

:assert_path
if not exist "%~1" (
    echo %~2 does not exist: %~1
    exit /b 1
)
exit /b 0

:git_fetch
set "FETCH_PROJECT_PATH=%~1"
set "FETCH_REMOTE=%~2"
if "%DRY_RUN%"=="1" exit /b 0
call :run_in_dir "%FETCH_PROJECT_PATH%" git -C "%FETCH_PROJECT_PATH%" fetch "%FETCH_REMOTE%"
exit /b %ERRORLEVEL%

:assert_remotes
set "REMOTE_PROJECT_PATH=%~1"
set "REMOTE_LIST=%~2"
if not defined REMOTE_LIST (
    echo Remote list is empty: %REMOTE_PROJECT_PATH%
    exit /b 1
)
for %%R in (%REMOTE_LIST%) do (
    git -C "%REMOTE_PROJECT_PATH%" remote get-url %%R >nul 2>nul
    if not "!ERRORLEVEL!"=="0" (
        echo Remote does not exist: %%R, project: %REMOTE_PROJECT_PATH%
        exit /b 1
    )
)
exit /b 0

:first_word
set "FIRST_WORD_LIST=%~1"
for %%R in (%FIRST_WORD_LIST%) do (
    set "%~2=%%R"
    exit /b 0
)
exit /b 1

:get_current_branch
for /f "usebackq delims=" %%B in (`git -C "%~1" branch --show-current`) do set "%~2=%%B"
if not defined %~2 (
    echo Cannot resolve current branch: %~1
    exit /b 1
)
exit /b 0

:repo_has_changes
set "CHECK_PROJECT_PATH=%~1"
set "CHECK_STATUS_FILE=%TEMP%\nettyx_publish_precheck_%RANDOM%%RANDOM%.txt"
git -C "%CHECK_PROJECT_PATH%" status --porcelain > "%CHECK_STATUS_FILE%"
if not "%ERRORLEVEL%"=="0" (
    del "%CHECK_STATUS_FILE%" > nul 2> nul
    echo Failed to check git status: %CHECK_PROJECT_PATH%
    exit /b 1
)
for %%S in ("%CHECK_STATUS_FILE%") do set "CHECK_STATUS_SIZE=%%~zS"
del "%CHECK_STATUS_FILE%" > nul 2> nul
if "%CHECK_STATUS_SIZE%"=="0" (
    set "%~2=0"
) else (
    set "%~2=1"
)
exit /b 0

:get_project_version
for /f "usebackq delims=" %%V in (`cscript //nologo //E:JScript "%~f0" getProjectVersion "%~1" "%~2"`) do set "%~3=%%V"
if not defined %~3 exit /b 1
exit /b 0

:get_property_version
for /f "usebackq delims=" %%V in (`cscript //nologo //E:JScript "%~f0" getPropertyVersion "%~1" "%~2"`) do set "%~3=%%V"
if not defined %~3 exit /b 1
exit /b 0

:next_version_keep_suffix
for /f "usebackq delims=" %%V in (`cscript //nologo //E:JScript "%~f0" nextVersion "%~1"`) do set "%~2=%%V"
if not defined %~2 exit /b 1
exit /b 0

:next_project_version
for /f "usebackq delims=" %%V in (`cscript //nologo //E:JScript "%~f0" nextProjectVersion "%~1" "%~2" "%~3"`) do set "%~4=%%V"
if not defined %~4 exit /b 1
exit /b 0

:publish_nettyx
echo.
echo ========== nettyx release commit ==========
call :switch_branch "%PROJECT_NETTYX%" "%NETTYX_RELEASE_BRANCH%" "%NETTYX_PRIMARY_REMOTE%" || exit /b 1
call :commit_current_changes "nettyx release" "%PROJECT_NETTYX%" "chore release: nettyx %NETTYX_VERSION%" || exit /b 1
call :set_project_version "%PROJECT_NETTYX%" "nettyx" "%NETTYX_VERSION%" || exit /b 1
call :update_readme_version "%PROJECT_NETTYX%" "%NETTYX_VERSION%" || exit /b 1
call :stage_release_changes "%PROJECT_NETTYX%" || exit /b 1
call :commit_staged_changes "nettyx release" "%PROJECT_NETTYX%" "chore release: nettyx %NETTYX_VERSION%" || exit /b 1
call :push_branch "%PROJECT_NETTYX%" "%NETTYX_RELEASE_BRANCH%" "%NETTYX_REMOTES%" || exit /b 1

echo.
echo ========== nettyx release clean install ==========
call :run_maven "%PROJECT_NETTYX%" clean install || exit /b 1

if "%SKIP_DEPLOY%"=="1" (
    echo Skipped deploy: nettyx
) else (
    echo.
    echo ========== nettyx release clean deploy ==========
    call :run_maven "%PROJECT_NETTYX%" clean deploy || exit /b 1
)

echo.
echo ========== nettyx main sync ==========
call :switch_branch "%PROJECT_NETTYX%" "%NETTYX_MAIN_BRANCH%" "%NETTYX_PRIMARY_REMOTE%" || exit /b 1
call :run_in_dir "%PROJECT_NETTYX%" git -C "%PROJECT_NETTYX%" merge --no-ff "%NETTYX_RELEASE_BRANCH%" -m "chore release: nettyx %NETTYX_VERSION%" || exit /b 1
call :push_branch "%PROJECT_NETTYX%" "%NETTYX_MAIN_BRANCH%" "%NETTYX_REMOTES%" || exit /b 1
exit /b 0

:publish_starter
echo.
echo ========== spring-boot-starter-nettyx publish prepare ==========
call :switch_branch "%PROJECT_STARTER%" "%STARTER_PUBLISH_BRANCH%" "%STARTER_PRIMARY_REMOTE%" || exit /b 1
call :set_project_version "%PROJECT_STARTER%" "spring-boot-starter-nettyx" "%STARTER_VERSION%" || exit /b 1
call :set_property_version "%PROJECT_STARTER%" "nettyx.version" "%NETTYX_VERSION%" || exit /b 1
call :stage_release_changes "%PROJECT_STARTER%" || exit /b 1
call :commit_staged_changes "spring-boot-starter-nettyx publish" "%PROJECT_STARTER%" "chore release: spring-boot-starter-nettyx %STARTER_VERSION%" || exit /b 1
call :push_branch "%PROJECT_STARTER%" "%STARTER_PUBLISH_BRANCH%" "%STARTER_REMOTES%" || exit /b 1

echo.
echo ========== spring-boot-starter-nettyx publish clean install ==========
call :run_maven "%PROJECT_STARTER%" clean install || exit /b 1

if "%SKIP_DEPLOY%"=="1" (
    echo Skipped deploy: spring-boot-starter-nettyx
) else (
    echo.
    echo ========== spring-boot-starter-nettyx publish clean deploy ==========
    call :run_maven "%PROJECT_STARTER%" clean deploy || exit /b 1
)

echo.
echo ========== spring-boot-starter-nettyx main sync ==========
call :switch_branch "%PROJECT_STARTER%" "%STARTER_MAIN_BRANCH%" "%STARTER_PRIMARY_REMOTE%" || exit /b 1
call :merge_no_ff_accept_theirs "%PROJECT_STARTER%" "%STARTER_PUBLISH_BRANCH%" "chore release: spring-boot-starter-nettyx %STARTER_VERSION%" || exit /b 1
call :push_branch "%PROJECT_STARTER%" "%STARTER_MAIN_BRANCH%" "%STARTER_REMOTES%" || exit /b 1
exit /b 0

:return_work_branches
echo.
echo ========== return work branches ==========
call :switch_branch "%PROJECT_NETTYX%" "%NETTYX_RELEASE_BRANCH%" "%NETTYX_PRIMARY_REMOTE%" || exit /b 1
call :switch_branch "%PROJECT_STARTER%" "%STARTER_PUBLISH_BRANCH%" "%STARTER_PRIMARY_REMOTE%" || exit /b 1
exit /b 0

:set_project_version
if "%DRY_RUN%"=="1" (
    cscript //nologo //E:JScript "%~f0" setProjectVersion "%~1" "%~2" "%~3" dryRun
) else (
    cscript //nologo //E:JScript "%~f0" setProjectVersion "%~1" "%~2" "%~3"
)
exit /b %ERRORLEVEL%

:update_readme_version
set "README_PROJECT_PATH=%~1"
set "README_VERSION=%~2"
if "%DRY_RUN%"=="1" (
    echo DryRun: update README version to %README_VERSION%
    exit /b 0
)
for %%F in (README.md README_zh.md) do (
    if exist "%README_PROJECT_PATH%\%%F" (
        powershell -NoProfile -Command "(Get-Content -LiteralPath '%README_PROJECT_PATH%\%%F' -Raw) -replace '<version>[^^<]*</version>', '<version>%README_VERSION%</version>' | Set-Content -LiteralPath '%README_PROJECT_PATH%\%%F' -NoNewline -Encoding UTF8"
    )
)
exit /b 0

:set_property_version
if "%DRY_RUN%"=="1" (
    cscript //nologo //E:JScript "%~f0" setPropertyVersion "%~1" "%~2" "%~3" dryRun
) else (
    cscript //nologo //E:JScript "%~f0" setPropertyVersion "%~1" "%~2" "%~3"
)
exit /b %ERRORLEVEL%

:switch_branch
set "SWITCH_PROJECT_PATH=%~1"
set "SWITCH_BRANCH=%~2"
set "SWITCH_REMOTE=%~3"
if "%DRY_RUN%"=="1" (
    call :run_in_dir "%SWITCH_PROJECT_PATH%" git -C "%SWITCH_PROJECT_PATH%" switch "%SWITCH_BRANCH%"
    exit /b %ERRORLEVEL%
)
git -C "%SWITCH_PROJECT_PATH%" show-ref --verify --quiet "refs/heads/%SWITCH_BRANCH%"
if "%ERRORLEVEL%"=="0" (
    call :run_in_dir "%SWITCH_PROJECT_PATH%" git -C "%SWITCH_PROJECT_PATH%" switch "%SWITCH_BRANCH%"
    exit /b %ERRORLEVEL%
)
git -C "%SWITCH_PROJECT_PATH%" show-ref --verify --quiet "refs/remotes/%SWITCH_REMOTE%/%SWITCH_BRANCH%"
if "%ERRORLEVEL%"=="0" (
    call :run_in_dir "%SWITCH_PROJECT_PATH%" git -C "%SWITCH_PROJECT_PATH%" switch -c "%SWITCH_BRANCH%" --track "%SWITCH_REMOTE%/%SWITCH_BRANCH%"
    exit /b %ERRORLEVEL%
)
echo Cannot switch branch because it does not exist locally or on %SWITCH_REMOTE%: %SWITCH_BRANCH%
exit /b 1

:push_branch
set "PUSH_PROJECT_PATH=%~1"
set "PUSH_BRANCH=%~2"
set "PUSH_REMOTES=%~3"
if "%SKIP_GIT_PUSH%"=="1" (
    echo Skipped git push: %PUSH_BRANCH%
    exit /b 0
)
for %%R in (%PUSH_REMOTES%) do (
    call :run_in_dir "%PUSH_PROJECT_PATH%" git -C "%PUSH_PROJECT_PATH%" push %%R HEAD:%PUSH_BRANCH% || exit /b 1
)
exit /b 0

:merge_no_ff_accept_theirs
set "MERGE_PROJECT_PATH=%~1"
set "MERGE_SOURCE_BRANCH=%~2"
set "MERGE_MESSAGE=%~3"
call :run_in_dir_no_fail "%MERGE_PROJECT_PATH%" git -C "%MERGE_PROJECT_PATH%" merge --no-ff "%MERGE_SOURCE_BRANCH%" -m "%MERGE_MESSAGE%"
set "MERGE_EXIT=%ERRORLEVEL%"
if "%DRY_RUN%"=="1" exit /b 0
if "%MERGE_EXIT%"=="0" exit /b 0
echo Merge reported conflicts. Accepting source branch changes for unresolved paths: %MERGE_SOURCE_BRANCH%
call :checkout_all_theirs_unmerged "%MERGE_PROJECT_PATH%" || exit /b 1
call :assert_no_unmerged_paths "%MERGE_PROJECT_PATH%" || exit /b 1
call :commit_staged_changes "merge %MERGE_SOURCE_BRANCH%" "%MERGE_PROJECT_PATH%" "%MERGE_MESSAGE%" || exit /b 1
exit /b 0

:checkout_all_theirs_unmerged
set "ALL_THEIRS_PROJECT_PATH=%~1"
set "ALL_THEIRS_FILE=%TEMP%\fz_unmerged_%RANDOM%%RANDOM%.txt"
git -C "%ALL_THEIRS_PROJECT_PATH%" diff --name-only --diff-filter=U > "%ALL_THEIRS_FILE%"
for /f "usebackq delims=" %%P in ("%ALL_THEIRS_FILE%") do (
    call :run_in_dir "%ALL_THEIRS_PROJECT_PATH%" git -C "%ALL_THEIRS_PROJECT_PATH%" checkout --theirs -- "%%P" || exit /b 1
    call :run_in_dir "%ALL_THEIRS_PROJECT_PATH%" git -C "%ALL_THEIRS_PROJECT_PATH%" add -- "%%P" || exit /b 1
)
del "%ALL_THEIRS_FILE%" >nul 2>nul
exit /b 0

:restore_head_paths
set "RESTORE_PROJECT_PATH=%~1"
shift
:restore_head_paths_loop
if "%~1"=="" exit /b 0
if "%DRY_RUN%"=="1" (
    call :run_in_dir "%RESTORE_PROJECT_PATH%" git -C "%RESTORE_PROJECT_PATH%" restore --source=HEAD --staged --worktree -- "%~1" || exit /b 1
) else (
    git -C "%RESTORE_PROJECT_PATH%" cat-file -e "HEAD:%~1" >nul 2>nul
    if "!ERRORLEVEL!"=="0" (
        call :run_in_dir "%RESTORE_PROJECT_PATH%" git -C "%RESTORE_PROJECT_PATH%" restore --source=HEAD --staged --worktree -- "%~1" || exit /b 1
    )
)
shift
goto restore_head_paths_loop

:drop_or_restore_head_paths
set "DROP_PROJECT_PATH=%~1"
shift
:drop_or_restore_head_paths_loop
if "%~1"=="" exit /b 0
if "%DRY_RUN%"=="1" (
    call :run_in_dir "%DROP_PROJECT_PATH%" git -C "%DROP_PROJECT_PATH%" restore --source=HEAD --staged --worktree -- "%~1" || exit /b 1
    call :run_in_dir "%DROP_PROJECT_PATH%" git -C "%DROP_PROJECT_PATH%" rm -r --cached --ignore-unmatch -- "%~1" || exit /b 1
    call :run_in_dir "%DROP_PROJECT_PATH%" git -C "%DROP_PROJECT_PATH%" clean -fd -- "%~1" || exit /b 1
) else (
    git -C "%DROP_PROJECT_PATH%" cat-file -e "HEAD:%~1" >nul 2>nul
    if "!ERRORLEVEL!"=="0" (
        call :run_in_dir "%DROP_PROJECT_PATH%" git -C "%DROP_PROJECT_PATH%" restore --source=HEAD --staged --worktree -- "%~1" || exit /b 1
    ) else (
        call :run_in_dir "%DROP_PROJECT_PATH%" git -C "%DROP_PROJECT_PATH%" rm -r --cached --ignore-unmatch -- "%~1" || exit /b 1
        call :run_in_dir "%DROP_PROJECT_PATH%" git -C "%DROP_PROJECT_PATH%" clean -fd -- "%~1" || exit /b 1
    )
)
shift
goto drop_or_restore_head_paths_loop

:commit_current_changes
set "CURRENT_COMMIT_LABEL=%~1"
set "CURRENT_COMMIT_PROJECT_PATH=%~2"
set "CURRENT_COMMIT_MESSAGE=%~3"
call :repo_has_changes "%CURRENT_COMMIT_PROJECT_PATH%" CURRENT_COMMIT_HAS_CHANGES || exit /b 1
if "%CURRENT_COMMIT_HAS_CHANGES%"=="0" (
    echo No working tree changes to commit: %CURRENT_COMMIT_LABEL%
    exit /b 0
)
call :stage_release_changes "%CURRENT_COMMIT_PROJECT_PATH%" || exit /b 1
call :commit_staged_changes "%CURRENT_COMMIT_LABEL%" "%CURRENT_COMMIT_PROJECT_PATH%" "%CURRENT_COMMIT_MESSAGE%"
exit /b %ERRORLEVEL%

:commit_staged_changes
set "STAGED_COMMIT_LABEL=%~1"
set "STAGED_COMMIT_PROJECT_PATH=%~2"
set "STAGED_COMMIT_MESSAGE=%~3"
if "%DRY_RUN%"=="1" goto commit_staged_dry_run
git -C "%STAGED_COMMIT_PROJECT_PATH%" diff --cached --quiet
if "%ERRORLEVEL%"=="0" (
    echo No staged changes to commit: %STAGED_COMMIT_LABEL%
    exit /b 0
)
call :run_in_dir "%STAGED_COMMIT_PROJECT_PATH%" git -C "%STAGED_COMMIT_PROJECT_PATH%" commit -m "%STAGED_COMMIT_MESSAGE%"
exit /b %ERRORLEVEL%

:commit_staged_dry_run
call :run_in_dir "%STAGED_COMMIT_PROJECT_PATH%" git -C "%STAGED_COMMIT_PROJECT_PATH%" commit -m "%STAGED_COMMIT_MESSAGE%"
exit /b %ERRORLEVEL%

:run_maven
set "WORK_DIR=%~1"
set "TEST_ARG="
if "%SKIP_TESTS%"=="1" set "TEST_ARG=-DskipTests"
call :run_in_dir "%WORK_DIR%" "%MVN%" -B -U -ntp -s "%MAVEN_SETTINGS%" "-Dmaven.repo.local=%MAVEN_REPO%" %TEST_ARG% "%~2" "%~3"
exit /b %ERRORLEVEL%

:stage_release_changes
set "STAGE_PROJECT_PATH=%~1"
call :run_in_dir "%STAGE_PROJECT_PATH%" git -C "%STAGE_PROJECT_PATH%" add -u || exit /b 1
for %%P in (.gitignore pom.xml README.md README_zh.md src tools) do (
    if exist "%STAGE_PROJECT_PATH%\%%P" (
        call :run_in_dir "%STAGE_PROJECT_PATH%" git -C "%STAGE_PROJECT_PATH%" add -A -- "%%P" || exit /b 1
    )
)
exit /b 0

:run_in_dir
set "WORK_DIR=%~1"
set "RUN_COMMAND="
:run_in_dir_args
shift
if "%~1"=="" goto run_in_dir_ready
if defined RUN_COMMAND (
    set "RUN_COMMAND=!RUN_COMMAND! "%~1""
) else (
    set "RUN_COMMAND="%~1""
)
goto run_in_dir_args
:run_in_dir_ready
echo [%WORK_DIR%] !RUN_COMMAND!
if "%DRY_RUN%"=="1" exit /b 0
pushd "%WORK_DIR%" || exit /b 1
call !RUN_COMMAND!
set "RUN_EXIT=%ERRORLEVEL%"
popd
if not "%RUN_EXIT%"=="0" (
    echo Command failed with exit code: %RUN_EXIT%
    exit /b %RUN_EXIT%
)
exit /b 0

:run_in_dir_no_fail
set "WORK_DIR=%~1"
set "RUN_COMMAND="
:run_in_dir_no_fail_args
shift
if "%~1"=="" goto run_in_dir_no_fail_ready
if defined RUN_COMMAND (
    set "RUN_COMMAND=!RUN_COMMAND! "%~1""
) else (
    set "RUN_COMMAND="%~1""
)
goto run_in_dir_no_fail_args
:run_in_dir_no_fail_ready
echo [%WORK_DIR%] !RUN_COMMAND!
if "%DRY_RUN%"=="1" exit /b 0
pushd "%WORK_DIR%" || exit /b 1
call !RUN_COMMAND!
set "RUN_EXIT=%ERRORLEVEL%"
popd
exit /b %RUN_EXIT%

:fail
echo.
echo ========== failed ==========
set "MAVEN_OPTS=%OLD_MAVEN_OPTS%"
if not "%NO_PAUSE%"=="1" pause
exit /b 1

:nothing_to_release
echo.
echo ========== nothing to release ==========
echo nettyx has no git changes. Release stopped before version update, install, git push, and deploy.
if not "%NO_PAUSE%"=="1" pause
exit /b 0

:done
set "MAVEN_OPTS=%OLD_MAVEN_OPTS%"
if not "%NO_PAUSE%"=="1" pause
exit /b 0

@end

var fso = new ActiveXObject("Scripting.FileSystemObject");

var POM_NS = "http://maven.apache.org/POM/4.0.0";

function fail(message) {
    WScript.StdErr.WriteLine(message);
    WScript.Quit(1);
}

function trim(value) {
    return String(value).replace(/^\s+|\s+$/g, "");
}

function escapeRegExp(value) {
    return String(value).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

function pomPath(projectPath) {
    return fso.BuildPath(projectPath, "pom.xml");
}

function readUtf8(path) {
    var stream = new ActiveXObject("ADODB.Stream");
    stream.Type = 2;
    stream.Charset = "utf-8";
    stream.Open();
    stream.LoadFromFile(path);
    var text = stream.ReadText();
    stream.Close();
    return text;
}

function writeUtf8NoBom(path, text) {
    var textStream = new ActiveXObject("ADODB.Stream");
    textStream.Type = 2;
    textStream.Charset = "utf-8";
    textStream.Open();
    textStream.WriteText(text);
    textStream.Position = 3;

    var binaryStream = new ActiveXObject("ADODB.Stream");
    binaryStream.Type = 1;
    binaryStream.Open();
    textStream.CopyTo(binaryStream);
    binaryStream.SaveToFile(path, 2);
    binaryStream.Close();
    textStream.Close();
}

function requirePom(projectPath) {
    var path = pomPath(projectPath);
    if (!fso.FileExists(path)) {
        fail("pom.xml does not exist: " + path);
    }
    return path;
}

function loadDoc(path) {
    var doc = new ActiveXObject("MSXML2.DOMDocument.6.0");
    doc.async = false;
    doc.preserveWhiteSpace = true;
    doc.setProperty("SelectionNamespaces", "xmlns:m='" + POM_NS + "'");
    if (!doc.load(path)) {
        fail("Failed to parse pom.xml: " + path + " - " + doc.parseError.reason);
    }
    return doc;
}

function selectNode(node, xpath) {
    return node ? node.selectSingleNode(xpath) : null;
}

function selectNodes(node, xpath) {
    return node ? node.selectNodes(xpath) : null;
}

function getNodeText(node) {
    return node ? trim(node.text) : "";
}

function resolvePropertyRefs(pomText, value) {
    var resolved = trim(value);
    for (var i = 0; i < 20; i++) {
        var changed = false;
        resolved = resolved.replace(/\$\{([^}]+)\}/g, function(all, name) {
            var propRegex = new RegExp("<" + escapeRegExp(name) + ">([^<]*)</" + escapeRegExp(name) + ">");
            var match = pomText.match(propRegex);
            if (match) {
                changed = true;
                return trim(match[1]);
            }
            return all;
        });
        if (!changed || resolved.indexOf("${") < 0) break;
    }
    return resolved;
}

function getProjectVersion(projectPath, artifactId) {
    var path = requirePom(projectPath);
    var text = readUtf8(path);
    var doc = loadDoc(path);
    var versionEl = selectNode(doc, "/m:project/m:version");
    var rawVersion = getNodeText(versionEl);

    if (rawVersion === "${revision}") {
        var revision = getNodeText(selectNode(doc, "/m:project/m:properties/m:revision"));
        if (!revision) fail("Revision property not found in " + path);
        WScript.Echo(resolvePropertyRefs(text, revision));
    } else {
        WScript.Echo(resolvePropertyRefs(text, rawVersion));
    }
}

function getPropertyVersion(projectPath, propertyName) {
    var path = requirePom(projectPath);
    var doc = loadDoc(path);
    var propEl = selectNode(doc, "/m:project/m:properties/m:" + propertyName);
    if (!propEl) fail("Property not found: " + propertyName + " in " + path);
    WScript.Echo(getNodeText(propEl));
}

function setProjectVersion(projectPath, artifactId, newVersion, dryRun) {
    var path = requirePom(projectPath);
    var doc = loadDoc(path);
    var versionEl = selectNode(doc, "/m:project/m:version");

    if (!versionEl || getNodeText(versionEl) === "") {
        var revisionEl = selectNode(doc, "/m:project/m:properties/m:revision");
        if (revisionEl) {
            setNodeText(path, doc, revisionEl, newVersion, "property revision", dryRun);
            return;
        }
        fail("Cannot find version element for " + artifactId + " in " + path);
    }
    setNodeText(path, doc, versionEl, newVersion, "project version for " + artifactId, dryRun);
}

function setPropertyVersion(projectPath, propertyName, newVersion, dryRun) {
    var path = requirePom(projectPath);
    var doc = loadDoc(path);
    var propEl = selectNode(doc, "/m:project/m:properties/m:" + propertyName);
    if (!propEl) fail("Property not found: " + propertyName + " in " + path);
    setNodeText(path, doc, propEl, newVersion, "property " + propertyName, dryRun);
}

function setNodeText(path, doc, node, newValue, subject, dryRun) {
    var oldValue = getNodeText(node);
    if (oldValue === newValue) {
        WScript.Echo("Version unchanged: " + path + " " + subject + " -> " + newValue);
        return;
    }
    if (dryRun) {
        WScript.Echo("DryRun: " + path + " " + subject + " " + oldValue + " -> " + newValue);
        return;
    }
    node.text = newValue;
    var xml = '<?xml version="1.0" encoding="UTF-8"?>\r\n' + doc.documentElement.xml;
    writeUtf8NoBom(path, xml);
    WScript.Echo("Updated version: " + path + " " + subject + " " + oldValue + " -> " + newValue);
}

function removeNode(doc, xpath) {
    var node = selectNode(doc, xpath);
    if (node) {
        var parent = node.parentNode;
        parent.removeChild(node);
        return true;
    }
    return false;
}

function cleanNettyxDevPom(projectPath, dryRun) {
    var path = requirePom(projectPath);
    var doc = loadDoc(path);
    var changed = false;

    if (removeNode(doc, "/m:project/m:properties/m:junit.version")) changed = true;

    var artifactNames = ["junit"];
    for (var i = 0; i < artifactNames.length; i++) {
        var deps = selectNodes(doc, "/m:project/m:dependencies/m:dependency[m:artifactId='" + artifactNames[i] + "']");
        if (deps) {
            for (var j = deps.length - 1; j >= 0; j--) {
                var parent = deps[j].parentNode;
                parent.removeChild(deps[j]);
                changed = true;
            }
        }
    }

    if (!changed) {
        WScript.Echo("Nettyx dev pom unchanged after test dependency cleanup: " + path);
        return;
    }

    if (dryRun) {
        WScript.Echo("DryRun: clean nettyx dev pom test-only properties and dependencies: " + path);
        return;
    }

    var xml = readUtf8(path);
    var cleaned = xml;

    // Remove <!--test--> comments that were adjacent to removed dependencies
    cleaned = cleaned.replace(/^[ \t]*<!--test-->\r?\n?/gm, "");

    // The DOM output preserves whitespace, but leftover blank lines from removed elements need cleanup
    cleaned = cleaned.replace(/\r?\n{3,}/g, "\r\n\r\n");

    // Remove empty <dependencies> element if all deps were removed
    cleaned = cleaned.replace(/^[ \t]*<dependencies>\s*[\r\n]+\s*<\/dependencies>\r?\n?/gm, "");

    // Remove empty <properties> element if all props were removed
    cleaned = cleaned.replace(/^[ \t]*<properties>\s*[\r\n]+\s*<\/properties>\r?\n?/gm, "");

    if (cleaned !== xml) {
        writeUtf8NoBom(path, cleaned);
    }
    WScript.Echo("Cleaned nettyx dev pom test-only properties and dependencies: " + path);
}

function nextVersion(currentVersion) {
    var match = /^(\d+)\.(\d+)\.(\d+)(.*)$/.exec(trim(currentVersion));
    if (!match) {
        fail("Version must be MAJOR.MINOR.PATCH with optional suffix: " + currentVersion);
    }

    var major = parseInt(match[1], 10);
    var minor = parseInt(match[2], 10);
    var patch = parseInt(match[3], 10) + 1;
    var suffix = match[4];

    if (patch > 99) {
        patch = 0;
        minor++;
    }
    if (minor > 99) {
        minor = 0;
        major++;
    }

    WScript.Echo(String(major) + "." + String(minor) + "." + String(patch) + suffix);
}

function nextVersionValue(currentVersion) {
    var match = /^(\d+)\.(\d+)\.(\d+)(.*)$/.exec(trim(currentVersion));
    if (!match) {
        fail("Version must be MAJOR.MINOR.PATCH with optional suffix: " + currentVersion);
    }

    var major = parseInt(match[1], 10);
    var minor = parseInt(match[2], 10);
    var patch = parseInt(match[3], 10) + 1;
    var suffix = match[4];

    if (patch > 99) {
        patch = 0;
        minor++;
    }
    if (minor > 99) {
        minor = 0;
        major++;
    }

    return String(major) + "." + String(minor) + "." + String(patch) + suffix;
}

function nextProjectVersion(projectPath, artifactId, nettyxVersion) {
    var path = requirePom(projectPath);
    var text = readUtf8(path);
    var doc = loadDoc(path);
    var versionEl = selectNode(doc, "/m:project/m:version");
    var rawVersion = getNodeText(versionEl);

    if (rawVersion === "${revision}") {
        var revision = getNodeText(selectNode(doc, "/m:project/m:properties/m:revision"));
        if (revision && revision.indexOf("${") >= 0) {
            WScript.Echo(resolvePropertyRefs(text, revision));
            return;
        }
        WScript.Echo(nextVersionValue(revision || rawVersion));
        return;
    }

    WScript.Echo(nextVersionValue(rawVersion));
}

if (WScript.Arguments.length < 1) {
    fail("Missing helper command.");
}

var command = WScript.Arguments(0);
if (command === "getProjectVersion") {
    getProjectVersion(WScript.Arguments(1), WScript.Arguments(2));
} else if (command === "getPropertyVersion") {
    getPropertyVersion(WScript.Arguments(1), WScript.Arguments(2));
} else if (command === "setProjectVersion") {
    setProjectVersion(WScript.Arguments(1), WScript.Arguments(2), WScript.Arguments(3), WScript.Arguments.length > 4 && WScript.Arguments(4) === "dryRun");
} else if (command === "setPropertyVersion") {
    setPropertyVersion(WScript.Arguments(1), WScript.Arguments(2), WScript.Arguments(3), WScript.Arguments.length > 4 && WScript.Arguments(4) === "dryRun");
} else if (command === "cleanNettyxDevPom") {
    cleanNettyxDevPom(WScript.Arguments(1), WScript.Arguments.length > 2 && WScript.Arguments(2) === "dryRun");
} else if (command === "nextVersion") {
    nextVersion(WScript.Arguments(1));
} else if (command === "nextProjectVersion") {
    nextProjectVersion(WScript.Arguments(1), WScript.Arguments(2), WScript.Arguments(3));
} else {
    fail("Unknown helper command: " + command);
}
