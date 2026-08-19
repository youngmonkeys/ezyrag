set -e
mvn -pl . clean install
mvn -pl ezyrag-sdk clean install
mvn -pl ezyrag-admin-plugin clean install -Pexport,\!test
mvn -pl ezyrag-socket-plugin clean install -Pexport,\!test
mvn -pl ezyrag-web-plugin clean install -Pexport,\!test
ezy.sh package
ezy.sh export
