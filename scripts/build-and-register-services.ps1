<#
Builds Eureka and API Gateway, opens Windows Firewall, and creates
Scheduled Tasks to run both at startup. It does NOT install system packages.
Run in an elevated PowerShell (Run as Administrator).
#>
param(
  [string]$JavaHome,
  [switch]$Clean = $false
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Assert-Admin {
  $currentUser = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
  if(-not $currentUser.IsInRole([Security.Principal.WindowsBuiltinRole]::Administrator)){
    throw "Please run this script in an elevated PowerShell (Run as Administrator)."
  }
}

function Run-Maven([string]$path){
  Write-Host "[BUILD] $path" -ForegroundColor Cyan
  Push-Location $path
  try {
    $cmd = 'mvn'
    if($Clean){ & $cmd clean package -DskipTests } else { & $cmd package -DskipTests }
  } finally {
    Pop-Location
  }
}

function Open-Firewall([string]$name,[int]$port){
  if(-not (Get-NetFirewallRule -DisplayName $name -ErrorAction SilentlyContinue)){
    New-NetFirewallRule -DisplayName $name -Direction Inbound -LocalPort $port -Protocol TCP -Action Allow | Out-Null
    Write-Host "[FIREWALL] Opened TCP $port ($name)" -ForegroundColor Green
  } else {
    Write-Host "[FIREWALL] Rule exists: $name" -ForegroundColor Yellow
  }
}

function Create-Startup-Task([string]$taskName,[string]$scriptPath){
  $ps = "powershell.exe -NoProfile -ExecutionPolicy Bypass -File `"$scriptPath`""
  if(Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue){
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false | Out-Null
  }
  $action = New-ScheduledTaskAction -Execute 'powershell.exe' -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$scriptPath`""
  $trigger = New-ScheduledTaskTrigger -AtStartup
  Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -RunLevel Highest -User $env:USERNAME | Out-Null
  Write-Host "[TASK] Created: $taskName -> $scriptPath" -ForegroundColor Green
}

# Main
Assert-Admin
$Root = (Join-Path $PSScriptRoot "..") | Resolve-Path

# Build services
Run-Maven (Join-Path $Root 'eureka-server')
Run-Maven (Join-Path $Root 'api-gateway')

# Open firewall
Open-Firewall 'WOLTAXI-HTTP-80' 80
Open-Firewall 'WOLTAXI-HTTPS-443' 443
Open-Firewall 'WOLTAXI-Eureka-8761' 8761
Open-Firewall 'WOLTAXI-Gateway-8765' 8765

# Create startup tasks
$EurekaScript = (Join-Path $PSScriptRoot 'run-eureka.ps1')
$GatewayScript = (Join-Path $PSScriptRoot 'run-gateway.ps1')
Create-Startup-Task 'WOLTAXI-Eureka' $EurekaScript
Create-Startup-Task 'WOLTAXI-API-Gateway' $GatewayScript

Write-Host "All done. Ensure DNS and Caddy are configured for HTTPS." -ForegroundColor Cyan
