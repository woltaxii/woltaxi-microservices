# Safely rename only problematic root-level .md files starting with "# "
$ErrorActionPreference = 'SilentlyContinue'
function MakeSafe([string]$name){
  $s = $name -replace '^#+\s*',''
  $s = [System.Text.RegularExpressions.Regex]::Replace($s, "[^\u0000-\u007F]", "")
  $s = $s -replace "\s+","-"
  $s = $s -replace "[^A-Za-z0-9_.-]",""
  $s.Trim('-')
}
Get-ChildItem -File -Path . -Filter "*.md" | Where-Object { $_.Name -match '^#\s' } | ForEach-Object {
  $new = MakeSafe $_.Name
  if($new -and $new -ne $_.Name){ Move-Item -LiteralPath $_.FullName -Destination (Join-Path $_.DirectoryName $new) -Force; Write-Host "Renamed: $($_.Name) -> $new" }
}
