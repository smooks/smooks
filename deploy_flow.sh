#!/bin/bash

#
# Run:
# ./deploy.sh -u <repo-username> -p <repo-password> -g <passphrase-of-gpg-key> -v <post-release-version> -f <force-release-version>
#


color() {
      printf '\033[%sm%s\033[m\n' "$@"
      # usage color "31;5" "string"
      # 0 default
      # 5 blink, 1 strong, 4 underlined
      # fg: 31 red,  32 green, 33 yellow, 34 blue, 35 purple, 36 cyan, 37 white
      # bg: 40 black, 41 red, 44 blue, 45 purple
}



line(){
      echo ------------------------------------------------------------------
}


error(){
      color '31;1' "error : $@"

      }

info(){
      color '37;1' "info : $@"
      }

success(){
      color '32;1' "success : $@"
      }

warn(){
      color '33;1' "warn : $@"
      }




while getopts u:p:g:v:f: option
do
    case "${option}"
    in
        u) USERNM=${OPTARG};;
        p) PASSWD=${OPTARG};;
        g) GPGPPH=${OPTARG};;
        v) POSTVE=${OPTARG};;
        v) FORVER=${OPTARG};;
    esac
done

export REPO_UN="$USERNM"
export REPO_PW="$PASSWD"

main_dir=$(pwd)

line
info "0: Precondition"
if [ -z "POSTVE" ];then
     error "Required post release version not set"
     exit 1
fi

info "   Requested post-release project version: $POSTVE, post release version $POSTVE-SNAPSHOT will be created"
info "   Force release project version: $FORVER"


line
info "1: Update project version to release version "

info "   Obtain version from smooks-parent"
cd smooks-parent
current_version=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | sed -n -e '/^\[.*\]/ !{ /^[0-9]/ { p; q } }' | xargs`
info "   Current project version: $current_version"

current_version_no_snapshot=$(echo $current_version | sed -e "s/\-SNAPSHOT//g")
info "   Current project version without snapshot: $current_version_no_snapshot"


if [ -z "$FORVER" ];then
     info "Forcing the release version was not set - current snapshot version will be used"
     release_version=$current_version_no_snapshot
else
     info "Release version was forced, following version will be used: $FORVER"
    release_version=$FORVER
fi



info "   Update project version to: $current_version_no_snapshot"
mvn -q versions:set -DnewVersion=$release_version -DgenerateBackupPoms=false

rc=$?;
if [[ $rc != 0 ]]; then
    error "   New project version not set properly, error code $rc"
    info "Trying to git rest project"
    git reset --hard HEAD
    git clean -f
    exit $rc
else
    success "   New project version set properly, to: $ $current_version_no_snapshot"
    info "  Looking for not updated files ..."
    cd $main_dir
    grep -rl $current_version . | xargs sed -i 's/'$current_version'/'$release_version'/g'
    rc1=$?;
    if [[ $rc1 != 0 ]]; then
        error "   Problem with checking not updated files $rc1"
    else
        success "   Not updated files checked "
    fi
fi


info "Starting release deploy process "
mvn clean deploy -Pdeploy -Dgpg.passphrase="$GPGPPH" -Dmaven.test.skip=true --settings settings_deploy.xml

rc=$?;
if [[ $rc != 0 ]]; then
    error "   Problem during release deploy, error code $rc"
    info "Trying to git rest project"
    git reset --hard HEAD
    git clean -f
    exit $rc
else
    success "   New project version was properly deployed: $ $current_version_no_snapshot"
    info "Committing release version"
        git commit -am "Release version: "$release_version"
        git merge upstream/master
        git push upstream master
        git tag -a v$release_version -m "Release version: "$release_version"
        git push upstream v$release_version
        if [[ $rc1 != 0 ]]; then
                error "   Problem with committing released version to upstream, error $rc1"
            else
                success "   Released version committed to upstream "

                info "   Update project version to post version: $current_version_no_snapshot"
                cd smooks-parent
                mvn -q versions:set -DnewVersion=$POSTVE -DgenerateBackupPoms=false

                rc2=$?;
                if [[ $rc2 != 0 ]]; then
                    error "   New project version not set properly, error code $rc"
                    info "Trying to git rest project"
                    git reset --hard HEAD
                    git clean -f
                    exit $rc2
                else
                    success "   New project version set properly, to: $POSTVE"
                    info "  Looking for not updated files ..."
                    cd $main_dir
                    grep -rl $release_version . | xargs sed -i 's/'$release_version'/'$POSTVE'/g'
                    rc3=$?;
                    if [[ $rc3 != 0 ]]; then
                        error "   Problem with checking not updated files $rc1"
                    else
                        success "   Not updated files checked "
                        git commit -am "New post release version: "$POSTVE"
                        git merge upstream/master
                        git push upstream master
                    fi
                fi

        fi

fi