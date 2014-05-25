#!/bin/bash

#
# Run:
# ./deploy.sh -u <codehaus-username> -p <codehaus-password> -g <passphrase-of-gpg-key>
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

export CH_UN=$USERNM
export CH_PW=$PASSWD

mvn clean deploy -Pdeploy -Dgpg.passphrase=$GPGPPH -Dmaven.test.skip=true --settings settings_codehaus.xml