@if (@CodeSection)==(@Batch) @then
@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "MAVEN_HOME=D:\maven\apache-maven-3.9.14"
set "MAVEN_SETTINGS=D:\maven\settings.xml"
set "MAVEN_REPO=D:\maven\repository"

set "PROJECT_NETTYX=D:\workspace\nettyx"
set "PROJECT_STARTER=D:\workspace\spring-boot-starter-nettyx"

set "MVN=%MAVEN_HOME%\bin\mvn.cmd"

set "NETTYX_VERSION="
set "STARTER_VERSION="
set "NETTYX_REMOTES=gitee github"
set "STARTER_REMOTES=gitee github"
set "NETTYX_FEATURE_BRANCH="
set "NETTYX_DEV_BRANCH=develop"
set "NETTYX_RELEASE_BRANCH=release"
set "NETTYX_MAIN_BRANCH=main"
set "STARTER_PUBLISH_BRANCH=publish"
set "STARTER_MAIN_BRANCH=main"
set "FEATURE_COMMIT_MESSAGE=feat: update nettyx"
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
if /i "%ARG:~0,21%"=="/nettyxFeatureBranch:" set "NETTYX_FEATURE_BRANCH=%ARG:~21%" & shift & goto parse_args
if /i "%ARG:~0,21%"=="-nettyxFeatureBranch:" set "NETTYX_FEATURE_BRANCH=%ARG:~21%" & shift & goto parse_args
if /i "%ARG:~0,17%"=="/nettyxDevBranch:" set "NETTYX_DEV_BRANCH=%ARG:~17%" & shift & goto parse_args
if /i "%ARG:~0,17%"=="-nettyxDevBranch:" set "NETTYX_DEV_BRANCH=%ARG:~17%" & shift & goto parse_args
if /i "%ARG:~0,21%"=="/nettyxReleaseBranch:" set "NETTYX_RELEASE_BRANCH=%ARG:~21%" & shift & goto parse_args
if /i "%ARG:~0,21%"=="-nettyxReleaseBranch:" set "NETTYX_RELEASE_BRANCH=%ARG:~21%" & shift & goto parse_args
if /i "%ARG:~0,18%"=="/nettyxMainBranch:" set "NETTYX_MAIN_BRANCH=%ARG:~18%" & shift & goto parse_args
if /i "%ARG:~0,18%"=="-nettyxMainBranch:" set "NETTYX_MAIN_BRANCH=%ARG:~18%" & shift & goto parse_args
if /i "%ARG:~0,22%"=="/starterPublishBranch:" set "STARTER_PUBLISH_BRANCH=%ARG:~22%" & shift & goto parse_args
if /i "%ARG:~0,22%"=="-starterPublishBranch:" set "STARTER_PUBLISH_BRANCH=%ARG:~22%" & shift & goto parse_args
if /i "%ARG:~0,19%"=="/starterMainBranch:" set "STARTER_MAIN_BRANCH=%ARG:~19%" & shift & goto parse_args
if /i "%ARG:~0,19%"=="-starterMainBranch:" set "STARTER_MAIN_BRANCH=%ARG:~19%" & shift & goto parse_args
if /i "%ARG:~0,22%"=="/featureCommitMessage:" set "FEATURE_COMMIT_MESSAGE=%ARG:~22%" & shift & goto parse_args
if /i "%ARG:~0,22%"=="-featureCommitMessage:" set "FEATURE_COMMIT_MESSAGE=%ARG:~22%" & shift & goto parse_args
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
call :assert_path "%MVN%" "mvn.cmd" || goto fail
call :assert_path "%MAVEN_SETTINGS%" "Maven settings.xml" || goto fail
call :assert_path "%MAVEN_REPO%" "Maven local repository" || goto fail
call :assert_path "%PROJECT_NETTYX%" "Project directory" || goto fail
call :assert_path "%PROJECT_STARTER%" "Project directory" || goto fail

call :get_current_branch "%PROJECT_NETTYX%" CURRENT_NETTYX_BRANCH || goto fail
if not defined NETTYX_FEATURE_BRANCH set "NETTYX_FEATURE_BRANCH=%CURRENT_NETTYX_BRANCH%"
call :first_word "%NETTYX_REMOTES%" NETTYX_PRIMARY_REMOTE || goto fail
call :first_word "%STARTER_REMOTES%" STARTER_PRIMARY_REMOTE || goto fail

call :nettyx_has_updates "%PROJECT_NETTYX%" "%NETTYX_FEATURE_BRANCH%" "%NETTYX_DEV_BRANCH%" "%NETTYX_PRIMARY_REMOTE%" NETTYX_HAS_CHANGES || goto fail
if "%NETTYX_HAS_CHANGES%"=="0" goto nothing_to_release

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
echo nettyx branches:            %NETTYX_FEATURE_BRANCH% -^> %NETTYX_DEV_BRANCH% -^> %NETTYX_RELEASE_BRANCH% -^> %NETTYX_MAIN_BRANCH%
echo starter branches:           %STARTER_PUBLISH_BRANCH% -^> %STARTER_MAIN_BRANCH%
if "%DRY_RUN%"=="1" echo DryRun is enabled. No files, git refs, or deployments will be changed.

set "OLD_MAVEN_OPTS=%MAVEN_OPTS%"
set "MAVEN_OPTS=-Xms512m -Xmx2048m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Duser.language=zh -Duser.country=CN --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
echo MAVEN_OPTS=%MAVEN_OPTS%

call :publish_nettyx || goto fail
call :publish_starter || goto fail

echo.
echo ========== done ==========
echo nettyx version: %NETTYX_VERSION%
echo spring-boot-starter-nettyx version: %STARTER_VERSION%
goto done

:assert_path
if not exist "%~1" (
    echo %~2 does not exist: %~1
    exit /b 1
)
exit /b 0

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
    del "%CHECK_STATUS_FILE%" >nul 2>nul
    echo Failed to check git status: %CHECK_PROJECT_PATH%
    exit /b 1
)
for %%S in ("%CHECK_STATUS_FILE%") do set "CHECK_STATUS_SIZE=%%~zS"
del "%CHECK_STATUS_FILE%" >nul 2>nul
if "%CHECK_STATUS_SIZE%"=="0" (
    set "%~2=0"
) else (
    set "%~2=1"
)
exit /b 0

:resolve_branch_ref
set "RESOLVE_PROJECT_PATH=%~1"
set "RESOLVE_BRANCH=%~2"
set "RESOLVE_REMOTE=%~3"
git -C "%RESOLVE_PROJECT_PATH%" rev-parse --verify --quiet "%RESOLVE_BRANCH%" >nul
if "%ERRORLEVEL%"=="0" (
    set "%~4=%RESOLVE_BRANCH%"
    exit /b 0
)
git -C "%RESOLVE_PROJECT_PATH%" rev-parse --verify --quiet "%RESOLVE_REMOTE%/%RESOLVE_BRANCH%" >nul
if "%ERRORLEVEL%"=="0" (
    set "%~4=%RESOLVE_REMOTE%/%RESOLVE_BRANCH%"
    exit /b 0
)
echo Cannot resolve branch ref: %RESOLVE_BRANCH%, project: %RESOLVE_PROJECT_PATH%
exit /b 1

:branch_has_diff
call :resolve_branch_ref "%~1" "%~2" "%~4" DIFF_LEFT_REF || exit /b 1
call :resolve_branch_ref "%~1" "%~3" "%~4" DIFF_RIGHT_REF || exit /b 1
git -C "%~1" diff --quiet "%DIFF_RIGHT_REF%...%DIFF_LEFT_REF%" --
if "%ERRORLEVEL%"=="0" (
    set "%~5=0"
) else (
    set "%~5=1"
)
exit /b 0

:nettyx_has_updates
call :repo_has_changes "%~1" NETTYX_WORKTREE_HAS_CHANGES || exit /b 1
if "%NETTYX_WORKTREE_HAS_CHANGES%"=="1" (
    set "%~5=1"
    exit /b 0
)
call :branch_has_diff "%~1" "%~2" "%~3" "%~4" NETTYX_BRANCH_HAS_DIFF || exit /b 1
set "%~5=%NETTYX_BRANCH_HAS_DIFF%"
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
echo ========== nettyx feature commit ==========
call :switch_branch "%PROJECT_NETTYX%" "%NETTYX_FEATURE_BRANCH%" "%NETTYX_PRIMARY_REMOTE%" || exit /b 1
call :commit_current_changes "nettyx feature" "%PROJECT_NETTYX%" "%FEATURE_COMMIT_MESSAGE%" || exit /b 1
call :push_branch "%PROJECT_NETTYX%" "%NETTYX_FEATURE_BRANCH%" "%NETTYX_REMOTES%" || exit /b 1

echo.
echo ========== nettyx develop release commit ==========
call :switch_branch "%PROJECT_NETTYX%" "%NETTYX_DEV_BRANCH%" "%NETTYX_PRIMARY_REMOTE%" || exit /b 1
call :run_in_dir "%PROJECT_NETTYX%" git -C "%PROJECT_NETTYX%" merge --squash "%NETTYX_FEATURE_BRANCH%" || exit /b 1
call :restore_head_paths "%PROJECT_NETTYX%" README.md README_zh.md || exit /b 1
call :drop_or_restore_head_paths "%PROJECT_NETTYX%" src/test/java || exit /b 1
call :clean_nettyx_develop_pom "%PROJECT_NETTYX%" || exit /b 1
call :set_project_version "%PROJECT_NETTYX%" "nettyx" "%NETTYX_VERSION%" || exit /b 1
call :stage_release_changes "%PROJECT_NETTYX%" || exit /b 1
call :commit_staged_changes "nettyx develop" "%PROJECT_NETTYX%" "chore release: nettyx %NETTYX_VERSION%" || exit /b 1
call :push_branch "%PROJECT_NETTYX%" "%NETTYX_DEV_BRANCH%" "%NETTYX_REMOTES%" || exit /b 1

echo.
echo ========== nettyx release merge ==========
call :switch_branch "%PROJECT_NETTYX%" "%NETTYX_RELEASE_BRANCH%" "%NETTYX_PRIMARY_REMOTE%" || exit /b 1
call :run_in_dir "%PROJECT_NETTYX%" git -C "%PROJECT_NETTYX%" merge --no-ff "%NETTYX_DEV_BRANCH%" -m "chore release: nettyx %NETTYX_VERSION%" || exit /b 1
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
call :run_in_dir "%PROJECT_STARTER%" git -C "%PROJECT_STARTER%" merge --no-ff "%STARTER_PUBLISH_BRANCH%" -m "chore release: spring-boot-starter-nettyx %STARTER_VERSION%" || exit /b 1
call :push_branch "%PROJECT_STARTER%" "%STARTER_MAIN_BRANCH%" "%STARTER_REMOTES%" || exit /b 1
exit /b 0

:set_project_version
if "%DRY_RUN%"=="1" (
    cscript //nologo //E:JScript "%~f0" setProjectVersion "%~1" "%~2" "%~3" dryRun
) else (
    cscript //nologo //E:JScript "%~f0" setProjectVersion "%~1" "%~2" "%~3"
)
exit /b %ERRORLEVEL%

:set_property_version
if "%DRY_RUN%"=="1" (
    cscript //nologo //E:JScript "%~f0" setPropertyVersion "%~1" "%~2" "%~3" dryRun
) else (
    cscript //nologo //E:JScript "%~f0" setPropertyVersion "%~1" "%~2" "%~3"
)
exit /b %ERRORLEVEL%

:clean_nettyx_develop_pom
if "%DRY_RUN%"=="1" (
    cscript //nologo //E:JScript "%~f0" cleanNettyxDevelopPom "%~1" dryRun
) else (
    cscript //nologo //E:JScript "%~f0" cleanNettyxDevelopPom "%~1"
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

:commit_and_push
set "COMMIT_PROJECT_NAME=%~1"
set "COMMIT_PROJECT_PATH=%~2"
set "COMMIT_REMOTES=%~3"
set "COMMIT_RELEASE_VERSION=%~4"

if "%SKIP_GIT_PUSH%"=="1" (
    echo Skipped git push: %COMMIT_PROJECT_NAME%
    exit /b 0
)

set "BRANCH="
for /f "usebackq delims=" %%B in (`git -C "%COMMIT_PROJECT_PATH%" branch --show-current`) do set "BRANCH=%%B"
if not defined BRANCH (
    echo Cannot resolve current branch: %COMMIT_PROJECT_PATH%
    exit /b 1
)

set "STATUS_FILE=%TEMP%\fz_publish_status_%RANDOM%%RANDOM%.txt"
git -C "%COMMIT_PROJECT_PATH%" status --porcelain > "%STATUS_FILE%"
for %%S in ("%STATUS_FILE%") do set "STATUS_SIZE=%%~zS"

if not "%STATUS_SIZE%"=="0" (
    echo Changes to commit:
    type "%STATUS_FILE%"
    del "%STATUS_FILE%" >nul 2>nul
    call :stage_release_changes "%COMMIT_PROJECT_PATH%" || exit /b 1
    if "%DRY_RUN%"=="1" (
        call :run_in_dir "%COMMIT_PROJECT_PATH%" git -C "%COMMIT_PROJECT_PATH%" commit -m "chore: release %COMMIT_PROJECT_NAME% %COMMIT_RELEASE_VERSION%" || exit /b 1
    ) else (
        git -C "%COMMIT_PROJECT_PATH%" diff --cached --quiet
        if "%ERRORLEVEL%"=="0" (
            echo No stageable repository changes to commit: %COMMIT_PROJECT_NAME%
        ) else (
            call :run_in_dir "%COMMIT_PROJECT_PATH%" git -C "%COMMIT_PROJECT_PATH%" commit -m "chore: release %COMMIT_PROJECT_NAME% %COMMIT_RELEASE_VERSION%" || exit /b 1
        )
    )
) else (
    del "%STATUS_FILE%" >nul 2>nul
    echo No repository changes to commit: %COMMIT_PROJECT_NAME%
)

for %%R in (%COMMIT_REMOTES%) do (
    call :run_in_dir "%COMMIT_PROJECT_PATH%" git -C "%COMMIT_PROJECT_PATH%" push %%R HEAD:%BRANCH% || exit /b 1
)
exit /b 0

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

function projectVersionPattern(artifactId) {
    return new RegExp("(<artifactId>\\s*" + escapeRegExp(artifactId) + "\\s*</artifactId>\\s*<version>)([\\s\\S]*?)(</version>)", "g");
}

function propertyVersionPattern(propertyName) {
    return new RegExp("(<" + escapeRegExp(propertyName) + ">)([\\s\\S]*?)(</" + escapeRegExp(propertyName) + ">)", "g");
}

function getSingleMatch(text, pattern, path, subject) {
    var match;
    var count = 0;
    var value = "";
    while ((match = pattern.exec(text)) !== null) {
        count++;
        value = trim(match[2]);
    }
    if (count !== 1) {
        fail("Expected exactly one " + subject + ", but found " + count + ": " + path);
    }
    return value;
}

function getPropertyValueFromText(text, path, propertyName, required) {
    var pattern = propertyVersionPattern(propertyName);
    var match;
    var count = 0;
    var value = "";
    while ((match = pattern.exec(text)) !== null) {
        count++;
        value = trim(match[2]);
    }
    if (count === 0 && !required) {
        return "";
    }
    if (count !== 1) {
        fail("Expected exactly one property " + propertyName + ", but found " + count + ": " + path);
    }
    return value;
}

function resolveProperties(text, path, value, overrides) {
    var resolved = trim(value);
    for (var i = 0; i < 20; i++) {
        var changed = false;
        resolved = resolved.replace(/\$\{([^}]+)\}/g, function(all, name) {
            if (overrides && overrides.hasOwnProperty(name)) {
                changed = true;
                return overrides[name];
            }
            var prop = getPropertyValueFromText(text, path, name, false);
            if (prop === "") {
                return all;
            }
            changed = true;
            return prop;
        });
        if (!changed || resolved.indexOf("${") < 0) {
            break;
        }
    }
    return resolved;
}

function rawProjectVersion(text, path, artifactId) {
    return getSingleMatch(text, projectVersionPattern(artifactId), path, "project version for " + artifactId);
}

function resolvedProjectVersion(text, path, artifactId, overrides) {
    return resolveProperties(text, path, rawProjectVersion(text, path, artifactId), overrides || {});
}

function getProjectVersion(projectPath, artifactId) {
    var path = requirePom(projectPath);
    var text = readUtf8(path);
    WScript.Echo(resolvedProjectVersion(text, path, artifactId, {}));
}

function getPropertyVersion(projectPath, propertyName) {
    var path = requirePom(projectPath);
    var text = readUtf8(path);
    WScript.Echo(getSingleMatch(text, propertyVersionPattern(propertyName), path, "property " + propertyName));
}

function setByPattern(projectPath, pattern, newVersion, subject, dryRun) {
    var path = requirePom(projectPath);
    var text = readUtf8(path);
    var oldVersion = getSingleMatch(text, pattern, path, subject);

    if (oldVersion === newVersion) {
        WScript.Echo("Version unchanged: " + projectPath + " " + subject + " -> " + newVersion);
        return;
    }

    if (dryRun) {
        WScript.Echo("DryRun: " + path + " " + subject + " " + oldVersion + " -> " + newVersion);
        return;
    }

    var newText = text.replace(pattern, "$1" + newVersion + "$3");
    writeUtf8NoBom(path, newText);
    WScript.Echo("Updated version: " + projectPath + " " + subject + " " + oldVersion + " -> " + newVersion);
}

function setProjectVersion(projectPath, artifactId, newVersion, dryRun) {
    var path = requirePom(projectPath);
    var text = readUtf8(path);
    var rawVersion = rawProjectVersion(text, path, artifactId);

    if (rawVersion === "${revision}") {
        var revision = getPropertyValueFromText(text, path, "revision", true);
        if (revision.indexOf("${") >= 0) {
            WScript.Echo("Project version is managed by revision expression, unchanged: " + projectPath + " -> " + revision);
            return;
        }
        setByPattern(projectPath, propertyVersionPattern("revision"), newVersion, "property revision", dryRun);
        return;
    }

    setByPattern(projectPath, projectVersionPattern(artifactId), newVersion, "project version for " + artifactId, dryRun);
}

function setPropertyVersion(projectPath, propertyName, newVersion, dryRun) {
    setByPattern(projectPath, propertyVersionPattern(propertyName), newVersion, "property " + propertyName, dryRun);
}

function removeProperty(text, propertyName) {
    var pattern = new RegExp("^[ \\t]*<" + escapeRegExp(propertyName) + ">[\\s\\S]*?</" + escapeRegExp(propertyName) + ">\\r?\\n?", "gm");
    return text.replace(pattern, "");
}

function removeDependencyByArtifact(text, artifactId) {
    return text.replace(/^[ \t]*<dependency>[\s\S]*?<\/dependency>\r?\n?/gm, function(block) {
        var artifactPattern = new RegExp("<artifactId>\\s*" + escapeRegExp(artifactId) + "\\s*</artifactId>");
        return artifactPattern.test(block) ? "" : block;
    });
}

function cleanNettyxDevelopPom(projectPath, dryRun) {
    var path = requirePom(projectPath);
    var text = readUtf8(path);
    var cleaned = text;

    cleaned = cleaned.replace(/^[ \t]*<!--test-->\r?\n?/gm, "");

    var properties = [
        "protostuff-core.version",
        "junit.version",
        "cglib.version",
        "javolution-core-java.version"
    ];
    for (var i = 0; i < properties.length; i++) {
        cleaned = removeProperty(cleaned, properties[i]);
    }

    var artifacts = [
        "protostuff-core",
        "protostuff-runtime",
        "junit",
        "cglib",
        "javolution-core-java"
    ];
    for (var j = 0; j < artifacts.length; j++) {
        cleaned = removeDependencyByArtifact(cleaned, artifacts[j]);
    }

    cleaned = cleaned.replace(/\r?\n{3,}/g, "\r\n\r\n");

    if (cleaned === text) {
        WScript.Echo("Nettyx develop pom unchanged after test dependency cleanup: " + path);
        return;
    }

    if (dryRun) {
        WScript.Echo("DryRun: clean nettyx develop pom test-only properties and dependencies: " + path);
        return;
    }

    writeUtf8NoBom(path, cleaned);
    WScript.Echo("Cleaned nettyx develop pom test-only properties and dependencies: " + path);
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
    var rawVersion = rawProjectVersion(text, path, artifactId);

    if (rawVersion === "${revision}") {
        var revision = getPropertyValueFromText(text, path, "revision", true);
        if (revision.indexOf("${") >= 0) {
            WScript.Echo(resolveProperties(text, path, revision, { "nettyx.version": nettyxVersion }));
            return;
        }
        WScript.Echo(nextVersionValue(revision));
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
} else if (command === "cleanNettyxDevelopPom") {
    cleanNettyxDevelopPom(WScript.Arguments(1), WScript.Arguments.length > 2 && WScript.Arguments(2) === "dryRun");
} else if (command === "nextVersion") {
    nextVersion(WScript.Arguments(1));
} else if (command === "nextProjectVersion") {
    nextProjectVersion(WScript.Arguments(1), WScript.Arguments(2), WScript.Arguments(3));
} else {
    fail("Unknown helper command: " + command);
}
