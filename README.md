The `scripts` directory contains building and running scripts.
All of them need to be run from root of the project (the directory containing this README).

`registry.sh` needs to be run before `agent.sh`.
It uses an option not shown during the lectures, so any `rmiregistry` run in different way might not work properly.
Here is the explanation for using this option:
`https://stackoverflow.com/questions/16769729/why-rmi-registry-is-ignoring-the-java-rmi-server-codebase-property?fbclid=IwAR1IgyV-mx32EUJqHyGEJ9_-rbWNzdSeM5AfFJ4IXY1xSwr-WllsBZc83RU`

`client.sh` runs the web UI on localhost:8080
