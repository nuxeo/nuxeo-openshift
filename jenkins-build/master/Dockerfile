FROM docker.io/openshift/jenkins-agent-maven-35-centos7:v3.11
USER root
RUN yum -y install http://opensource.wandisco.com/centos/7/git/x86_64/wandisco-git-release-7-2.noarch.rpm && \
    yum -y install git
USER 1000