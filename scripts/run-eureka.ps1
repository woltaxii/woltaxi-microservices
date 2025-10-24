param(
  [string]$JavaHome,
  [string]$JarPath = "..\eureka-server\target"
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# Resolve repo root and cd
$RepoRoot = (Join-Path $PSScriptRoot "..") | Resolve-Path
Set-Location -LiteralPath $RepoRoot

# Java setup
if($JavaHome){ $env:JAVA_HOME = $JavaHome }
$javaCmd = if($env:JAVA_HOME){ Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }

# Find jar
$jar = Get-ChildItem -Path $JarPath -Filter '*eureka*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if(-not $jar){ Write-Error "Eureka jar not found in $JarPath. Build it first." }

# Run Eureka on 0.0.0.0:8761
$env:SERVER_PORT = '8761'
$env:SERVER_ADDRESS = '0.0.0.0'

& $javaCmd -jar $jar.FullName
