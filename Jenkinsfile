pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:3355.v388858a_47b_33-19
    resources:
      requests:
        memory: "1024Mi"
      limits:
        memory: "1024Mi"
  - name: docker
    image: docker:27-dind
    securityContext:
      privileged: true
    volumeMounts:
    - name: docker-storage
      mountPath: /var/lib/docker
  - name: kubectl
    image: alpine/k8s:1.28.3
    command:
    - /bin/sh
    - -c
    - sleep 9999999
    tty: true
  volumes:
  - name: docker-storage
    emptyDir: {}
'''
        }
    }

    environment {
        DOCKERHUB_USERNAME = 'yeaxyzi'
        IMAGE_NAME = 'beatbuddy-backend'
        IMAGE_TAG = "${BUILD_NUMBER}"
        GRADLE_OPTS = '-Xmx512m -Xms256m'
    }

    stages {

        stage('빌드') {
            steps {
                sh 'cd backend && chmod +x gradlew && ./gradlew clean build -x test --no-daemon'
            }
        }

        stage('Docker 이미지 빌드') {
            steps {
                container('docker') {
                    sh '''
                    docker build -f backend/dockerfile -t ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG} backend
                    docker tag ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG} ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:latest
                    '''
                }
            }
        }

        stage('Docker 이미지 Push') {
            steps {
                container('docker') {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-access',
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh '''
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                        docker push ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${IMAGE_TAG}
                        docker push ${DOCKERHUB_USERNAME}/${IMAGE_NAME}:latest
                        '''
                    }
                }
            }
        }

        stage('K8s Secret 생성') {
            steps {
                container('kubectl') {
                    withCredentials([file(credentialsId: 'beatbuddy-secret-env', variable: 'SECRET_FILE')]) {
                        sh '''
                        kubectl create secret generic beatbuddy-secret \
                          --from-env-file=$SECRET_FILE \
                          --dry-run=client -o yaml | kubectl apply -f -
                        '''
                    }
                }
            }
        }

        stage('K8s 배포') {
            steps {
                container('kubectl') {
                    sh '''
                    sed -i "s|IMAGE_TAG|${IMAGE_TAG}|g" k8s/backend/deploy.yaml
                    kubectl apply -f k8s/backend/ -f k8s/mariadb/
                    kubectl rollout status deployment/beatbuddy-backend -n default
                    '''
                }
            }
        }
    }

    post {
        success {
            echo '배포 성공!'
        }
        failure {
            echo '배포 실패!'
            container('kubectl') {
                sh '''
                kubectl rollout undo deployment/beatbuddy-backend -n default
                '''
            }
        }
    }
}