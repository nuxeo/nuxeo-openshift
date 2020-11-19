#!/bin/bash

# This script install all builded artifact into
# a Nuxeo based image :
#   * Nuxeo Package in `marketplace` is installed thru install-package.sh
#


# Description of environment variable that can be used:
#   * NUXEO_CLID: the CLID to be able to authenticate against Connect
#   * NUXEO_CONNECT_URL: A custom Connect URL
#   * STUDIO_PACKAGE: A studio package to install


# Don't use case sensitive match (only used for True or False here)
shopt -s nocasematch


echo "---> Installing what has been built"


# Handling ENV inputs
ADDITIONAL_NUXEO_PACKAGES=""
CLID=""
CONNECT=""

if [ -n "$NUXEO_CLID" ]; then
  echo "---> Found CLID"
  CLID="--clid $NUXEO_CLID"
else
  echo "---> No CLID found, package installation may be problematic"
fi

if [ -n "$NUXEO_CONNECT_URL" ]; then
  echo "---> Found Custom Connect URL: $NUXEO_CONNECT_URL"
  CONNECT="--connect-url=$NUXEO_CONNECT_URL"
fi

# Installing packages
if [ "$(ls -A /build/marketplace 2>/dev/null)" ]; then
  PACKAGE=$(ls -A /build/marketplace)
  echo "---> Found package $PACKAGE"
  echo "---> Installing Nuxeo Package for project from /build/marketplace"
  /install-packages.sh $CLID $CONNECT /build/marketplace/$PACKAGE
else
  echo "---> No Nuxeo Package found"
fi

if [ -n "$NUXEO_PACKAGES" ]; then
  echo "---> Adding Packages $NUXEO_PACKAGE"
  ADDITIONAL_NUXEO_PACKAGES=$NUXEO_PACKAGES
fi

if [ -n "$STUDIO_PACKAGE" ]; then
  echo "---> Adding Studio Package $STUDIO_PACKAGE"
  ADDITIONAL_NUXEO_PACKAGES="$ADDITIONAL_NUXEO_PACKAGES $STUDIO_PACKAGE"
fi

if [ "$ADDITIONAL_NUXEO_PACKAGES" != "" ]; then
  echo "---> Installing additional packages: $ADDITIONAL_NUXEO_PACKAGES"
  /install-packages $CLID $CONNECT $ADDITIONAL_NUXEO_PACKAGES
fi
