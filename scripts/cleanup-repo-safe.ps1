# Safe repository cleanup: removes temporary and OS junk files only
Set-StrictMode -Version Latest
$ErrorActionPreference = 'SilentlyContinue'

$Root = (Join-Path $PSScriptRoot "..") | Resolve-Path
Set-Location -LiteralPath $Root

$patterns = @('*.tmp','*~','Thumbs.db','.DS_Store')
$removed = 0
foreach($p in $patterns){
  Get-ChildItem -Recurse -Filter $p -ErrorAction SilentlyContinue | ForEach-Object {
    try { Remove-Item -LiteralPath $_.FullName -Force; $removed++ } catch {}
  }
}

# Optionally clear logs except the autofix log
$logDir = Join-Path $Root 'logs'
if(Test-Path $logDir){
  Get-ChildItem -Path $logDir -File -Recurse | Where-Object { $_.Name -ne 'autofix-min.log' } | ForEach-Object {
    try { Remove-Item -LiteralPath $_.FullName -Force; $removed++ } catch {}
  }
}

Write-Host "Removed items: $removed"
