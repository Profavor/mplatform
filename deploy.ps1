& minikube -p minikube docker-env | Invoke-Expression

echo "Building Backend Image..."
# docker build --no-cache -t mplatform-backend:v58 -f c:\dev\ai\backend\Dockerfile c:\dev\ai\backend

echo "Building Frontend Image..."
docker build -t mplatform-frontend:v59 -f c:\dev\ai\frontend\Dockerfile c:\dev\ai\frontend

echo "Applying Kubernetes configs..."
kubectl apply -f c:\dev\ai\k8s\30-backend.yaml
kubectl apply -f c:\dev\ai\k8s\31-frontend.yaml

echo "Restarting Deployments to pick up new images..."
kubectl rollout restart deployment backend -n mdm-system
kubectl rollout restart deployment frontend -n mdm-system

echo "Waiting for rollout to complete..."
kubectl rollout status deployment backend -n mdm-system
kubectl rollout status deployment frontend -n mdm-system
