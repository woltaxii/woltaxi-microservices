<#
Builds Eureka and API Gateway using portable JDK/Maven from .tools (no admin).
Run setup-portable-java-maven.ps1 first.
#>
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$Root = (Join-Path $PSScriptRoot "..") | Resolve-Path
Set-Location -LiteralPath $Root
$Tools = Join-Path $Root ".tools"

$JAVA_HOME = (Get-Content -Raw (Join-Path $Tools 'JAVA_HOME.txt'))
$MAVEN_HOME = (Get-Content -Raw (Join-Path $Tools 'MAVEN_HOME.txt'))

if(-not (Test-Path $JAVA_HOME)){ throw "JAVA_HOME not found: $JAVA_HOME" }
if(-not (Test-Path $MAVEN_HOME)){ throw "MAVEN_HOME not found: $MAVEN_HOME" }

$env:JAVA_HOME = $JAVA_HOME
$env:Path = (Join-Path $MAVEN_HOME 'bin') + ";" + $env:Path

function Run-Maven([string]$path){
  Write-Host "[BUILD] $path" -ForegroundColor Cyan
  Push-Location $path
  try {
    & mvn -version
    & mvn -q -DskipTests package
  } finally { Pop-Location }
}

Run-Maven (Join-Path $Root 'eureka-server')
Run-Maven (Join-Path $Root 'api-gateway')

Write-Host "Build complete. JARs should be under target/ folders." -ForegroundColor Green
