The `scripts` directory contains building and running scripts.
All of them need to be run from root of the project (the directory containing this README).

`build.sh` compiles the project.
It might need setting up JAVA_HOME env variable. The project was written in Java 8.

`registry.sh` needs to be run before `agent.sh` and `signer.sh`.
It uses an option not shown in the lab scenario, so any `rmiregistry` run in different way might not work properly.
Here is the explanation for using this option:
`https://stackoverflow.com/questions/16769729/why-rmi-registry-is-ignoring-the-java-rmi-server-codebase-property?fbclid=IwAR1IgyV-mx32EUJqHyGEJ9_-rbWNzdSeM5AfFJ4IXY1xSwr-WllsBZc83RU`

`client.sh` runs the web UI on localhost:8080.
It accepts signer's hostname as its only argument.
By default connects to localhost agent, but that can be changed in the web interface.

`signer.sh` runs query signer with an RMI API.
It needs path to private key and IP to use as its arguments.
Example private key is in the file `config/keys/private_key.der`.

`agent.sh` takes path to the agent config as its only argument.
`fetcher.sh` takes path to the fetcher config as its only argument.
Example configs are in the `config` folder.
Minimal configs for the live presentation are in the `demo` subdirectories.
