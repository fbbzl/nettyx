param(
    [ValidateSet('prepare', 'benchmark')]
    [string]$Mode = 'prepare',
    [string]$WorkDirectory
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$maven = 'D:\Maven\apache-maven-3.9.16\bin\mvn.cmd'
$java = 'D:\Java\openjdk\jdk-21\bin\java.exe'

function Invoke-NativeCommand {
    param (
        [Parameter(Mandatory = $true)]
        [string] $Executable,
        [Parameter(Mandatory = $true)]
        [string[]] $Arguments,
        [Parameter(Mandatory = $true)]
        [string] $LogPath
    )

    $previousErrorActionPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        & $Executable @Arguments *> $LogPath
        return $LASTEXITCODE
    }
    finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
}

$trialRoot = if ($WorkDirectory) {
    [System.IO.Path]::GetFullPath($WorkDirectory)
}
else {
    Join-Path $env:TEMP ('nettyx-generic-library-comparison-' + [Guid]::NewGuid().ToString('N'))
}

if (Test-Path -LiteralPath $trialRoot) {
    throw "Comparison output already exists: $trialRoot"
}
New-Item -ItemType Directory -Path $trialRoot | Out-Null

$geoRoot = Join-Path $trialRoot 'geantyref'
$guavaRoot = Join-Path $trialRoot 'guava'
foreach ($variantRoot in @($geoRoot, $guavaRoot)) {
    New-Item -ItemType Directory -Path $variantRoot | Out-Null
    Copy-Item -LiteralPath (Join-Path $repoRoot 'pom.xml') -Destination $variantRoot
    Copy-Item -LiteralPath (Join-Path $repoRoot 'src') -Destination $variantRoot -Recurse
}

$paths = @(
    'src/main/java/org/fz/nettyx/serializer/struct/StructContext.java',
    'src/main/java/org/fz/nettyx/serializer/struct/annotation/ToArray.java',
    'src/main/java/org/fz/nettyx/serializer/struct/generator/StructAccessor.java'
)
foreach ($relativePath in $paths) {
    $geoFile = Join-Path $geoRoot $relativePath
    $guavaFile = Join-Path $guavaRoot $relativePath
    $guavaText = [System.IO.File]::ReadAllText($guavaFile, [System.Text.Encoding]::UTF8)
    $geoText = $guavaText.Replace('TypeToken.of(actualRoot).resolveType(fieldType).getType()', 'GenericTypeReflector.resolveType(fieldType, actualRoot)')
    $geoText = $geoText.Replace('TypeToken.of(root)' + [Environment]::NewLine + '                        .resolveType(genericArrayType.getGenericComponentType()).getType()', 'GenericTypeReflector.resolveType(genericArrayType.getGenericComponentType(), root)')
    $geoText = $geoText.Replace('TypeToken.of(root).resolveType(structType).getType()', 'GenericTypeReflector.resolveType(structType, root)')
    if ($guavaText -eq $geoText -or -not $geoText.Contains('GenericTypeReflector.resolveType(')) {
        throw "Unexpected Guava call shape; review comparison transform for $relativePath"
    }
    $geoImport = 'import io.leangen.geantyref.GenericTypeReflector;'
    if ($geoText.Contains('TypeToken.of(')) {
        $geoText = $geoText.Replace('import com.google.common.reflect.TypeToken;',
            'import com.google.common.reflect.TypeToken;' + [Environment]::NewLine + $geoImport)
    }
    else {
        $geoText = $geoText.Replace('import com.google.common.reflect.TypeToken;', $geoImport)
    }
    [System.IO.File]::WriteAllText($geoFile, $geoText, [System.Text.UTF8Encoding]::new($false))
}

$geoPomPath = Join-Path $geoRoot 'pom.xml'
$geoPom = New-Object System.Xml.XmlDocument
$geoPom.PreserveWhitespace = $true
$geoPom.Load($geoPomPath)
$pomNamespace = New-Object System.Xml.XmlNamespaceManager($geoPom.NameTable)
$pomNamespace.AddNamespace('m', $geoPom.DocumentElement.NamespaceURI)
$geoScope = $geoPom.SelectSingleNode("/m:project/m:dependencies/m:dependency[m:groupId='io.leangen.geantyref']/m:scope", $pomNamespace)
if ($null -eq $geoScope -or $geoScope.InnerText -ne 'test') { throw 'Expected a test-only GeAnTyRef dependency' }
$geoScope.InnerText = 'compile'
$geoPom.Save($geoPomPath)

$jdkStdout = Join-Path $trialRoot 'jdk-version.stdout.txt'
$jdkStderr = Join-Path $trialRoot 'jdk-version.stderr.txt'
$jdkProcess = Start-Process -FilePath $java -ArgumentList '-version' -NoNewWindow -Wait -PassThru `
    -RedirectStandardOutput $jdkStdout -RedirectStandardError $jdkStderr
if ($jdkProcess.ExitCode -ne 0) { throw "Could not read Java version (exit code $($jdkProcess.ExitCode))" }
$jdkOutput = @(
    [System.IO.File]::ReadAllLines($jdkStderr)
    [System.IO.File]::ReadAllLines($jdkStdout)
) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
if ($jdkOutput.Count -eq 0) { throw 'Java version output was empty' }
$versions = [ordered]@{
    jdk = $jdkOutput[0]
    guava = '33.6.0-jre'
    commonsLang = '3.21.0'
    springCore = '7.0.10'
    geantyref = '2.0.1'
    jmh = '1.37'
}
$contract = Join-Path $trialRoot 'candidate-contracts.json'
foreach ($variantRoot in @($geoRoot, $guavaRoot)) {
    Push-Location $variantRoot
    try {
        $testLog = Join-Path $trialRoot ((Split-Path $variantRoot -Leaf) + '-maven-test.log')
        $testExitCode = Invoke-NativeCommand -Executable $maven `
            -Arguments @('-Pgeneric-library-comparison', '-o', 'test') -LogPath $testLog
        if ($testExitCode -ne 0) { throw "Maven tests failed with exit code $testExitCode. See $testLog" }
        $testClass = Join-Path $variantRoot 'target/test-classes'
        $mainClass = Join-Path $variantRoot 'target/classes'
        $classpathFile = Join-Path $trialRoot ((Split-Path $variantRoot -Leaf) + '-classpath.txt')
        $classpathLog = Join-Path $trialRoot ((Split-Path $variantRoot -Leaf) + '-classpath.log')
        $classpathArgument = '-Dmdep.outputFile=' + $classpathFile
        $classpathExitCode = Invoke-NativeCommand -Executable $maven `
            -Arguments @('-Pgeneric-library-comparison', '-q', 'dependency:build-classpath', $classpathArgument) `
            -LogPath $classpathLog
        if ($classpathExitCode -ne 0) {
            throw "Could not build classpath for $variantRoot (exit code $classpathExitCode). See $classpathLog"
        }
        $classpath = "$testClass;$mainClass;" + (Get-Content -LiteralPath $classpathFile -Raw).Trim()
        if ($variantRoot -eq $geoRoot) {
            $geoContract = Join-Path $trialRoot 'candidate-contracts.json'
            $contractLog = Join-Path $trialRoot 'geantyref-contract.log'
            $contractExitCode = Invoke-NativeCommand -Executable $java `
                -Arguments @('-cp', $classpath, 'org.fz.nettyx.serializer.struct.comparison.GenericTypeContractsTest', $geoContract) `
                -LogPath $contractLog
            if ($contractExitCode -ne 0) {
                throw "Contract probe failed for $variantRoot (exit code $contractExitCode). See $contractLog"
            }
        }
    }
    finally {
        Pop-Location
    }
}

if ($Mode -eq 'benchmark') {
    foreach ($variantRoot in @($geoRoot, $guavaRoot)) {
        Push-Location $variantRoot
        try {
            $variant = Split-Path $variantRoot -Leaf
            $benchmarkLog = Join-Path $trialRoot ($variant + '-jmh.log')
            $result = Join-Path $trialRoot ($variant + '-jmh.json')
            $benchmarkClassPath = "target/test-classes;target/classes;" + `
                (Get-Content -LiteralPath (Join-Path $trialRoot ($variant + '-classpath.txt')) -Raw).Trim()
            $benchmarkExitCode = Invoke-NativeCommand -Executable $java `
                -Arguments @('-cp', $benchmarkClassPath, `
                    'org.fz.nettyx.serializer.struct.comparison.RunGenericLibraryBenchmark', $variant, $result) `
                -LogPath $benchmarkLog
            if ($benchmarkExitCode -ne 0) {
                throw "JMH failed for $variant (exit code $benchmarkExitCode). See $benchmarkLog"
            }
        }
        finally {
            Pop-Location
        }
    }
}

[ordered]@{
    output = $trialRoot
    versions = $versions
    contractReport = $contract
    testLogs = @((Join-Path $trialRoot 'geantyref-maven-test.log'), (Join-Path $trialRoot 'guava-maven-test.log'))
    benchmark = ($Mode -eq 'benchmark')
    comparisonScope = 'Field, array-component and struct-type resolution differ; remaining runtime paths share Guava.'
} | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath (Join-Path $trialRoot 'comparison-run.json') -Encoding UTF8
Write-Output $trialRoot
