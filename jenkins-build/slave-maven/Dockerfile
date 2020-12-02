FROM docker.io/openshift/jenkins-agent-maven-35-centos7

USER root

# Install Git
RUN yum -y install http://opensource.wandisco.com/centos/7/git/x86_64/wandisco-git-release-7-2.noarch.rpm && \
    yum -y install git

ENV NODE_VERSION=10 \
    NPM_CONFIG_PREFIX=$HOME/.npm-global \
    PATH=$HOME/node_modules/.bin/:$HOME/.npm-global/bin/:/opt/maven/bin:$PATH \
    BASH_ENV=/usr/local/bin/scl_enable \
    ENV=/usr/local/bin/scl_enable \
    PROMPT_COMMAND=". /usr/local/bin/scl_enable"

COPY contrib/bin/scl_enable /usr/local/bin/scl_enable
ADD settings.xml $HOME/.m2/

# Install ffmpeg rpm
RUN yum -y install epel-release && \
    rpm -Uvh http://li.nux.ro/download/nux/dextop/el7/x86_64/nux-dextop-release-0-5.el7.nux.noarch.rpm && \
    yum -y install ffmpeg ffmpeg-devel

# Install exiftool and wkhtmltopdf
RUN yum install -y perl-Image-ExifTool && \
    yum install -y https://github.com/wkhtmltopdf/wkhtmltopdf/releases/download/0.12.5/wkhtmltox-0.12.5-1.centos7.x86_64.rpm

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

# Install Java 11
RUN yum install -y java-11-openjdk-devel

# Install Maven
RUN curl -L --output /tmp/apache-maven-bin.zip  https://www-eu.apache.org/dist/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.zip && \
    unzip -q /tmp/apache-maven-bin.zip -d /opt && \
    ln -s /opt/apache-maven-3.6.3 /opt/maven && \
    rm /tmp/apache-maven-bin.zip && \
    mkdir -p $HOME/.m2



ENV LIBREOFFICE7_VERSION=7.0.3
# Install LibreOffice7
RUN mkdir -p /tmp/installoo && \
    curl -Ls  https://download.documentfoundation.org/libreoffice/stable/${LIBREOFFICE7_VERSION}/rpm/x86_64/LibreOffice_${LIBREOFFICE7_VERSION}_Linux_x86-64_rpm.tar.gz | tar zxf - -C /tmp/installoo/ && \
    rpm -Uvh /tmp/installoo/LibreOffice_${LIBREOFFICE7_VERSION}.1_Linux_x86-64_rpm/RPMS/*.rpm && \
    rm -rf /tmp/installoo/



# Install NodeJS
RUN yum install -y centos-release-scl-rh && \
    yum -y install gcc-c++ make && \
    curl -sL https://rpm.nodesource.com/setup_${NODE_VERSION}.x | bash - && \
    yum -y install nodejs && \
    npm cache clean -f && \
    npm install -g gulp grunt grunt-cli bower yo && \
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
    export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-11.0.9.11-0.el7_9.x86_64 && \
    git clone https://github.com/nuxeo-cloud/nuxeo-cloud-sample && \
    cd nuxeo-cloud-sample && git checkout main && \
    source /usr/bin/scl_source enable rh-maven35 && \
    mvn clean package || \
    cd .. && rm -rf nuxeo-cloud-sample && \
    sed /^default/d /etc/passwd > /tmp/passwd && \
    cp -f /tmp/passwd /etc/passwd && \
    chown -R 1001:0 $HOME/.m2 && \
    chmod -R g+rw $HOME/.m2

RUN chown -R 1001:0  $HOME/{.npm,.npm-global,.config} && \
    chmod -R g+rw $HOME/{.npm,.npm-global,.config} && \
    rm -rf $HOME/.config
