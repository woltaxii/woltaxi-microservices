<#
Downloads and sets up portable Temurin JDK 21 and Apache Maven under .tools (no admin required).
After running, use build-services-portable.ps1 to build.
#>
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$Root = (Join-Path $PSScriptRoot "..") | Resolve-Path
Set-Location -LiteralPath $Root
$Tools = Join-Path $Root ".tools"
if(-not (Test-Path $Tools)){ New-Item -ItemType Directory -Path $Tools | Out-Null }

# URLs (may change over time). Adjust if 404.
$jdkZip = Join-Path $Tools "temurin-jdk-21.zip"
$mvnZip = Join-Path $Tools "apache-maven.zip"

$jdkUrl = "https://github.com/adoptium/temurin21-binaries/releases/latest/download/OpenJDK21U-jdk_x64_windows_hotspot.zip"
$mvnUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"

Write-Host "Downloading JDK 21..." -ForegroundColor Cyan
Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip -UseBasicParsing

Write-Host "Downloading Maven..." -ForegroundColor Cyan
Invoke-WebRequest -Uri $mvnUrl -OutFile $mvnZip -UseBasicParsing

Write-Host "Extracting JDK..." -ForegroundColor Cyan
Expand-Archive -Path $jdkZip -DestinationPath $Tools -Force

Write-Host "Extracting Maven..." -ForegroundColor Cyan
Expand-Archive -Path $mvnZip -DestinationPath $Tools -Force

# Detect extracted directories
$jdkDir = Get-ChildItem $Tools -Directory | Where-Object { $_.Name -match 'jdk-21|jdk-21.*' -or $_.Name -match 'jdk-21.*hotspot' } | Select-Object -First 1
if(-not $jdkDir){
  $jdkDir = Get-ChildItem $Tools -Directory | Where-Object { $_.Name -match 'jdk' } | Select-Object -First 1
}
$mvnDir = Get-ChildItem $Tools -Directory | Where-Object { $_.Name -match 'apache-maven-3.9' } | Select-Object -First 1

if(-not $jdkDir -or -not $mvnDir){ throw "Portable JDK or Maven not found after extraction." }

# Create env hint files
Set-Content -Path (Join-Path $Tools 'JAVA_HOME.txt') -Value $jdkDir.FullName
Set-Content -Path (Join-Path $Tools 'MAVEN_HOME.txt') -Value $mvnDir.FullName

Write-Host "Portable tools installed in: $Tools" -ForegroundColor Green
Write-Host "JAVA_HOME -> $($jdkDir.FullName)" -ForegroundColor Green
Write-Host "MAVEN_HOME -> $($mvnDir.FullName)" -ForegroundColor Green
