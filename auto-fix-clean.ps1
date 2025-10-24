# 🤖 WOLTAXI Auto-Fix PowerShell System - CLEAN VERSION
# Sürekli çalışan akıllı hata düzeltme sistemi

param(
    [switch]$Continuous = $false,
    [int]$IntervalSeconds = 30,
    [switch]$AutoCommit = $false,
    [switch]$Verbose = $false
)

# Renkli output fonksiyonları
function Write-Success { param($Message) Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Error { param($Message) Write-Host "❌ $Message" -ForegroundColor Red }
function Write-Info { param($Message) Write-Host "ℹ️ $Message" -ForegroundColor Cyan }
function Write-Warning { param($Message) Write-Host "⚠️ $Message" -ForegroundColor Yellow }
function Write-Fix { param($Message) Write-Host "🔧 $Message" -ForegroundColor Magenta }

# Ana hata kategorileri ve otomatik düzeltmeler
$ErrorFixers = @{
    "InvalidFilename" = {
        param($ErrorDetails)
        Write-Fix "Invalid filename hatası düzeltiliyor..."
        
        # Problematik dosyaları bul (emoji ve özel karakterler)
        $ProblematicFiles = Get-ChildItem -Recurse | Where-Object { 
            $_.Name -match "[\u{1F680}\u{1F4CB}\u{1F3AF}]|[#⚡💪🌟🔥]|MASAÜSTÜ|YEDEk"
        }
        
        $Fixed = $false
        foreach ($File in $ProblematicFiles) {
            $SafeName = $File.Name -replace "[\u{1F680}\u{1F4CB}\u{1F3AF}]", ""
            $SafeName = $SafeName -replace "[#⚡💪🌟🔥]", ""
            $SafeName = $SafeName -replace "MASAÜSTÜ", "DESKTOP"
            $SafeName = $SafeName -replace "YEDEK", "BACKUP"
            $SafeName = $SafeName -replace "\s+", "-"
            $SafeName = $SafeName -replace "[^\w\-\.]", ""
            $SafeName = $SafeName.Trim("-").Trim(".")
            
            if ($SafeName -ne $File.Name -and $SafeName -ne "") {
                Write-Fix "Renaming: $($File.Name) → $SafeName"
                try {
                    Rename-Item $File.FullName $SafeName -ErrorAction Stop
                    $Fixed = $true
                } catch {
                    Write-Warning "Rename failed: $_"
                }
            }
        }
        return $Fixed
    }
    
    "PowerShellSyntax" = {
        param($ErrorDetails)
        Write-Fix "PowerShell syntax hatalarını düzeltiliyor..."
        
        $PSFiles = Get-ChildItem -Recurse -Filter "*.ps1"
        $Fixed = $false
        
        foreach ($PSFile in $PSFiles) {
            try {
                $Content = Get-Content $PSFile.FullName -Raw -Encoding UTF8
                $OriginalContent = $Content
                
                # Yaygın PowerShell hatalarını düzelt
                $Content = $Content -replace 'iex\s+', 'Invoke-Expression '
                $Content = $Content -replace '\[string\]\$Password', '[SecureString]$Password'
                $Content = $Content -replace 'Write-Host\s+"[^"]*\$(\w+):', 'Write-Host "Text $($1):"'
                
                if ($Content -ne $OriginalContent) {
                    Set-Content $PSFile.FullName $Content -Encoding UTF8
                    Write-Success "Fixed: $($PSFile.Name)"
                    $Fixed = $true
                }
            } catch {
                Write-Warning "Error processing $($PSFile.Name): $_"
            }
        }
        return $Fixed
    }
    
    "GitIssues" = {
        param($ErrorDetails)
        Write-Fix "Git issues düzeltiliyor..."
        
        try {
            # Git status kontrolü
            $GitStatus = git status --porcelain 2>$null
            
            if ($GitStatus) {
                # Untracked dosyaları ekle
                git add . 2>$null | Out-Null
                Write-Success "Git files staged"
                return $true
            }
            
            return $false
        } catch {
            Write-Warning "Git fix failed: $_"
            return $false
        }
    }
    
    "FileCleanup" = {
        param($ErrorDetails)
        Write-Fix "File cleanup yapılıyor..."
        
        $CleanupPatterns = @(
            "*.tmp",
            "*.log",
            "*~",
            "Thumbs.db",
            ".DS_Store"
        )
        
        $Cleaned = $false
        foreach ($Pattern in $CleanupPatterns) {
            $FilesToClean = Get-ChildItem -Recurse -Filter $Pattern -ErrorAction SilentlyContinue
            foreach ($File in $FilesToClean) {
                try {
                    Remove-Item $File.FullName -Force
                    Write-Success "Cleaned: $($File.Name)"
                    $Cleaned = $true
                } catch {
                    Write-Warning "Cleanup failed for $($File.Name): $_"
                }
            }
        }
        return $Cleaned
    }
}

# Hata tespit ve çözme motoru
function Invoke-AutoFix {
    param([string]$ProjectPath = ".")
    
    Write-Info "🔍 Auto-Fix scan başlatılıyor: $ProjectPath"
    
    $FixedIssues = 0
    $TotalChecks = 0
    
    # Her hata türü için kontrol yap
    foreach ($ErrorType in $ErrorFixers.Keys) {
        Write-Info "Checking: $ErrorType"
        
        try {
            $TotalChecks++
            $Fixed = & $ErrorFixers[$ErrorType] @()
            if ($Fixed) {
                $FixedIssues++
                Write-Success "$ErrorType fixed!"
            }
        } catch {
            Write-Error "Fix failed for $ErrorType : $_"
        }
    }
    
    Write-Info "📊 Auto-Fix Report:"
    Write-Info "  Total Checks: $TotalChecks"
    Write-Info "  Fixed Issues: $FixedIssues"
    
    return @{
        Fixed = $FixedIssues
        Total = $TotalChecks
        Success = ($FixedIssues -gt 0)
    }
}

# Git auto-commit feature
function Invoke-AutoCommit {
    param($FixResults)
    
    if ($FixResults.Fixed -gt 0) {
        Write-Info "🚀 Auto-committing fixes..."
        
        try {
            git add . 2>$null | Out-Null
            $CommitMessage = "🤖 Auto-Fix: Resolved $($FixResults.Fixed) issues automatically"
            git commit -m $CommitMessage 2>$null | Out-Null
            
            Write-Success "Auto-committed fixes!"
            
            return $true
        } catch {
            Write-Warning "Auto-commit failed: $_"
            return $false
        }
    }
    return $false
}

# Sürekli izleme modu
function Start-ContinuousMonitoring {
    param($IntervalSeconds)
    
    Write-Info "🔄 Continuous monitoring started (interval: $IntervalSeconds seconds)"
    Write-Info "Press Ctrl+C to stop"
    
    $CycleCount = 0
    
    while ($true) {
        try {
            $CycleCount++
            Clear-Host
            Write-Info "🤖 WOLTAXI Auto-Fix Cycle #$CycleCount - $(Get-Date -Format 'HH:mm:ss')"
            Write-Info "=" * 60
            
            $Results = Invoke-AutoFix
            
            if ($AutoCommit -and $Results.Success) {
                Invoke-AutoCommit $Results
            }
            
            Write-Info "=" * 60
            Write-Info "💤 Next scan in $IntervalSeconds seconds..."
            Start-Sleep -Seconds $IntervalSeconds
            
        } catch {
            Write-Error "Monitoring error: $_"
            Start-Sleep -Seconds 10
        }
    }
}

# Ana execution logic
try {
    Write-Info "🤖 WOLTAXI Intelligent Auto-Fix System"
    Write-Info "=" * 50
    
    if ($Continuous) {
        Start-ContinuousMonitoring $IntervalSeconds
    } else {
        $Results = Invoke-AutoFix
        
        if ($AutoCommit -and $Results.Success) {
            Invoke-AutoCommit $Results
        }
        
        Write-Success "🎉 Auto-Fix completed!"
        
        if ($Results.Fixed -eq 0) {
            Write-Success "🌟 No issues found! Your code is clean!"
        } else {
            Write-Info "✨ Fixed $($Results.Fixed) issues automatically!"
        }
    }
    
} catch {
    Write-Error "Auto-Fix system error: $_"
    exit 1
}

# Usage info
if (!$Continuous) {
    Write-Info ""
    Write-Info "💡 Usage Examples:"
    Write-Info "  Single scan:      .\auto-fix-clean.ps1"
    Write-Info "  Auto-commit:      .\auto-fix-clean.ps1 -AutoCommit"
    Write-Info "  Continuous:       .\auto-fix-clean.ps1 -Continuous"
    Write-Info "  Custom interval:  .\auto-fix-clean.ps1 -Continuous -IntervalSeconds 60"
}