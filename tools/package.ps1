param(
    [switch]$SkipBuild,
    [string]$SdkRoot = $env:ANDROID_HOME,
    [string]$JavaRoot = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $SdkRoot) { $SdkRoot = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
if (-not $JavaRoot) { $JavaRoot = 'C:\Program Files\Android\Android Studio\jbr' }
$env:JAVA_HOME = $JavaRoot
$env:ANDROID_HOME = $SdkRoot

if (-not $SkipBuild) {
    Push-Location $projectRoot
    try {
        & (Join-Path $projectRoot 'gradlew.bat') :app:assembleRelease --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'Release build failed.' }
    } finally { Pop-Location }
}

$unsignedApk = Join-Path $projectRoot 'app\build\outputs\apk\release\app-release-unsigned.apk'
if (-not (Test-Path -LiteralPath $unsignedApk)) { throw 'Build the release APK first.' }
$signingRoot = Join-Path $env:USERPROFILE '.android\moment-signing'
$keyStore = Join-Path $signingRoot 'release.p12'
$credentialsFile = Join-Path $signingRoot 'credentials.json'
New-Item -ItemType Directory -Path $signingRoot -Force | Out-Null

if ((Test-Path -LiteralPath $keyStore) -and (Test-Path -LiteralPath $credentialsFile)) {
    $credentials = Get-Content -Raw -LiteralPath $credentialsFile | ConvertFrom-Json
    $password = $credentials.storePassword
} elseif ((Test-Path -LiteralPath $keyStore) -or (Test-Path -LiteralPath $credentialsFile)) {
    throw 'Signing identity is incomplete. Restore its matching key and credentials rather than replacing it.'
} else {
    $password = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
    $env:MOMENT_SIGNING_PASSWORD = $password
    & (Join-Path $JavaRoot 'bin\keytool.exe') -genkeypair -noprompt -keystore $keyStore -storetype PKCS12 `
        -storepass:env MOMENT_SIGNING_PASSWORD -keypass:env MOMENT_SIGNING_PASSWORD `
        -alias moment -keyalg RSA -keysize 3072 -validity 10000 `
        -dname 'CN=Moment Android, OU=Local Development'
    if ($LASTEXITCODE -ne 0) { throw 'Unable to create local signing identity.' }
    @{ storePassword = $password; alias = 'moment' } | ConvertTo-Json | Set-Content -LiteralPath $credentialsFile -Encoding utf8
}

$artifactsRoot = Join-Path $projectRoot 'artifacts'
New-Item -ItemType Directory -Path $artifactsRoot -Force | Out-Null
$releaseMetadata = Get-Content -Raw -LiteralPath (Join-Path $projectRoot 'app\build\outputs\apk\release\output-metadata.json') | ConvertFrom-Json
$versionName = $releaseMetadata.elements[0].versionName
$signedApk = Join-Path $artifactsRoot ("moment-$versionName.apk")
$env:MOMENT_SIGNING_PASSWORD = $password
try {
    & (Join-Path $SdkRoot 'build-tools\36.0.0\apksigner.bat') sign --ks $keyStore --ks-key-alias moment `
        --ks-pass env:MOMENT_SIGNING_PASSWORD --key-pass env:MOMENT_SIGNING_PASSWORD --out $signedApk $unsignedApk
    if ($LASTEXITCODE -ne 0) { throw 'APK signing failed.' }
    & (Join-Path $SdkRoot 'build-tools\36.0.0\apksigner.bat') verify --verbose --print-certs $signedApk
    if ($LASTEXITCODE -ne 0) { throw 'APK signature verification failed.' }
    & (Join-Path $SdkRoot 'build-tools\36.0.0\zipalign.exe') -c -P 16 4 $signedApk
    if ($LASTEXITCODE -ne 0) { throw 'APK alignment verification failed.' }
    (Get-FileHash -LiteralPath $signedApk -Algorithm SHA256).Hash | Set-Content -LiteralPath ($signedApk + '.sha256')
    Write-Output "Signed APK: $signedApk"
    Write-Output "Local signing identity: $signingRoot"
} finally {
    Remove-Item Env:MOMENT_SIGNING_PASSWORD -ErrorAction SilentlyContinue
}
