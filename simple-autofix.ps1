# 🤖 WOLTAXI PowerShell Otomatik Düzeltme Sistemi
# Sürekli çalışan akıllı hata düzeltme ve izleme sistemi

Write-Host "🤖 WOLTAXI Intelligent Auto-Fix System" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

# Dosya adı düzeltme
Write-Host "🔧 Problematic file names düzeltiliyor..." -ForegroundColor Magenta

$ProblematicFiles = Get-ChildItem -Recurse -File | Where-Object { 
    $_.Name -like "*MASAÜSTÜ*" -or 
    $_.Name -like "*YEDEK*" -or 
    $_.Name -like "*PROJESİ*" -or
    $_.Name -like "*ÇALIŞM*" -or
    $_.Name -like "*DO.md*" -or
    $_.Name -match "#"
}

$Fixed = $false
foreach ($File in $ProblematicFiles) {
    $SafeName = $File.Name
    $SafeName = $SafeName -replace "MASAÜSTÜ", "DESKTOP"
    $SafeName = $SafeName -replace "YEDEK", "BACKUP"
    $SafeName = $SafeName -replace "PROJESİ", "PROJECT"
    $SafeName = $SafeName -replace "ÇALIŞM", "WORK"
    $SafeName = $SafeName -replace "DO\.md", "DOCS.md"
    $SafeName = $SafeName -replace "#", ""
    $SafeName = $SafeName -replace "\s+", "-"
    $SafeName = $SafeName.Trim("-")
    
    if ($SafeName -ne $File.Name -and $SafeName -ne "") {
        Write-Host "🔧 Renaming: $($File.Name) → $SafeName" -ForegroundColor Magenta
        try {
            $NewPath = Join-Path $File.Directory $SafeName
            Move-Item $File.FullName $NewPath -Force
            Write-Host "✅ Renamed: $($File.Name)" -ForegroundColor Green
            $Fixed = $true
        } catch {
            Write-Host "⚠️ Rename failed: $_" -ForegroundColor Yellow
        }
    }
}

# PowerShell dosya kontrolü
Write-Host "🔧 PowerShell dosyaları kontrol ediliyor..." -ForegroundColor Magenta

$PSFiles = Get-ChildItem -Recurse -Filter "*.ps1" -ErrorAction SilentlyContinue
foreach ($PSFile in $PSFiles) {
    try {
        $Content = Get-Content $PSFile.FullName -Raw -Encoding UTF8 -ErrorAction SilentlyContinue
        if ($Content) {
            $OriginalContent = $Content
            
            # Syntax hatalarını düzelt
            $Content = $Content -replace 'iex ', 'Invoke-Expression '
            $Content = $Content -replace '\[string\]\$Password', '[SecureString]$Password'
            
            if ($Content -ne $OriginalContent) {
                Set-Content $PSFile.FullName $Content -Encoding UTF8
                Write-Host "✅ Fixed: $($PSFile.Name)" -ForegroundColor Green
                $Fixed = $true
            }
        }
    } catch {
        Write-Host "⚠️ Error processing $($PSFile.Name): $_" -ForegroundColor Yellow
    }
}

# Git durumu kontrol
Write-Host "🔧 Git durumu kontrol ediliyor..." -ForegroundColor Magenta

try {
    $GitStatus = git status --porcelain 2>$null
    
    if ($GitStatus) {
        git add . 2>$null
        Write-Host "✅ Git files staged" -ForegroundColor Green
        $Fixed = $true
    }
} catch {
    # Sessiz geç
}

# Geçici dosyaları temizle
Write-Host "🔧 Geçici dosyalar temizleniyor..." -ForegroundColor Magenta

$TempPatterns = @("*.tmp", "*.log", "*~", "Thumbs.db")
foreach ($Pattern in $TempPatterns) {
    $TempFiles = Get-ChildItem -Recurse -Filter $Pattern -ErrorAction SilentlyContinue
    foreach ($TempFile in $TempFiles) {
        try {
            Remove-Item $TempFile.FullName -Force
            Write-Host "✅ Cleaned: $($TempFile.Name)" -ForegroundColor Green
            $Fixed = $true
        } catch {
            # Sessizce geç
        }
    }
}

# .gitignore güncelleme
Write-Host "🔧 Netlify deployment hazırlığı..." -ForegroundColor Magenta

$GitIgnorePath = ".gitignore"
$NetlifyIgnores = @(
    "*.tmp",
    "*.log", 
    "*~",
    "Thumbs.db",
    ".DS_Store",
    "node_modules/",
    "dist/",
    ".env",
    "*.zip"
)

try {
    if (Test-Path $GitIgnorePath) {
        $CurrentIgnores = Get-Content $GitIgnorePath -ErrorAction SilentlyContinue
    } else {
        $CurrentIgnores = @()
    }
    
    $ToAdd = @()
    foreach ($Ignore in $NetlifyIgnores) {
        if ($CurrentIgnores -notcontains $Ignore) {
            $ToAdd += $Ignore
        }
    }
    
    if ($ToAdd.Count -gt 0) {
        Add-Content $GitIgnorePath ($ToAdd -join "`n")
        Write-Host "✅ Updated .gitignore with $($ToAdd.Count) entries" -ForegroundColor Green
        $Fixed = $true
    }
} catch {
    Write-Host "⚠️ .gitignore update failed" -ForegroundColor Yellow
}

# Auto-commit
if ($Fixed) {
    Write-Host "🚀 Auto-commit yapılıyor..." -ForegroundColor Cyan
    
    try {
        git add . 2>$null
        $CommitMessage = "🤖 Auto-Fix: Sistem düzeltmeleri - $(Get-Date -Format 'yyyy-MM-dd HH:mm')"
        git commit -m $CommitMessage 2>$null
        Write-Host "✅ Auto-commit tamamlandı!" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ Auto-commit başarısız" -ForegroundColor Yellow
    }
} else {
    Write-Host "🌟 Sistem temiz - düzeltilecek sorun yok!" -ForegroundColor Green
}

Write-Host ""
Write-Host "🎉 WOLTAXI Auto-Fix tamamlandı!" -ForegroundColor Green
Write-Host "💡 Sürekli izleme için: powershell -Command '& { while ($true) { Clear-Host; .\simple-autofix.ps1; Start-Sleep 60 } }'" -ForegroundColor Cyan