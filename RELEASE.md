## Smooks Release process
This section describes the process to create a release for Smooks. To do this you will need a codehaus account.

### Generate a keypair for signing artifacts
To deploy to Nexus it is required that the artifacts are signed. This section describes how to generate the keys for signing:

Install [pgp](http://www.openpgp.org/resources/downloads.shtml). For example, using Mac OSX:

    brew install gpg

Now, generate the keypair:

    gpg --gen-key

List the key to get the public key identifier:

    gpg --list-keys
    /Users/username/.gnupg/pubring.gpg
    ----------------------------------------
    pub   2048R/DEA94886 2014-02-27
    uid                  Firstname Lastname <firstname.lastname@xyz.com>
    sub   2048R/AB98C664 2014-02-27

For others to be able to verify the signature, in our case Nexus must be able to do this, we to make the public key avaiable:

    gpg --keyserver hkp://pool.sks-keyservers.net --send-keys DEA94886

In the example above we are using the _pub_ identifer.

### Update the project version number
We need to update the project version number. This need to be done once for in the root project and once for the
smooks-examples module. The reason for this is that the smooks-examples module does has a different parent.

    mvn versions:set -DnewVersion=newVersionGoesHere
    cd smooks-examples
    mvn versions:set -DnewVersion=newVersionGoesHere

If all looks good then you can remove the backup files using:

    mvn versions:commit

And if you are not happy you can revert using:

    mvn versions:revert

When you are done you should commit and tag.

### Deploy artifacts to Codehaus Nexus repository

There are 2 ways of doing this, depending on the OS you are running on.  In either case, this should build and upload all artifacts to the [Codehaus Nexus maven Repository](https://nexus.codehaus.org) ([see HAUSEMATE docs for more details](http://docs.codehaus.org/display/HAUSMATES/Codehaus+Maven+Repository+Usage+Guide)), from which we can test and hopefully release the artifacts.


#### Deploy artifacts from a Linux type OS (including Mac OSX)
Simple run:

```
./deploy.sh -u <codehaus-username> -p <codehaus-password> -g <passphrase-of-gpg-key>
```

#### Deploy artifacts from a Docker container
We use Docker to build and deploy artifacts, guaranteeing a consistent build environment.  

Assuming you have Docker installed on the local host system, we install the `smooks` image:

```
docker build -t smooks github.com/smooks/smooks
```

One catch of running the build in a Docker container is that we need to make the GPG key available to the build running in the Docker container.  So, we need to mount the host system's `~/.gnupg` directory into the docker container as the root account's `~/.gnupg` directory by adding `-v $HOME/.gnupg:/.gnupg` to the docker commands.

#### Deploy artifacts from a non-Linux type OS (Windows etc)

Update your `~/.m2/settings.xml` with the correct Codehaus credentials (same as your Xircles login details):

```
<settings>
    <servers>
        <server>
            <id>codehaus-nexus-snapshots</id>
            <username>username</username>
            <password>xxxx</password>
        </server>
        <server>
            <id>codehaus-nexus-staging</id>
            <username>username</username>
            <password>xxxx</password>
        </server>
   </servers>
</settings>
```

Now you are ready to run the maven deploy goal:

    mvn clean deploy -Pdeploy -Dgpg.passphrase=<passphrase-of-gpg-key> -Dmaven.test.skip=true

### Releasing artifacts from Codehaus Nexus repository

You log into the Codehaus Nexus repo using your [Xircles](http://xircles.codehaus.org/) userid and password.  From there, take a look at the “Staging Repository”. The staging repository is where you can inspect what was uploaded and make sure that everything looks peachy.
If it doesn't, you can drop the repository and fix the issue and deploy again. Once you think everything is in order you need to “Close” the repository. Closing will run a number of verification
rules, among them verifying the signatures of the artifacts. Again if something fails you must fix and the drop and deploy again.

If all goes well you should now be able to use the “Release” button, which was previously shaded.  This will make the
artifacts available first on the local nexus which will later sync to maven central.

### Tag the release

    git tag -s vx.x.x <commit_SHA_of_the_prepare_release> -m "Tagging vx.x.x”

Optionally verify the tag:

    git tag -v vx.x.x

Push the tag:

    git push upstream vx.x.x
