# Smooks Framework

This is the git source code repository for the [Smooks][1] Project.

## Build Status

[![Build Status](https://travis-ci.org/smooks/smooks.svg?branch=master)](https://travis-ci.org/smooks/smooks)


## Building

### Pre-requisites

1. JDK 1.5
1. Apache Maven 3.2.x

### Maven

1. `git clone git://github.com/smooks/smooks.git`
2. `cd smooks`
3. `mvn clean install`

Note you will need both maven (version 3+) and git installed on your local machine.

## Docker Build

You can also build from the [docker](https://www.docker.io) image:

1. [Install docker](https://www.docker.io/gettingstarted/).
2. Run `sudo docker build -t smooks github.com/smooks/smooks`.  This will create a docker image named "smooks" that contains the correct build environment and a cloned copy of this git repo.
3. Run `sudo docker run -i smooks mvn clean install` to build the source code.

## Community

You can join these groups and chats to discuss and ask Smooks related questions:

- Mailing list: [![google groups: smooks-user](https://img.shields.io/badge/group%3A-smooks--user-blue.svg?style=flat-square)](https://groups.google.com/forum/#!forum/smooks-user)
- Mailing list: [![google groups: smooks-user](https://img.shields.io/badge/group%3A-smooks--dev-blue.svg?style=flat-square)](https://groups.google.com/forum/#!forum/smooks-dev)
- Chat room about using Smooks: [![gitter: smooks/smooks](https://img.shields.io/badge/gitter%3A-smooks%2Fsmooks-blue.svg?style=flat-square)](https://gitter.im/smooks/smooks)
- Issue tracker: [![github: smooks/smooks](https://img.shields.io/badge/github%3A-issues-blue.svg?style=flat-square)](https://github.com/smooks/smooks/issues)


## Contributing

If you'd like to contribute some code/fixes to Smooks, [please see the following guidelines][2].


## License

Smooks is Open Source and available under the LGPL license, Version 3.0.


[1]: http://www.smooks.org
[2]: http://www.smooks.org/mediawiki/index.php?title=Code_Contribution_Guide
[3]: https://groups.google.com/forum/#!forum/smooks-user
[4]: https://groups.google.com/forum/#!forum/smooks-dev
[5]: https://gitter.im/smooks/smooks
[6]: https://github.com/smooks/smooks/issues

