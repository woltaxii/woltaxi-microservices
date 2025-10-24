<#
Installs and configures Caddy as a reverse proxy on Windows.
- Tries winget, then Chocolatey fallback.
- Places Caddyfile at C:\caddy\Caddyfile (from repo caddy\Caddyfile)
- Creates a Scheduled Task to run Caddy at startup.
Run in elevated PowerShell.
#>
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Assert-Admin {
  $currentUser = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
  if(-not $currentUser.IsInRole([Security.Principal.WindowsBuiltinRole]::Administrator)){
    throw "Please run this script as Administrator."
  }
}

function Try-Install-Caddy {
  Write-Host "Installing Caddy via winget..." -ForegroundColor Cyan
  $winget = Get-Command winget -ErrorAction SilentlyContinue
  if($winget){
    try { winget install --id CaddyServer.Caddy -e --silent; return }
    catch { Write-Warning "winget install failed: $($_.Exception.Message)" }
  }
  $choco = Get-Command choco -ErrorAction SilentlyContinue
  if($choco){
    Write-Host "Installing Caddy via Chocolatey..." -ForegroundColor Cyan
    choco install caddy -y
    return
  }
  throw "Neither winget nor chocolatey available. Install Caddy manually from https://caddyserver.com/download and re-run."
}

function Create-Startup-Task([string]$taskName,[string]$exe,[string]$args,[string]$workDir){
  if(Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue){
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false | Out-Null
  }
  $action = New-ScheduledTaskAction -Execute $exe -Argument $args -WorkingDirectory $workDir
  $trigger = New-ScheduledTaskTrigger -AtStartup
  Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -RunLevel Highest -User $env:USERNAME | Out-Null
  Write-Host "[TASK] Created: $taskName" -ForegroundColor Green
}

Assert-Admin

# Install Caddy
Try-Install-Caddy

# Locate caddy.exe
$gc = Get-Command caddy -ErrorAction SilentlyContinue
$caddyExe = $null
if($gc){ $caddyExe = $gc.Source }
if(-not $caddyExe){ $caddyExe = 'C:\Program Files\caddy\caddy.exe' }
if(-not (Test-Path $caddyExe)){
  throw "caddy.exe not found. Adjust path and retry."
}

# Prepare Caddyfile
$targetDir = 'C:\caddy'
if(-not (Test-Path $targetDir)){ New-Item -ItemType Directory -Path $targetDir | Out-Null }
$repoCaddyfile = (Join-Path $PSScriptRoot '..\caddy\Caddyfile') | Resolve-Path
Copy-Item -Force -Path $repoCaddyfile -Destination (Join-Path $targetDir 'Caddyfile')
Write-Host "Caddyfile installed to $targetDir" -ForegroundColor Green

# Open firewall 80/443
if(-not (Get-NetFirewallRule -DisplayName 'WOLTAXI-HTTP-80' -ErrorAction SilentlyContinue)){
  New-NetFirewallRule -DisplayName 'WOLTAXI-HTTP-80' -Direction Inbound -Protocol TCP -LocalPort 80 -Action Allow | Out-Null
}
if(-not (Get-NetFirewallRule -DisplayName 'WOLTAXI-HTTPS-443' -ErrorAction SilentlyContinue)){
  New-NetFirewallRule -DisplayName 'WOLTAXI-HTTPS-443' -Direction Inbound -Protocol TCP -LocalPort 443 -Action Allow | Out-Null
}

# Create startup task
Create-Startup-Task 'WOLTAXI-Caddy' $caddyExe 'run --config C:\caddy\Caddyfile --adapter caddyfile' 'C:\caddy'

Write-Host "Caddy setup complete. Point DNS A records for woltaxi.com and api.woltaxi.com to this machine's public IP and ensure router forwards 80/443." -ForegroundColor Cyan
