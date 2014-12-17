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
We need to update the project version number using the **versions** Maven plugin. To do this, `cd` into the `smooks-parent` directory and execute the following command.

    mvn versions:set -DnewVersion=newVersionGoesHere

If all looks good then you can remove the backup files using:

    mvn versions:commit

And if you are not happy you can revert using:

    mvn versions:revert

Note you may need to execute the above command multiple time, from the root of the project and from the `smooks-parent` directory.

When you are done you should commit and tag.

### Deploy artifacts to Codehaus Nexus repository

There are 2 ways of doing this, depending on the OS you are running on.  In either case, this should build and upload all artifacts to the [Codehaus Nexus maven Repository](https://nexus.codehaus.org) ([see HAUSEMATE docs for more details](http://docs.codehaus.org/display/HAUSMATES/Codehaus+Maven+Repository+Usage+Guide)), from which we can test and hopefully release the artifacts.

___Note: Before deploying, attempt to manually log into the Nexus web interface at https://nexus.codehaus.org. If login fails, try logging into [Xircles](http://xircles.codehaus.org/), resetting the password if that fails. Once you can log into Xircles, you should be able to log into Nexus.___.

___Note: The Codehaus Nexus repo often runs into disk space issues, causing the deploys to fail with a 500 error.  See [Jira HAUS-2339](https://jira.codehaus.org/browse/HAUS-2339)___.


#### Deploy artifacts from a Linux type OS (including Mac OSX)
Simple run:

```
./deploy.sh -u <codehaus-username> -p <codehaus-password> -g <passphrase-of-gpg-key>
```

#### Deploy artifacts from a Docker container
We use Docker to build and deploy artifacts.  The main benefits of this are that it:

1. Guarantees a consistent, repeatable build environment.
1. Means we can easily build and deploy from an IaaS instance (AWS/Rackspace/etc) instance.

Assuming you have Docker installed on the local host system, we install the `smooks` image:

```
sudo docker build -t smooks github.com/smooks/smooks
```

Once the image is built we can kick off the `deploy.sh` script:

```
sudo docker run -i -v $HOME/.gnupg:/.gnupg smooks ./deploy.sh -u <codehaus-username> -p <codehaus-password> -g <passphrase-of-gpg-key>
```

You might notice the `-v $HOME/.gnupg:/.gnupg` parameter in the docker run command.  That is mounting the host system's `~/.gnupg` directory into the docker container as the root account's `~/.gnupg` directory so the GPG plugin can sign the artifacts using the GPG key generated above.

If you are running the deploy from an IaaS instance (AWS/Rackspace/etc), you can generate and publish a new GPG key on the IaaS instance.  Alternatively, you might want to export/import your GPG from your local machine to the IaaS instance.  This is easy.

We start by listing the keys on the local machine as follows:

```
$ gpg --list-keys
<HOME>/.gnupg/pubring.gpg
-----------------------------------
pub   2048R/234A1231 2014-05-24 [expires: 2018-05-24]
uid                  TOM FENNELLY <tom.fennelly@gmail.com>
sub   2048R/ABC12345 2014-05-24 [expires: 2018-05-24]
```

Then, we export the public and secret keys and copy them to the IaaS instance:

```
$ gpg --output pubkey.gpg --armor --export 234A1231
$ gpg --output secretkey.gpg --armor --export-secret-key 234A1231
$ scp pubkey.gpg secretkey.gpg root@<iaas-host-ip>:~/
```

And finally, on the IaaS host instance, we import the keys we just copied to it:

```
$ gpg --import pubkey.gpg
$ gpg --allow-secret-key-import --import secretkey.gpg

```

Of course you can check the import by running `gpg --list-keys` on the IaaS host instance.  Now your docker IaaS host instance has your GPG keys installed and you can execute the docker run command to deploy the artifacts to the Codehaus maven repo.

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
If it doesn't, you can drop the repository and fix the issue and deploy again. Once you think everything is in order you need to “**Close**” the repository. Closing will run a number of verification
rules, among them verifying the signatures of the artifacts. Again if something fails you must fix and the drop and deploy again.

If successfull, the artifacts are now available from the Codehaus Staging repository. From there, people will be able to test these artifacts.  If all goes well (no bugs etc), you should now be able to release the artifacts using the “**Release**” button.  This will make the artifacts available first on the local nexus which will later sync to maven central.

### Tag the release

    git tag -s vx.x.x <commit_SHA_of_the_prepare_release> -m "Tagging vx.x.x”

Optionally verify the tag:

    git tag -v vx.x.x

Push the tag:

    git push upstream vx.x.x
