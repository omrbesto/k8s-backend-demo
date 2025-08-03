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
        APP_NAME = 'demo'
        ENVIRONMENT = 'dev'
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'master', url: 'https://github.com/omrbesto/k8s-backend-demo.git'
            }
        }

//         stage('SAST Scan') {
//             steps {
//                 script {
//                     echo "Starting SAST scan with OWASP Dependency-Check..."
//                     withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_KEY')]) {
//                         sh 'mvn org.owasp:dependency-check-maven:check -Dformat=HTML -DoutputDirectory=dependency-check-report -Dnvd.api.key=$NVD_API_KEY'
//                     }
//                     // ตรวจสอบผลลัพธ์และอาจจะทำให้ build fail หากเจอช่องโหว่ระดับสูง
//                     // ตัวอย่างเช่น ตรวจสอบไฟล์ XML report
//                     // หากต้องการให้ Pipeline ไม่ล้มเหลวเมื่อมีช่องโหว่ร้ายแรง
//                     // คุณอาจต้องใช้ Junit หรือ Scripting เพื่ออ่านผลลัพธ์และออกจากการทำงาน
//                     // หรือแค่ใช้ || true เพื่อให้ Pipeline ดำเนินต่อไปแม้จะมี WARN/ERROR (หากคุณแค่ต้องการรายงาน)
//                     // sh 'mvn org.owasp:dependency-check-maven:check -Dformat=HTML -DoutputDirectory=dependency-check-report || true'
//                     archiveArtifacts artifacts: 'dependency-check-report/**/*', fingerprint: true
//                     echo "SAST scan completed. Check artifacts for report."
//                 }
//             }
//         }

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
                    // *** เพิ่ม --timeout เข้าไปในคำสั่ง Trivy ***
                    sh "trivy image --exit-code 1 --severity CRITICAL --timeout 15m ${NEXUS_DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${imageTag}"
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
                    def valuesFile = "apps/backend/${APP_NAME}/values.${ENVIRONMENT}.yaml"
                    // ใช้ Credential ของ GitOps Repo
                    withCredentials([usernamePassword(credentialsId: 'gitops-repo-token', passwordVariable: 'GIT_TOKEN', usernameVariable: 'GIT_USER')]) {
                        // Clone GitOps Repo ลงมาใน workspace ชั่วคราว
//                         if (fileExists('k8s-gitops-demo')) {
//                             dir('k8s-gitops-demo') {
//                                 // pull latest change
//                                 sh "git pull --rebase origin dynamic-gitops"
//                             }
//                         } else {
//                             // Clone ถ้ายังไม่มี
//                             sh "git clone -b dynamic-gitops https://${GIT_USER}:${GIT_TOKEN}@github.com/omrbesto/k8s-gitops-demo.git"
//                         }

                        sh "rm -rf k8s-gitops-demo"

                        // *** 2. git clone ใหม่เสมอ เพื่อให้ได้โค้ดที่สะอาดและถูกต้อง ***
                        sh "git clone -b dynamic-gitops https://${GIT_USER}:${GIT_TOKEN}@github.com/omrbesto/k8s-gitops-demo.git"

                        // เข้าไปใน directory
                        dir('k8s-gitops-demo') {
                            // อัพเดท image tag ในไฟล์ values.yaml
                            sh """
                                sed -i 's|tag: .*|tag: "${imageTag}"|g' ${valuesFile}
                            """

                            // ตั้งค่า git และ commit
                            sh "git config user.email 'jenkins@ci.com'"
                            sh "git config user.name 'Jenkins CI'"
                            sh "git add ${valuesFile}"
                            sh "git commit -m 'Update ${APP_NAME} ${ENVIRONMENT} image to version ${imageTag}'"
                            sh "git push origin dynamic-gitops"
                        }
                    }
                }
            }
        }
    }
}