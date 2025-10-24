# 🔧 WOLTAXI PowerShell Setup & Monitor
# Otomatik kurulum ve sürekli hata düzeltme sistemi

# Self-Elevating Admin Privileges
if (-NOT ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "🔒 Admin privileges required. Restarting as administrator..." -ForegroundColor Yellow
    Start-Process PowerShell -Verb RunAs "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
    exit
}

Write-Host "🤖 WOLTAXI PowerShell Auto-Setup & Monitor" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

# PowerShell execution policy setup
function Set-PowerShellPolicy {
    Write-Host "🔧 Setting PowerShell execution policy..." -ForegroundColor Yellow
    try {
        Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
        Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope LocalMachine -Force
        Write-Host "✅ PowerShell execution policy configured!" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ Policy setting warning: $_" -ForegroundColor Yellow
    }
}

# Git global configuration
function Initialize-GitConfig {
    Write-Host "🔧 Configuring Git for WOLTAXI..." -ForegroundColor Yellow
    try {
        git config --global user.name "WOLTAXI Developer"
        git config --global user.email "dev@woltaxi.com"
        git config --global init.defaultBranch main
        git config --global pull.rebase false
        git config --global core.autocrlf true
        Write-Host "✅ Git configuration completed!" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ Git config warning: $_" -ForegroundColor Yellow
    }
}

# Auto-Fix scheduler setup
function Install-AutoFixScheduler {
    Write-Host "🔧 Installing Auto-Fix scheduler..." -ForegroundColor Yellow
    
    $TaskName = "WOLTAXI-AutoFix"
    $ScriptPath = Join-Path $PWD "auto-fix.ps1"
    
    # Scheduled task oluştur
    $Action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$ScriptPath`" -Continuous -IntervalSeconds 300 -AutoCommit"
    $Trigger = New-ScheduledTaskTrigger -AtStartup
    $Settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable
    $Principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest
    
    try {
        Register-ScheduledTask -TaskName $TaskName -Action $Action -Trigger $Trigger -Settings $Settings -Principal $Principal -Force
        Write-Host "✅ Auto-Fix scheduler installed!" -ForegroundColor Green
        Write-Host "   Task Name: $TaskName" -ForegroundColor Cyan
        Write-Host "   Interval: 5 minutes" -ForegroundColor Cyan
    } catch {
        Write-Host "⚠️ Scheduler installation warning: $_" -ForegroundColor Yellow
    }
}

# PowerShell modules installation
function Install-RequiredModules {
    Write-Host "🔧 Installing required PowerShell modules..." -ForegroundColor Yellow
    
    $RequiredModules = @(
        "PowerShellGet",
        "PSScriptAnalyzer",
        "PSDiagnostics"
    )
    
    foreach ($Module in $RequiredModules) {
        try {
            if (!(Get-Module -ListAvailable -Name $Module)) {
                Write-Host "Installing $Module..." -ForegroundColor Cyan
                Install-Module -Name $Module -Force -AllowClobber -Scope CurrentUser
            }
            Write-Host "✅ $Module ready!" -ForegroundColor Green
        } catch {
            Write-Host "⚠️ $Module installation warning: $_" -ForegroundColor Yellow
        }
    }
}

# VSCode integration setup
function Setup-VSCodeIntegration {
    Write-Host "🔧 Setting up VSCode integration..." -ForegroundColor Yellow
    
    $VSCodeSettings = @{
        "powershell.scriptAnalysis.enable" = $true
        "powershell.integratedConsole.showOnStartup" = $false
        "terminal.integrated.defaultProfile.windows" = "PowerShell"
        "files.autoSave" = "afterDelay"
        "files.autoSaveDelay" = 1000
        "git.autofetch" = $true
        "git.enableSmartCommit" = $true
    }
    
    $SettingsPath = "$env:APPDATA\Code\User\settings.json"
    
    try {
        if (Test-Path $SettingsPath) {
            $CurrentSettings = Get-Content $SettingsPath | ConvertFrom-Json
            foreach ($Key in $VSCodeSettings.Keys) {
                $CurrentSettings | Add-Member -Name $Key -Value $VSCodeSettings[$Key] -Force
            }
            $CurrentSettings | ConvertTo-Json -Depth 10 | Set-Content $SettingsPath
        } else {
            $VSCodeSettings | ConvertTo-Json -Depth 10 | Set-Content $SettingsPath
        }
        Write-Host "✅ VSCode integration configured!" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ VSCode config warning: $_" -ForegroundColor Yellow
    }
}

# Continuous monitoring function
function Start-ContinuousAutoFix {
    Write-Host "🚀 Starting continuous Auto-Fix monitoring..." -ForegroundColor Green
    
    $LogFile = "auto-fix-log.txt"
    
    while ($true) {
        try {
            $Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            Write-Host "[$Timestamp] 🔍 Running Auto-Fix scan..." -ForegroundColor Cyan
            
            # Auto-fix script'i çalıştır
            $AutoFixPath = Join-Path $PWD "auto-fix.ps1"
            if (Test-Path $AutoFixPath) {
                & $AutoFixPath -AutoCommit
                "[$Timestamp] Auto-Fix completed successfully" | Add-Content $LogFile
            } else {
                Write-Host "❌ Auto-fix script not found!" -ForegroundColor Red
                "[$Timestamp] ERROR: Auto-fix script not found" | Add-Content $LogFile
            }
            
            # 5 dakika bekle
            Write-Host "💤 Waiting 5 minutes for next scan..." -ForegroundColor Yellow
            Start-Sleep -Seconds 300
            
        } catch {
            $ErrorMsg = "[$Timestamp] ERROR: $_"
            Write-Host $ErrorMsg -ForegroundColor Red
            $ErrorMsg | Add-Content $LogFile
            Start-Sleep -Seconds 60  # Hata durumunda 1 dakika bekle
        }
    }
}

# Ana kurulum işlemi
try {
    Write-Host "🚀 WOLTAXI PowerShell Environment Setup Starting..." -ForegroundColor Green
    Write-Host ""
    
    # Adım adım kurulum
    Set-PowerShellPolicy
    Initialize-GitConfig
    Install-RequiredModules
    Setup-VSCodeIntegration
    Install-AutoFixScheduler
    
    Write-Host ""
    Write-Host "🎉 WOLTAXI PowerShell Environment Ready!" -ForegroundColor Green
    Write-Host "=" * 50 -ForegroundColor Green
    
    # Kullanıcıya seçenekler sun
    Write-Host ""
    Write-Host "Select operation mode:" -ForegroundColor Cyan
    Write-Host "1. Run single Auto-Fix scan" -ForegroundColor White
    Write-Host "2. Start continuous monitoring (manual)" -ForegroundColor White
    Write-Host "3. Exit (scheduled task will handle auto-fix)" -ForegroundColor White
    Write-Host ""
    
    $Choice = Read-Host "Enter choice (1-3)"
    
    switch ($Choice) {
        "1" {
            Write-Host "🔧 Running single Auto-Fix scan..." -ForegroundColor Yellow
            $AutoFixPath = Join-Path $PWD "auto-fix.ps1"
            if (Test-Path $AutoFixPath) {
                & $AutoFixPath -AutoCommit -Verbose
            } else {
                Write-Host "❌ Auto-fix script not found!" -ForegroundColor Red
            }
        }
        "2" {
            Start-ContinuousAutoFix
        }
        "3" {
            Write-Host "✅ Setup complete! Scheduled task will handle auto-fixing." -ForegroundColor Green
            Write-Host "   Use 'Get-ScheduledTask WOLTAXI-AutoFix' to check status." -ForegroundColor Cyan
        }
        default {
            Write-Host "❌ Invalid choice. Exiting..." -ForegroundColor Red
        }
    }
    
} catch {
    Write-Host "❌ Setup failed: $_" -ForegroundColor Red
    Write-Host "Please run this script as Administrator." -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "🌟 WOLTAXI PowerShell Auto-Fix System is now active!" -ForegroundColor Green
Read-Host "Press Enter to exit"