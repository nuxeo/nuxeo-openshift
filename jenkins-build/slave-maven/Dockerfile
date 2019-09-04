FROM docker.io/openshift/jenkins-agent-maven-35-centos7:v4.0

USER root

# Install Git
RUN yum -y install http://opensource.wandisco.com/centos/7/git/x86_64/wandisco-git-release-7-2.noarch.rpm && \
    yum -y install git

ENV NODE_VERSION=6.14.4 \
    NPM_CONFIG_PREFIX=$HOME/.npm-global \
    PATH=$HOME/node_modules/.bin/:$HOME/.npm-global/bin/:$PATH \
    BASH_ENV=/usr/local/bin/scl_enable \
    ENV=/usr/local/bin/scl_enable \
    PROMPT_COMMAND=". /usr/local/bin/scl_enable"

COPY contrib/bin/scl_enable /usr/local/bin/scl_enable
ADD settings.xml $HOME/.m2/

# Install ffmpeg rpm
RUN yum -y install epel-release && \
    rpm -Uvh http://li.nux.ro/download/nux/dextop/el7/x86_64/nux-dextop-release-0-5.el7.nux.noarch.rpm && \
    yum -y install ffmpeg ffmpeg-devel

# Install Nuxeo 3rd party
RUN yum -y upgrade && yum -y install \
    perl \
    ImageMagick \
    ufraw \
    poppler-utils \
    libreoffice \
    libwpd-tools \
    perl-Image-ExifTool \
    ghostscript

# Install NodeJS
RUN yum install -y centos-release-scl-rh && \
    yum -y install gcc-c++ make && \
    curl -sL https://rpm.nodesource.com/setup_6.x | bash - && \
    yum -y install nodejs && \
    npm cache clean -f && \
    npm install -g n  && \
    n stable && \
    ln -sf /usr/local/n/versions/node/8.0.0/bin/node /usr/bin/node  && \
    npm install -g gulp grunt grunt-cli polymer-cli bower yo && \
    rm -rf /var/lib/apt/lists/* && \
    chown -R 1001:0 $HOME && \
    chmod -R g+rw $HOME


USER 1001

RUN source /usr/local/bin/generate_container_user && \
    source /usr/bin/scl_source enable rh-maven35 && \
    git clone https://github.com/dmetzler/nuxeo-dsl && \
    cd nuxeo-dsl && git checkout master && \
    source /usr/bin/scl_source enable rh-maven35 && \
    mvn clean package || \
    cd .. && rm -rf nuxeo-dsl && \
    sed /^default/d /etc/passwd > /tmp/passwd && \
    cp -f /tmp/passwd /etc/passwd && \
    chown -R 1001:0 $HOME/.m2 && \
    chmod -R g+rw $HOME/.m2

RUN chown -R 1001:0  $HOME/{.cache,.npm,.npm-global,.config} && \
    chmod -R g+rw $HOME/{.cache,.npm,.npm-global,.config} && \
    rm -rf $HOME/.config
