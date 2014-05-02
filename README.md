# Smooks Framework

This is the git source code repository for the Smooks Project.

* [Home Page][1].

## Build Status

[![Build Status](https://travis-ci.org/smooks/smooks.svg?branch=master)](https://travis-ci.org/smooks/smooks)

[Bamboo Builds][3]

## Local Build

1.  `git clone git://github.com/smooks/smooks.git`
2.  `cd smooks`
3.  `mvn -s settings.xml clean install`

Note you will need both maven (version 3+) and git installed on your local machine. 

## Docker Build

You can also build from the [docker](docker.io) image:

1. [Install docker](https://www.docker.io/gettingstarted/).
2. Run `sudo docker build -t smooks github.com/smooks/smooks`.  This will create a docker image named "smooks" that contains the correct build environment and a cloned copy of this git repo.
3. Run `sudo docker run -i smooks mvn clean install` to build the source code.

## Contributing

If you'd like to contribute some code/fixes to Smooks, [please see the following guidelines][2].

[1]: http://www.smooks.org
[2]: http://www.smooks.org/mediawiki/index.php?title=Code_Contribution_Guide
[3]: https://bamboo-ci.codehaus.org/browse/MILYN
