pipeline {
    agent any

    tools {
        // ตรวจสอบว่ามี Maven installation ชื่อ 'maven-3.8.5' ใน Global Tool Configuration ของ Jenkins
        maven 'maven-3.9.6'
    }

    environment {
        // ที่อยู่ของ Nexus Docker Registry
        NEXUS_DOCKER_REGISTRY = '192.168.56.103:8082'
        // ชื่อ Docker Image
        DOCKER_IMAGE_NAME = "demo-spring-boot-app"
        // ที่อยู่ของ GitOps Repo
        GITOPS_REPO = 'https://github.com/omrbesto/k8s-gitops-demo.git'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'master', url: 'https://github.com/omrbesto/k8s-backend-demo.git'
            }
        }

        stage('Build & Unit Test') {
            steps {
                // รัน Maven build
                sh "mvn clean package"
            }
        }

        stage('SonarQube Analysis') {
            steps {
                // ใช้ SonarQube Scanner ที่ตั้งค่าไว้ใน Jenkins
                withSonarQubeEnv('My-SonarQube-Server') {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Build & Scan Docker Image') {
            steps {
                script {
                    // สร้าง Tag สำหรับ Image โดยใช้ Build Number ของ Jenkins
                    def imageTag = "${env.BUILD_NUMBER}"
                    // Build Docker Image
                    sh "docker build -t ${NEXUS_DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${imageTag} ."
                    // Scan Image ด้วย Trivy
                    // ถ้าเจอช่องโหว่ระดับ CRITICAL, pipeline จะล้มเหลว
                    sh "trivy image --exit-code 1 --severity CRITICAL ${NEXUS_DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${imageTag}"
                }
            }
        }

        stage('Push Image to Nexus') {
            steps {
                script {
                    def imageTag = "${env.BUILD_NUMBER}"
                    // Login เข้า Nexus โดยใช้ Credential ที่ตั้งไว้
                    withCredentials([usernamePassword(credentialsId: 'nexus-credentials', passwordVariable: 'NEXUS_PASS', usernameVariable: 'NEXUS_USER')]) {
                        sh "docker login -u '${NEXUS_USER}' -p '${NEXUS_PASS}' ${NEXUS_DOCKER_REGISTRY}"
                        // Push Image
                        sh "docker push ${NEXUS_DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${imageTag}"
                    }
                }
            }
        }

        stage('Update GitOps Repository') {
            steps {
                script {
                    def imageTag = "${env.BUILD_NUMBER}"
                    // ใช้ Credential ของ GitOps Repo
                    withCredentials([usernamePassword(credentialsId: 'gitops-repo-token', passwordVariable: 'GIT_TOKEN', usernameVariable: 'GIT_USER')]) {
                        // Clone GitOps Repo ลงมาใน workspace ชั่วคราว
                        if (fileExists('k8s-gitops-demo')) {
                            dir('k8s-gitops-demo') {
                                // ดึงการเปลี่ยนแปลงล่าสุด
                                sh "git pull origin master" // หรือ branch ที่คุณใช้
                            }
                        } else {
                            // Clone ถ้ายังไม่มี
                            sh "git clone https://${GIT_USER}:${GIT_TOKEN}@github.com/omrbesto/k8s-gitops-demo.git"
                        }
                        // เข้าไปใน directory
                        dir('k8s-gitops-demo') {
                            // ใช้คำสั่ง sed เพื่อเปลี่ยน image tag ในไฟล์ deployment.yaml
                            sh "sed -i 's|image: .*|image: ${NEXUS_DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${imageTag}|g' deployment.yaml"
                            // ตั้งค่า git user
                            sh "git config user.email 'jenkins@ci.com'"
                            sh "git config user.name 'Jenkins CI'"
                            // Commit และ Push การเปลี่ยนแปลงกลับขึ้นไป
                            sh "git add deployment.yaml"
                            sh "git commit -m 'Update image to version ${imageTag}'"
                            sh "git push"
                        }
                    }
                }
            }
        }
    }
}