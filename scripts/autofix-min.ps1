# WOLTAXI Auto-Fix (Minimal, ASCII-only) with logging
param(
  [switch]$Continuous = $false,
  [int]$IntervalSeconds = 120,
  [switch]$AllowRenames = $false
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'SilentlyContinue'

# Ensure we operate from repo root
$RepoRoot = (Join-Path $PSScriptRoot "..") | Resolve-Path -ErrorAction SilentlyContinue
try { if($RepoRoot){ Set-Location -LiteralPath $RepoRoot } } catch {}

# Logging setup
$LogDir = Join-Path $PSScriptRoot "..\logs"
try { if(-not (Test-Path $LogDir)){ New-Item -ItemType Directory -Path $LogDir -Force | Out-Null } } catch {}
$LogFile = Join-Path $LogDir "autofix-min.log"
function Write-Log([string]$level,[string]$msg){
  $ts = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
  $line = "[$ts] [$level] $msg"
  try { Add-Content -LiteralPath $LogFile -Value $line -Encoding UTF8 } catch {}
}
function Write-Info([string]$m){ Write-Log 'INFO' $m; Write-Host "[INFO] $m" }
function Write-Ok([string]$m){ Write-Log 'OK' $m; Write-Host "[OK]   $m" -ForegroundColor Green }
function Write-Warn([string]$m){ Write-Log 'WARN' $m; Write-Host "[WARN] $m" -ForegroundColor Yellow }
function Write-Err([string]$m){ Write-Log 'ERR' $m; Write-Host "[ERR]  $m" -ForegroundColor Red }

function Convert-ToAsciiName([string]$name){
  $s = $name
  # Specific Turkish letters only
  $s = $s -replace 'Ç','C'
  $s = $s -replace 'Ğ','G'
  $s = $s -replace 'İ','I'
  $s = $s -replace 'Ö','O'
  $s = $s -replace 'Ş','S'
  $s = $s -replace 'Ü','U'
  $s = $s -replace 'ç','c'
  $s = $s -replace 'ğ','g'
  $s = $s -replace 'ı','i'
  $s = $s -replace 'ö','o'
  $s = $s -replace 'ş','s'
  $s = $s -replace 'ü','u'
  # remove emojis and non-ascii
  $s = [System.Text.RegularExpressions.Regex]::Replace($s, "[^\u0000-\u007F]", "")
  # remove '#', collapse whitespace
  $s = $s -replace '#',''
  $s = $s -replace "\s+","-"
  # only allow [A-Za-z0-9_.-]
  $s = $s -replace "[^A-Za-z0-9_.-]",""
  $s.Trim('-')
}

function Fix-Filenames{
  Write-Info "Scanning for problematic file names (safe mode)"
  $fixed = $false
  Get-ChildItem -Recurse -File | ForEach-Object {
    $new = Convert-ToAsciiName $_.Name
    if([string]::IsNullOrWhiteSpace($new)){ return }
    if($new -ne $_.Name){
      try {
        $target = Join-Path $_.DirectoryName $new
        Move-Item -LiteralPath $_.FullName -Destination $target -Force
        Write-Ok "Renamed: '$($_.Name)' -> '$new'"
        $fixed = $true
      } catch {
        Write-Warn "Rename failed: $($_.FullName)"
      }
    }
  }
  return $fixed
}

function Clean-Temp{
  Write-Info "Cleaning temp files"
  $fixed = $false
  foreach($p in @('*.tmp','*~','Thumbs.db','.DS_Store')){
    Get-ChildItem -Recurse -Filter $p -ErrorAction SilentlyContinue | ForEach-Object {
      try { Remove-Item -LiteralPath $_.FullName -Force; $fixed=$true; Write-Ok "Removed: $($_.Name)" } catch {}
    }
  }
  return $fixed
}

function Update-Gitignore{
  $path = Join-Path $PSScriptRoot "..\.gitignore" | Resolve-Path -ErrorAction SilentlyContinue
  if(-not $path){ $path = ".gitignore" }
  $entries = @('node_modules/','dist/','*.tmp','*~','Thumbs.db','.DS_Store','*.zip','.env')
  try{
    if(Test-Path $path){ $cur = Get-Content $path } else { $cur = @() }
    $add = @()
    foreach($e in $entries){ if($cur -notcontains $e){ $add += $e } }
    if($add.Count -gt 0){ Add-Content -Path $path -Value ($add -join "`n"); Write-Ok ".gitignore updated"; return $true }
  }catch{ Write-Warn ".gitignore update skipped" }
  return $false
}

function Git-Autocommit{
  try{
    $status = git status --porcelain 2>$null
    if($status){
      git add . 2>$null | Out-Null
      $msg = "Auto-Fix: repository maintenance - " + (Get-Date -Format 'yyyy-MM-dd HH:mm')
      git commit -m $msg 2>$null | Out-Null
      Write-Info "Committed message: $msg"
      Write-Ok "Committed changes"
      return $true
    }
  }catch{ Write-Warn "Git not available" }
  return $false
}

function Run-Cycle{
  Write-Host "================ Auto-Fix ================="
  Write-Log 'INFO' 'Cycle start'
  $changed = $false
  if($AllowRenames){ if(Fix-Filenames){ $changed = $true } }
  if(Clean-Temp){ $changed = $true }
  if(Update-Gitignore){ $changed = $true }
  if($changed){ Git-Autocommit | Out-Null } else { Write-Info "No changes" }
  Write-Log 'INFO' 'Cycle end'
}

if($Continuous){
  while($true){ Run-Cycle; Start-Sleep -Seconds $IntervalSeconds }
} else {
  Run-Cycle
}
