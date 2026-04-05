# run_generate_pom.ps1
# =====================================================================
# Wrapper script: tự động kiểm tra Python, cài libs và chạy generate_pom.py
# Cách dùng: .\scripts\run_generate_pom.ps1
# =====================================================================

$ScriptDir  = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectDir = Split-Path -Parent $ScriptDir
$PomScript  = "$ScriptDir\generate_pom.py"

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  Yokara — Auto POM Generator" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan

# ─── 1. Kiểm tra Python ──────────────────────────────────────────────
function Find-Python {
    foreach ($cmd in @("python", "python3", "py")) {
        try {
            $ver = & $cmd --version 2>&1
            if ($ver -match "Python 3\.") {
                return $cmd
            }
        } catch {}
    }
    return $null
}

$pythonCmd = Find-Python

if (-not $pythonCmd) {
    Write-Host ""
    Write-Host "[ERROR] Không tìm thấy Python 3!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Cài Python theo 1 trong 2 cách:" -ForegroundColor Yellow
    Write-Host "  A) Winget (khuyến nghị):  winget install Python.Python.3.12" -ForegroundColor Yellow
    Write-Host "  B) Tải thủ công:          https://www.python.org/downloads/" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Sau khi cài xong, chạy lại script này." -ForegroundColor Yellow
    exit 1
}

$pyVer = & $pythonCmd --version 2>&1
Write-Host "[ok] Tìm thấy: $pythonCmd  ($pyVer)" -ForegroundColor Green

# ─── 2. Kiểm tra / cài Appium-Python-Client ─────────────────────────
Write-Host "[setup] Kiểm tra dependencies..." -ForegroundColor Gray

$deps = @("Appium-Python-Client", "lxml")
foreach ($dep in $deps) {
    $check = & $pythonCmd -m pip show $dep 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[setup] Cài $dep ..." -ForegroundColor Yellow
        & $pythonCmd -m pip install $dep -q
    } else {
        $ver = ($check | Select-String "Version:").ToString().Trim()
        Write-Host "[ok]    $dep ($ver)" -ForegroundColor Green
    }
}

# ─── 3. Chạy script generate ─────────────────────────────────────────
Write-Host ""
Write-Host "[run] Chạy generate_pom.py ..." -ForegroundColor Cyan
Write-Host ""

Set-Location $ProjectDir
& $pythonCmd $PomScript

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "======================================================" -ForegroundColor Green
    Write-Host "  ✅ Hoàn tất! Mở IDE để xem các file POM mới sinh." -ForegroundColor Green
    Write-Host "======================================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "[ERROR] Script kết thúc với lỗi (exit code $LASTEXITCODE)" -ForegroundColor Red
}
