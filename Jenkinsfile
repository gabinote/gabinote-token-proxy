pipeline {
    agent any
    environment {
        REGISTRY_URL = "${env.PRIVATE_REGISTRY_URL}"
        APP_VERSION = '1.0.0'
        IMAGE_NAME = 'token-proxy-server'

        REGISTRY_CRED_ID = 'private-registry-auth'

    }

    stages {

       stage('Generate Tag') {
            steps {
                script {
                    def now = new Date()
                    def timestamp = now.format("yyyyMMdd-HHmm", TimeZone.getTimeZone('Asia/Seoul'))

                    //ex) 0.0.1-20260130-1530
                    env.DOCKER_TAG = "${env.APP_VERSION}-${timestamp}"

                    echo "Use Tag : ${env.DOCKER_TAG}"
                }
            }
       }
      stage('Build Gradle') {
                  steps {
                      script {
                          docker.image('eclipse-temurin:21-jdk').inside {
                              sh 'chmod +x gradlew'
                              sh './gradlew clean bootJar'
                          }
                      }
                  }
              }

        stage('Build & Push Docker') {
            steps {
                script {
                     def imageFullName = "${REGISTRY_URL}/${IMAGE_NAME}:${DOCKER_TAG}"
                     def dockerImage = docker.build(imageFullName)
                     docker.withRegistry("http://${REGISTRY_URL}", REGISTRY_CRED_ID) {
                         dockerImage.push()
                         dockerImage.push('latest')
                     }
                }
            }
        }
    }

    post {
        success {
                echo "Build and Push Successful. Triggering Deployment Job..."
                        build job: 'gabinote-token-proxy-server-preview-deploy',
                              parameters: [
                                  string(name: 'IMAGE_TAG', value: DOCKER_TAG),
                                  string(name: 'APP_NAME', value: IMAGE_NAME)
                              ],
                              wait: false


        }
        failure {
            echo 'Pipeline Failed...'
        }
    }
}