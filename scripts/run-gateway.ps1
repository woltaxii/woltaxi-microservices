param(
  [string]$JavaHome,
  [string]$JarPath = "..\api-gateway\target"
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# Resolve repo root and cd
$RepoRoot = (Join-Path $PSScriptRoot "..") | Resolve-Path
Set-Location -LiteralPath $RepoRoot

# Java setup
if($JavaHome){ $env:JAVA_HOME = $JavaHome }
$javaCmd = if($env:JAVA_HOME){ Join-Path $env:JAVA_HOME 'bin\java.exe' } else { 'java' }

# Disable rate limiter to avoid Redis requirement initially
$env:WOLTAXI_GATEWAY_RATE_LIMIT_ENABLED = 'false'

# Eureka host for gateway discovery
Write-Output "Ensure Eureka is reachable at EUREKA_HOST before starting Gateway."
$env:EUREKA_HOST = $env:EUREKA_HOST -or 'localhost'

# Find jar
$jar = Get-ChildItem -Path $JarPath -Filter '*gateway*.jar' | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if(-not $jar){ Write-Error "Gateway jar not found in $JarPath. Build it first." }

# Run Gateway on 0.0.0.0:8765
$env:SERVER_PORT = '8765'
$env:SERVER_ADDRESS = '0.0.0.0'

& $javaCmd -jar $jar.FullName
