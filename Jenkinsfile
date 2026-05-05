pipeline {
    agent any

    environment {
        IMAGE_NAME = 'beatbuddy-backend'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('체크아웃') {
            steps {
                checkout scm
            }
        }

        stage('테스트') {
            steps {
                sh './gradlew test'
            }
        }

        stage('빌드') {
            steps {
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker 이미지 빌드') {
            steps {
                sh 'docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .'
            }
        }

        stage('K8s 배포') {
            steps {
                sh 'kubectl apply -f k8s/'
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