# Move legacy autofix scripts into scripts/archive to keep repo lean
Set-StrictMode -Version Latest
$ErrorActionPreference = 'SilentlyContinue'

$Root = (Join-Path $PSScriptRoot "..") | Resolve-Path
$Scripts = (Join-Path $Root 'scripts')
$Archive = (Join-Path $Scripts 'archive')
if(-not (Test-Path $Archive)){ New-Item -ItemType Directory -Path $Archive | Out-Null }

$files = @(
  'auto-fix.ps1',
  'auto-fix-clean.ps1',
  'woltaxi-autofix.ps1',
  'woltaxi-autofix-final.ps1',
  'simple-autofix.ps1',
  'setup-powershell-autofix.ps1',
  'setup-autofix-system.ps1'
)

foreach($f in $files){
  $src = Join-Path $Scripts $f
  if(Test-Path $src){
    try { Move-Item -LiteralPath $src -Destination $Archive -Force } catch {}
  }
}

Write-Host "Legacy autofix scripts archived under scripts/archive" -ForegroundColor Green
