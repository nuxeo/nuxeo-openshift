FROM nuxeo/nuxeo-dev-apb-jenkins-slave-maven:latest

# Install Chrome
USER root
RUN wget https://dl.google.com/linux/direct/google-chrome-stable_current_x86_64.rpm && \
    yum -y install redhat-lsb libXScrnSaver && \
    yum -y install google-chrome-stable_current_x86_64.rpm && \
    rm -rf /home/jenkins/.npm
USER 1000

