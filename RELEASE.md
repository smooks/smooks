## Smooks Release process
This section describes the process to create a release for Smooks. To do this you will to have a codehaus account. 

### Generate a keypair for signing artifacts
To deploy to Nexus it is required that the artifacts are signed. This section describes how to generate the keys for signing:

Install [pgp](http://www.openpgp.org/resources/downloads.shtml). For example, using Mac OSX:

    brew install gpg

Now, generate the keypair:

    gpg —gen-key

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

### Update settings.xml
Update your settings.xml with the correct credentials:

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

### Run the deploy goal/target
Now you are ready to run the maven deploy goal:

    mvn clean deploy -Pdeploy-release -Dgpg.passphrase=key-password

This should build and upload and complete successfully. Things will be uploaded to the [Codehaus Nexus maven Repository](https://nexus.codehaus.org) ([see HAUSEMATE docs for more details](http://docs.codehaus.org/display/HAUSMATES/Codehaus+Maven+Repository+Usage+Guide)).

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



