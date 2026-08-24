<#
.SYNOPSIS
    MPlatform Docker Build & Publish Automation Script (PowerShell)
.DESCRIPTION
    Builds and publishes backend, frontend, and mobile Docker images to a Docker Registry (Docker Hub, GHCR, Private Registry, etc.).
.PARAMETER Registry
    Docker registry prefix/namespace (e.g., ghcr.io/your-org or docker.io/your-user). Default: $env:DOCKER_REGISTRY or 'mplatform'
.PARAMETER Tag
    Image tag to apply (e.g., v1.0.0, dev, latest). Default: $env:IMAGE_TAG or 'latest'
.PARAMETER Target
    Target service to build (all, backend, frontend, mobile). Default: 'all'
.PARAMETER NoPush
    If specified, builds and tags images locally without pushing to registry.
.PARAMETER SkipLatest
    If specified, skips creating/pushing the 'latest' tag when a specific version tag is provided.
.PARAMETER NoCache
    Build docker images with --no-cache. Default: $false
.EXAMPLE
    .\publish-docker.ps1 -Registry "ghcr.io/myorg" -Tag "v1.0.0" -Target "all"
.EXAMPLE
    .\publish-docker.ps1 -Target backend -NoPush
#>
[CmdletBinding()]
param(
    [string]$Registry = $(if ($env:DOCKER_REGISTRY) { $env:DOCKER_REGISTRY } else { "mplatform" }),
    [string]$Tag = $(if ($env:IMAGE_TAG) { $env:IMAGE_TAG } else { "latest" }),
    [ValidateSet("all", "backend", "frontend", "mobile")]
    [string]$Target = "all",
    [switch]$NoPush,
    [switch]$SkipLatest,
    [switch]$NoCache
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Push = -not $NoPush
$IncludeLatest = -not $SkipLatest

# Color output helpers
function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host "  $Message" -ForegroundColor Cyan
    Write-Host "================================================================" -ForegroundColor Cyan
}

function Write-Step {
    param([string]$Message)
    Write-Host "==> $Message" -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Failure {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Ensure registry format doesn't have trailing slash
$Registry = $Registry.TrimEnd('/')

Write-Header "MPlatform Docker Build & Publish Pipeline"
Write-Host " Registry:        $Registry" -ForegroundColor White
Write-Host " Tag:             $Tag" -ForegroundColor White
Write-Host " Target:          $Target" -ForegroundColor White
Write-Host " Push to Registry:$Push" -ForegroundColor White
Write-Host " Include Latest:  $IncludeLatest" -ForegroundColor White
Write-Host " No Cache:        $NoCache" -ForegroundColor White

$Services = @(
    @{
        Name = "backend"
        ImageName = "mplatform-backend"
        Context = Join-Path $ScriptDir "backend"
        Dockerfile = Join-Path $ScriptDir "backend\Dockerfile"
    },
    @{
        Name = "frontend"
        ImageName = "mplatform-frontend"
        Context = Join-Path $ScriptDir "frontend"
        Dockerfile = Join-Path $ScriptDir "frontend\Dockerfile"
    },
    @{
        Name = "mobile"
        ImageName = "mplatform-mobile"
        Context = Join-Path $ScriptDir "mobile"
        Dockerfile = Join-Path $ScriptDir "mobile\Dockerfile"
    }
)

$SelectedServices = $Services | Where-Object { $Target -eq "all" -or $_.Name -eq $Target }

$Results = @()

foreach ($service in $SelectedServices) {
    $sName = $service.Name
    $fullImageTag = "$Registry/$($service.ImageName):$Tag"
    $latestImageTag = "$Registry/$($service.ImageName):latest"
    
    Write-Header "Processing Service: $sName"
    
    # 1. Build
    Write-Step "Building Docker Image: $fullImageTag"
    $buildArgs = @("build", "-t", $fullImageTag, "-f", $service.Dockerfile, $service.Context)
    if ($NoCache) {
        $buildArgs += "--no-cache"
    }
    
    & docker @buildArgs
    if ($LASTEXITCODE -ne 0) {
        Write-Failure "Failed to build image: $fullImageTag"
        $Results += [PSCustomObject]@{ Service = $sName; Status = "Build Failed"; Image = $fullImageTag }
        continue
    }
    Write-Success "Build completed: $fullImageTag"
    
    # 2. Tag as latest if requested and tag != 'latest'
    if ($IncludeLatest -and $Tag -ne "latest") {
        Write-Step "Tagging as latest: $latestImageTag"
        & docker tag $fullImageTag $latestImageTag
        if ($LASTEXITCODE -ne 0) {
            Write-Failure "Failed to tag latest: $latestImageTag"
        }
    }
    
    # 3. Push to Registry
    if ($Push) {
        Write-Step "Pushing image to registry: $fullImageTag"
        & docker push $fullImageTag
        if ($LASTEXITCODE -ne 0) {
            Write-Failure "Failed to push image: $fullImageTag"
            $Results += [PSCustomObject]@{ Service = $sName; Status = "Push Failed"; Image = $fullImageTag }
            continue
        }
        Write-Success "Successfully pushed: $fullImageTag"
        
        if ($IncludeLatest -and $Tag -ne "latest") {
            Write-Step "Pushing latest image to registry: $latestImageTag"
            & docker push $latestImageTag
            if ($LASTEXITCODE -ne 0) {
                Write-Failure "Failed to push latest: $latestImageTag"
            } else {
                Write-Success "Successfully pushed: $latestImageTag"
            }
        }
        
        $Results += [PSCustomObject]@{ Service = $sName; Status = "Pushed"; Image = $fullImageTag }
    } else {
        $Results += [PSCustomObject]@{ Service = $sName; Status = "Built (Push Skipped)"; Image = $fullImageTag }
    }
}

Write-Header "Publish Summary"
$Results | Format-Table -AutoSize

$FailedCount = ($Results | Where-Object { $_.Status -like "*Failed*" }).Count
if ($FailedCount -gt 0) {
    Write-Failure "$FailedCount service(s) failed."
    exit 1
} else {
    Write-Success "All target services processed successfully!"
}
