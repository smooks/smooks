## Smooks Release process
This section describes the process to create a release for Smooks.


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

#### Project configuration

##### Distribution management and authentication

In order to configure Maven to deploy to the OSSRH Nexus Repository Manager with the Nexus Staging Maven plugin you have to configure it like this


```
<distributionManagement>
  <snapshotRepository>
    <id>ossrh</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
  </snapshotRepository>
</distributionManagement>
<build>
  <plugins>
    <plugin>
      <groupId>org.sonatype.plugins</groupId>
      <artifactId>nexus-staging-maven-plugin</artifactId>
      <version>1.6.7</version>
      <extensions>true</extensions>
      <configuration>
        <serverId>ossrh</serverId>
        <nexusUrl>https://oss.sonatype.org/</nexusUrl>
        <autoReleaseAfterClose>true</autoReleaseAfterClose>
      </configuration>
    </plugin>
    ...
  </plugins>
</build>
```



The above configurations will get the user account details to deploy to OSSRH from your Maven settings.xml file. A minimal settings with the authentication is

```
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
  </servers>
</settings>
```


Note how the id element in the server element in settings.xml is identical to the id elements in the snapshotRepository and repository element as well as the serverId configuration of the Nexus Staging Maven plugin

##### GPG signed components

The Maven GPG plugin is used to sign the components with the following configuration.

```
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-gpg-plugin</artifactId>
      <version>1.5</version>
      <executions>
        <execution>
          <id>sign-artifacts</id>
          <phase>verify</phase>
          <goals>
            <goal>sign</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

It relies on the gpg command being installed and the GPG credentials being available e.g. from settings.xml. In addition you can configure the gpg command in case it is different from gpg. This is a common scenario on some operating systems.

```
<settings>
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.executable>gpg2</gpg.executable>
        <gpg.passphrase>the_pass_phrase</gpg.passphrase>
      </properties>
    </profile>
  </profiles>
</settings>
```



## Releasing artifacts

#### Nexus Staging Maven Plugin for Deployment and Release


The Nexus Staging Maven Plugin is the recommended way to deploy your components to OSSRH and release them to the Central Repository. To configure it simply add the plugin to your Maven pom.xml.

```
<build>
<plugins>
...
<plugin>
  <groupId>org.sonatype.plugins</groupId>
  <artifactId>nexus-staging-maven-plugin</artifactId>
  <version>1.6.7</version>
  <extensions>true</extensions>
  <configuration>
     <serverId>ossrh</serverId>
     <nexusUrl>https://oss.sonatype.org/</nexusUrl>
     <autoReleaseAfterClose>true</autoReleaseAfterClose>
  </configuration>
</plugin>
```


#### Update the project version number

We need to update the project version number using the versions Maven plugin. To do this, cd into the smooks-parent directory and execute the following command.

```
mvn versions:set -DnewVersion=newVersionGoesHere
```

If all looks good then you can remove the backup files using:

```
mvn versions:commit
```

And if you are not happy you can revert using:

```
mvn versions:revert
```


#### Nexus Staging Maven Plugin for Deployment and Release

If your version is a release version (does not end in -SNAPSHOT) and with this setup in place, you can run a deployment to OSSRH and an automated release to the Central Repository with the usual:

```
mvn clean deploy -P deploy
```


Please note that the `deploy` profile should be used to enable `javadoc`, `gpg`, and `nexus-staging` build plugins.


##### Deploy artifacts from a Linux type OS (including Mac OSX)

Simple run:
      
```
 ./deploy.sh -u <repo-username> -p <repo-password> -g <passphrase-of-gpg-key>
```



### Build Promotion with the Nexus Staging Suite


First of all become familiar with release staging process:
[Improved Releases with Staging](http://books.sonatype.com/nexus-book/reference/staging.html) 

Log in into [Nexus repository](https://oss.sonatype.org) (using Sonatype Jira account) and inspect staging repository which was created (id of the repository should be printed in the output of the `maven clean deploy` build).
From there, take a look at the “Staging Repository”. The staging repository is where you can inspect what was uploaded and make sure that everything looks peachy.
If it doesn't, you can drop the repository and fix the issue and deploy again. Once you think everything is in order you need to “**Close**” the repository. Closing will run a number of verification
rules, among them verifying the signatures of the artifacts. Again if something fails you must fix and the drop and deploy again.
If successful, the artifacts are now available from the staging repository. From there, people will be able to test these artifacts.  If all goes well (no bugs etc), you should now be able to release the artifacts using the “**Release**” button.  This will make the artifacts available first on the local nexus which will later sync to maven central.

#### Tag the release

    git tag -s vx.x.x <commit_SHA_of_the_prepare_release> -m "Tagging vx.x.x”

Optionally verify the tag:

    git tag -v vx.x.x

Push the tag:

    git push upstream vx.x.x

### Prepare for next development iteration

Once tagging is complete, update the versions in the project POM files for the next release (if necessary). The instructions for doing this (using the **versions** plugin) are outlined above in section titled "**Update the project version number**".





## Deploy artifacts from a Docker container
We use Docker to build and deploy artifacts.  The main benefits of this are that it:

1. Guarantees a consistent, repeatable build environment.
1. Means we can easily build and deploy from an IaaS instance (AWS/Rackspace/etc) instance.

Assuming you have Docker installed on the local host system, we install the `smooks` image:

```
sudo docker build -t smooks github.com/smooks/smooks
```

Once the image is built we can kick off the `deploy.sh` script:

```
sudo docker run -i -v $HOME/.gnupg:/.gnupg smooks ./deploy.sh -u <repo-username> -p <repo-password> -g <passphrase-of-gpg-key>
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


