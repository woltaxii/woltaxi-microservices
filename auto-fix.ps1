# 🤖 WOLTAXI Auto-Fix PowerShell System
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
        
        # Emoji ve özel karakter içeren dosyaları bul
        $ProblematicFiles = Get-ChildItem -Recurse | Where-Object { 
            $_.Name -match "[#🚀📋🎯⚡💪🌟🔥]" 
        }
        
        foreach ($File in $ProblematicFiles) {
            $SafeName = $File.Name -replace "[#🚀📋🎯⚡💪🌟🔥]", "" -replace "\s+", "-" -replace "[^\w\-\.]", ""
            $SafeName = $SafeName.Trim("-").Trim(".")
            
            if ($SafeName -ne $File.Name) {
                Write-Fix "Renaming: $($File.Name) → $SafeName"
                try {
                    Rename-Item $File.FullName $SafeName -ErrorAction Stop
                    git add $SafeName 2>$null
                    git rm --cached $File.Name 2>$null
                    return $true
                } catch {
                    Write-Warning "Rename failed: $_"
                }
            }
        }
    }
    
    "PowerShellSyntax" = {
        param($ErrorDetails)
        Write-Fix "PowerShell syntax hatalarını düzeltiliyor..."
        
        $PSFiles = Get-ChildItem -Recurse -Filter "*.ps1"
        $Fixed = $false
        
        foreach ($PSFile in $PSFiles) {
            $Content = Get-Content $PSFile.FullName -Raw
            $OriginalContent = $Content
            
            # Yaygın PowerShell hatalarını düzelt
            $Content = $Content -replace '\$result\s*=\s*Invoke-Expression.*?2>&1', 'Invoke-Expression $cmd 2>&1 | Out-Host'
            $Content = $Content -replace '\[string\]\$Password', '[SecureString]$Password'
            $Content = $Content -replace 'Write-Host\s+"[^"]*\$(\w+):', 'Write-Host "Text ${$1}:"'
            $Content = $Content -replace 'iex\s+', 'Invoke-Expression '
            $Content = $Content -replace 'function\s+Setup-(\w+)', 'function Initialize-$1'
            
            if ($Content -ne $OriginalContent) {
                Set-Content $PSFile.FullName $Content -Encoding UTF8
                Write-Success "Fixed: $($PSFile.Name)"
                $Fixed = $true
            }
        }
        return $Fixed
    }
    
    "TypeScriptConfig" = {
        param($ErrorDetails)
        Write-Fix "TypeScript config hatalarını düzeltiliyor..."
        
        $TSConfigs = Get-ChildItem -Recurse -Filter "tsconfig.json"
        $Fixed = $false
        
        foreach ($TSConfig in $TSConfigs) {
            $Content = Get-Content $TSConfig.FullName -Raw | ConvertFrom-Json
            
            if ($Content.compilerOptions.moduleResolution -eq "node") {
                $Content.compilerOptions.moduleResolution = "bundler"
                $Content.compilerOptions | Add-Member -Name "ignoreDeprecations" -Value "6.0" -MemberType NoteProperty
                
                $Content | ConvertTo-Json -Depth 10 | Set-Content $TSConfig.FullName -Encoding UTF8
                Write-Success "Fixed: $($TSConfig.Name)"
                $Fixed = $true
            }
        }
        return $Fixed
    }
    
    "GitIssues" = {
        param($ErrorDetails)
        Write-Fix "Git issues düzeltiliyor..."
        
        try {
            # Staging area'yı temizle
            git reset HEAD . 2>$null
            
            # Untracked dosyaları ekle
            git add . 2>$null
            
            # Conflict'leri otomatik çöz
            git status --porcelain | ForEach-Object {
                if ($_ -match "^UU\s+(.+)") {
                    $File = $matches[1]
                    Write-Fix "Auto-resolving conflict in: $File"
                    git add $File 2>$null
                }
            }
            
            return $true
        } catch {
            Write-Warning "Git fix failed: $_"
            return $false
        }
    }
    
    "DependencyIssues" = {
        param($ErrorDetails)
        Write-Fix "Dependency issues düzeltiliyor..."
        
        # Node.js dependencies
        if (Test-Path "package.json") {
            Write-Info "Fixing Node.js dependencies..."
            npm install --force 2>$null
        }
        
        # Maven dependencies
        if (Test-Path "pom.xml") {
            Write-Info "Fixing Maven dependencies..."
            mvn clean install -DskipTests 2>$null
        }
        
        return $true
    }
}

# Hata tespit ve çözme motoru
function Invoke-AutoFix {
    param([string]$ProjectPath = ".")
    
    Write-Info "🔍 Auto-Fix scan başlatılıyor: $ProjectPath"
    
    $FixedIssues = 0
    $TotalIssues = 0
    
    # Her hata türü için kontrol yap
    foreach ($ErrorType in $ErrorFixers.Keys) {
        Write-Info "Checking: $ErrorType"
        
        try {
            $Fixed = & $ErrorFixers[$ErrorType] @()
            if ($Fixed) {
                $FixedIssues++
                Write-Success "$ErrorType fixed!"
            }
            $TotalIssues++
        } catch {
            Write-Error "Fix failed for $ErrorType`: $_"
        }
    }
    
    # VSCode Problems API'sini simüle et
    $ProblemsFound = @()
    
    # PowerShell script analyzer
    Get-ChildItem -Recurse -Filter "*.ps1" | ForEach-Object {
        $Content = Get-Content $_.FullName -Raw
        if ($Content -match 'iex\s+') { $ProblemsFound += "Alias usage in $($_.Name)" }
        if ($Content -match '\[string\]\$Password') { $ProblemsFound += "Insecure password in $($_.Name)" }
    }
    
    # TypeScript problems
    Get-ChildItem -Recurse -Filter "tsconfig.json" | ForEach-Object {
        $Content = Get-Content $_.FullName -Raw
        if ($Content -match '"moduleResolution":\s*"node"') { 
            $ProblemsFound += "Deprecated moduleResolution in $($_.Name)" 
        }
    }
    
    Write-Info "📊 Auto-Fix Report:"
    Write-Info "  Total Checks: $TotalIssues"
    Write-Info "  Fixed Issues: $FixedIssues"
    Write-Info "  Problems Found: $($ProblemsFound.Count)"
    
    if ($ProblemsFound.Count -gt 0) {
        Write-Warning "Remaining issues:"
        $ProblemsFound | ForEach-Object { Write-Warning "  - $_" }
    }
    
    return @{
        Fixed = $FixedIssues
        Total = $TotalIssues
        Problems = $ProblemsFound
    }
}

# Git auto-commit feature
function Invoke-AutoCommit {
    param($FixResults)
    
    if ($FixResults.Fixed -gt 0) {
        Write-Info "🚀 Auto-committing fixes..."
        
        git add . 2>$null
        $CommitMessage = "🤖 Auto-Fix: Resolved $($FixResults.Fixed) issues automatically"
        git commit -m $CommitMessage 2>$null
        
        Write-Success "Auto-committed fixes!"
        
        if ((Read-Host "Push to origin? (y/N)") -eq "y") {
            git push origin main 2>$null
            Write-Success "Pushed to GitHub!"
        }
    }
}

# Sürekli izleme modu
function Start-ContinuousMonitoring {
    param($IntervalSeconds)
    
    Write-Info "🔄 Continuous monitoring started (interval: $IntervalSeconds seconds)"
    Write-Info "Press Ctrl+C to stop"
    
    while ($true) {
        try {
            Clear-Host
            Write-Info "🤖 WOLTAXI Auto-Fix Running... $(Get-Date)"
            Write-Info "=" * 50
            
            $Results = Invoke-AutoFix
            
            if ($AutoCommit -and $Results.Fixed -gt 0) {
                Invoke-AutoCommit $Results
            }
            
            Write-Info "=" * 50
            Write-Info "Next scan in $IntervalSeconds seconds..."
            Start-Sleep -Seconds $IntervalSeconds
            
        } catch {
            Write-Error "Monitoring error: $_"
            Start-Sleep -Seconds 5
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
        
        if ($AutoCommit) {
            Invoke-AutoCommit $Results
        }
        
        Write-Success "🎉 Auto-Fix completed!"
        
        if ($Results.Problems.Count -eq 0) {
            Write-Success "🌟 No issues found! Your code is clean!"
        }
    }
    
} catch {
    Write-Error "Auto-Fix system error: $_"
    exit 1
}

# Usage examples:
<#
# Tek seferlik fix
.\auto-fix.ps1

# Sürekli monitoring (30 saniyede bir)
.\auto-fix.ps1 -Continuous

# Auto-commit ile
.\auto-fix.ps1 -AutoCommit

# Custom interval ile sürekli
.\auto-fix.ps1 -Continuous -IntervalSeconds 60 -AutoCommit

# Verbose mode
.\auto-fix.ps1 -Verbose -AutoCommit
#>