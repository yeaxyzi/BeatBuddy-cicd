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
    image: bitnami/kubectl:latest
    command:
    - cat
    tty: true
    args:
    - infinity
  volumes:
  - name: docker-storage
    emptyDir: {}
'''
        }
    }

    environment {
        IMAGE_NAME = 'beatbuddy-backend'
        IMAGE_TAG = "${BUILD_NUMBER}"
        GRADLE_OPTS = '-Xmx512m -Xms256m'
    }

    stages {
        stage('체크아웃') {
            steps {
                checkout scm
            }
        }

        stage('빌드') {
            steps {
                sh './gradlew clean build -x test --no-daemon'
            }
        }

        stage('Docker 이미지 빌드') {
            steps {
                container('docker') {
                    sh 'docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .'
                }
            }
        }

        stage('K8s 배포') {
            steps {
                container('kubectl') {
                    sh 'kubectl apply -f k8s/'
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
        }
    }
}