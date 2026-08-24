<#
.SYNOPSIS
    MPlatform Kubernetes Deployment Script (PowerShell)
.DESCRIPTION
    Deploys or updates MPlatform services (backend, frontend, mobile, and configs) on a Kubernetes cluster.
.PARAMETER Registry
    Optional container registry (e.g. ghcr.io/myorg). If provided, updates deployment image references.
.PARAMETER Tag
    Docker image tag to deploy. Default: 'latest'
.PARAMETER Build
    If set, builds docker images before deploying.
.PARAMETER Minikube
    If set, configures docker environment for local Minikube daemon.
.EXAMPLE
    .\deploy.ps1 -Registry "ghcr.io/myorg" -Tag "v1.0.0"
.EXAMPLE
    .\deploy.ps1 -Minikube -Build
#>
[CmdletBinding()]
param(
    [string]$Registry = $env:DOCKER_REGISTRY,
    [string]$Tag = $(if ($env:IMAGE_TAG) { $env:IMAGE_TAG } else { "latest" }),
    [switch]$Build,
    [switch]$Minikube
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$K8sDir = Join-Path $ScriptDir "k8s"

function Write-Header {
    param([string]$Msg)
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host "  $Msg" -ForegroundColor Cyan
    Write-Host "================================================================" -ForegroundColor Cyan
}

function Write-Step {
    param([string]$Msg)
    Write-Host "==> $Msg" -ForegroundColor Yellow
}

function Write-Success {
    param([string]$Msg)
    Write-Host "[SUCCESS] $Msg" -ForegroundColor Green
}

Write-Header "Deploying MPlatform to Kubernetes"
Write-Host " Namespace: mdm-system" -ForegroundColor White
Write-Host " Tag:       $Tag" -ForegroundColor White
Write-Host " Registry:  $(if ($Registry) { $Registry } else { '(Default / Local)' })" -ForegroundColor White

if ($Minikube) {
    Write-Step "Configuring Minikube Docker environment..."
    & minikube -p minikube docker-env | Invoke-Expression
}

if ($Build) {
    Write-Step "Building Docker images locally..."
    & powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $ScriptDir "publish-docker.ps1") -Tag $Tag -NoPush
}

Write-Step "Applying Kubernetes Namespace, Configs, Storage, and Infrastructure..."
& kubectl apply -f (Join-Path $K8sDir "00-namespace.yaml")
& kubectl apply -f (Join-Path $K8sDir "01-config.yaml")
& kubectl apply -f (Join-Path $K8sDir "02-pvc.yaml")

Write-Step "Applying Application Deployments (Backend, Frontend, Mobile)..."
& kubectl apply -f (Join-Path $K8sDir "30-backend.yaml")
& kubectl apply -f (Join-Path $K8sDir "31-frontend.yaml")
& kubectl apply -f (Join-Path $K8sDir "32-mobile.yaml")
& kubectl apply -f (Join-Path $K8sDir "40-ingress.yaml")

if ($Registry) {
    $regPrefix = $Registry.TrimEnd('/')
    Write-Step "Setting remote registry image paths..."
    & kubectl set image deployment/backend backend="$regPrefix/mplatform-backend:$Tag" -n mdm-system
    & kubectl set image deployment/frontend frontend="$regPrefix/mplatform-frontend:$Tag" -n mdm-system
    & kubectl set image deployment/mobile mobile="$regPrefix/mplatform-mobile:$Tag" -n mdm-system
}

Write-Step "Restarting Deployments to pick up new images..."
& kubectl rollout restart deployment backend -n mdm-system
& kubectl rollout restart deployment frontend -n mdm-system
& kubectl rollout restart deployment mobile -n mdm-system

Write-Step "Waiting for rollout to complete..."
& kubectl rollout status deployment backend -n mdm-system --timeout=180s
& kubectl rollout status deployment frontend -n mdm-system --timeout=180s
& kubectl rollout status deployment mobile -n mdm-system --timeout=180s

Write-Success "All MPlatform deployments rolled out successfully!"
