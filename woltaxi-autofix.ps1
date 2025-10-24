# 🤖 WOLTAXI Auto-Fix PowerShell System - SIMPLE VERSION
# Sürekli çalışan akıllı hata düzeltme sistemi

param(
    [switch]$Continuous = $false,
    [int]$IntervalSeconds = 60,
    [switch]$AutoCommit = $false
)

# Renkli output fonksiyonları
function Write-Success { param($Message) Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Error { param($Message) Write-Host "❌ $Message" -ForegroundColor Red }
function Write-Info { param($Message) Write-Host "ℹ️ $Message" -ForegroundColor Cyan }
function Write-Warning { param($Message) Write-Host "⚠️ $Message" -ForegroundColor Yellow }
function Write-Fix { param($Message) Write-Host "🔧 $Message" -ForegroundColor Magenta }

# Dosya adı düzeltme fonksiyonu
function Fix-FileNames {
    Write-Fix "Problematic file names düzeltiliyor..."
    
    # Emoji ve Türkçe karakter içeren dosyaları bul
    $ProblematicFiles = Get-ChildItem -Recurse -File | Where-Object { 
        $_.Name -like "*MASAÜSTÜ*" -or 
        $_.Name -like "*YEDEK*" -or 
        $_.Name -like "*🚀*" -or 
        $_.Name -like "*📋*" -or 
        $_.Name -like "*#*"
    }
    
    $Fixed = $false
    foreach ($File in $ProblematicFiles) {
        $SafeName = $File.Name
        $SafeName = $SafeName -replace "MASAÜSTÜ", "DESKTOP"
        $SafeName = $SafeName -replace "YEDEK", "BACKUP"
        $SafeName = $SafeName -replace "PROJESİ", "PROJECT"
        $SafeName = $SafeName -replace "ÇALIŞM", "WORK"
        $SafeName = $SafeName -replace "🚀", ""
        $SafeName = $SafeName -replace "📋", ""
        $SafeName = $SafeName -replace "#", ""
        $SafeName = $SafeName -replace "\s+", "-"
        $SafeName = $SafeName.Trim("-")
        
        if ($SafeName -ne $File.Name -and $SafeName -ne "") {
            Write-Fix "Renaming: $($File.Name) → $SafeName"
            try {
                $NewPath = Join-Path $File.Directory $SafeName
                Move-Item $File.FullName $NewPath -Force
                Write-Success "Renamed: $($File.Name)"
                $Fixed = $true
            } catch {
                Write-Warning "Rename failed: $_"
            }
        }
    }
    return $Fixed
}

# PowerShell dosya düzeltme fonksiyonu
function Fix-PowerShellFiles {
    Write-Fix "PowerShell dosyaları düzeltiliyor..."
    
    $PSFiles = Get-ChildItem -Recurse -Filter "*.ps1" -ErrorAction SilentlyContinue
    $Fixed = $false
    
    foreach ($PSFile in $PSFiles) {
        try {
            $Content = Get-Content $PSFile.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
            if ($Content) {
                $OriginalContent = $Content
                
                # Basit düzeltmeler
                $Content = $Content -replace 'iex ', 'Invoke-Expression '
                $Content = $Content -replace '\[string\]\$Password', '[SecureString]$Password'
                
                if ($Content -ne $OriginalContent) {
                    Set-Content $PSFile.FullName $Content -Encoding UTF8
                    Write-Success "Fixed: $($PSFile.Name)"
                    $Fixed = $true
                }
            }
        } catch {
            Write-Warning "Error processing $($PSFile.Name): $_"
        }
    }
    return $Fixed
}

# Git durumu düzeltme
function Fix-GitStatus {
    Write-Fix "Git durumu düzeltiliyor..."
    
    try {
        # Git durumunu kontrol et
        $GitStatus = git status --porcelain 2>$null
        
        if ($GitStatus) {
            git add . 2>$null
            Write-Success "Git files staged"
            return $true
        }
        return $false
    } catch {
        Write-Warning "Git operation failed: $_"
        return $false
    }
}

# Geçici dosyaları temizle
function Clean-TempFiles {
    Write-Fix "Geçici dosyalar temizleniyor..."
    
    $TempPatterns = @("*.tmp", "*.log", "*~", "Thumbs.db")
    $Cleaned = $false
    
    foreach ($Pattern in $TempPatterns) {
        $TempFiles = Get-ChildItem -Recurse -Filter $Pattern -ErrorAction SilentlyContinue
        foreach ($TempFile in $TempFiles) {
            try {
                Remove-Item $TempFile.FullName -Force
                Write-Success "Cleaned: $($TempFile.Name)"
                $Cleaned = $true
            } catch {
                Write-Warning "Cleanup failed: $($TempFile.Name)"
            }
        }
    }
    return $Cleaned
}

# Ana Auto-Fix fonksiyonu
function Invoke-AutoFix {
    Write-Info "🔍 WOLTAXI Auto-Fix başlatılıyor..."
    
    $FixCount = 0
    
    # Dosya adlarını düzelt
    if (Fix-FileNames) { $FixCount++ }
    
    # PowerShell dosyalarını düzelt
    if (Fix-PowerShellFiles) { $FixCount++ }
    
    # Git durumunu düzelt
    if (Fix-GitStatus) { $FixCount++ }
    
    # Geçici dosyaları temizle
    if (Clean-TempFiles) { $FixCount++ }
    
    Write-Info "📊 Auto-Fix tamamlandı: $FixCount kategoride düzeltme yapıldı"
    
    return @{
        FixCount = $FixCount
        Success = ($FixCount -gt 0)
    }
}

# Auto-commit fonksiyonu
function Invoke-AutoCommit {
    param($Results)
    
    if ($Results.Success) {
        Write-Info "🚀 Auto-commit yapılıyor..."
        
        try {
            git add . 2>$null
            $CommitMessage = "🤖 Auto-Fix: $($Results.FixCount) kategori düzeltildi - $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
            git commit -m $CommitMessage 2>$null
            Write-Success "Auto-commit tamamlandı!"
            return $true
        } catch {
            Write-Warning "Auto-commit başarısız: $_"
            return $false
        }
    }
    return $false
}

# Sürekli izleme modu
function Start-ContinuousMode {
    Write-Info "🔄 Sürekli izleme modu başlatıldı (her $IntervalSeconds saniye)"
    Write-Info "Durdurmak için Ctrl+C basın"
    
    $Cycle = 0
    
    while ($true) {
        try {
            $Cycle++
            Clear-Host
            Write-Info "🤖 WOLTAXI Auto-Fix - Döngü #$Cycle - $(Get-Date -Format 'HH:mm:ss')"
            Write-Info "=" * 60
            
            $Results = Invoke-AutoFix
            
            if ($AutoCommit -and $Results.Success) {
                Invoke-AutoCommit $Results | Out-Null
            }
            
            Write-Info "=" * 60
            Write-Info "💤 $IntervalSeconds saniye bekleniyor..."
            Start-Sleep -Seconds $IntervalSeconds
            
        } catch {
            Write-Error "Monitoring hatası: $_"
            Start-Sleep -Seconds 30
        }
    }
}

# Ana çalıştırma
try {
    Write-Info "🤖 WOLTAXI Intelligent Auto-Fix System"
    Write-Info "=" * 50
    
    if ($Continuous) {
        Start-ContinuousMode
    } else {
        $Results = Invoke-AutoFix
        
        if ($AutoCommit -and $Results.Success) {
            Invoke-AutoCommit $Results | Out-Null
        }
        
        if ($Results.Success) {
            Write-Success "🎉 Auto-Fix başarıyla tamamlandı!"
        } else {
            Write-Success "🌟 Düzeltilecek sorun bulunamadı!"
        }
    }
    
} catch {
    Write-Error "Auto-Fix sistem hatası: $_"
    exit 1
}

Write-Info ""
Write-Info "💡 Kullanım:"
Write-Info "  Tek tarama:       .\woltaxi-autofix.ps1"
Write-Info "  Auto-commit:      .\woltaxi-autofix.ps1 -AutoCommit"
Write-Info "  Sürekli mod:      .\woltaxi-autofix.ps1 -Continuous"
Write-Info "  Özel aralık:      .\woltaxi-autofix.ps1 -Continuous -IntervalSeconds 120"