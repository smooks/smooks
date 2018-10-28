#!/bin/bash

#
# Run:
# ./deploy.sh -u <repo-username> -p <repo-password> -g <passphrase-of-gpg-key>
#

while getopts u:p:g: option
do
    case "${option}"
    in
        u) USERNM=${OPTARG};;
        p) PASSWD=${OPTARG};;
        g) GPGPPH=${OPTARG};;
    esac
done

export REPO_UN="$USERNM"
export REPO_PW="$PASSWD"

mvn clean deploy -Pdeploy -Dgpg.passphrase="$GPGPPH" -Dmaven.test.skip=true --settings ./settings_deploy.xml