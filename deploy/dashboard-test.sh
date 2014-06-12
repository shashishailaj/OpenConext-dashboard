#!/bin/bash

set -e

usage() { echo "Usage: $0 [-u <SSH username to t02>] [-v <Maven version to deploy>]" 1>&2; exit 1; }

while getopts ":u:v:" o; do
    case "${o}" in
        u)
            username=${OPTARG}
            [ -n "${username}" ]|| usage
            ;;
        v)
            version=${OPTARG}
            [ -n "${version}" ]|| usage
            ;;
        *)
            usage
            ;;
    esac
done

if [ -z "${username}" ] || [ -z "${version}" ]
then
  usage
fi

SSH_COMMANDS=$(cat <<CMD

set -e

DASHBOARD_MAVEN_REPO="https://build.surfconext.nl/repository/public/snapshots/org/surfnet/coin/dashboard-dist"
WORK_DIR="/tmp/dashboard"
MAVEN_METADATA_XML="maven-metadata.xml"
TOMCAT_DIR="/opt/tomcat-low"
APP_NAME="dashboard.test.surfconext.nl"

echo "\${WORK_DIR}"

sudo -u tomcat rm -Rf "\${WORK_DIR}"
sudo -u tomcat mkdir -p "\${WORK_DIR}"

cd "\${WORK_DIR}"
# get metadata from from maven repo
echo "Retrieving full version information for snapshot: ${version}..."
sudo -u tomcat curl "\${DASHBOARD_MAVEN_REPO}/${version}/maven-metadata.xml" -o "\${MAVEN_METADATA_XML}"
FULL_VERSION=\`xmllint --shell "\${MAVEN_METADATA_XML}" <<<"cat /metadata/versioning/snapshotVersions/snapshotVersion[1]/value/text()" | grep -v "^/ >"\`
echo "Installing dashboard \${FULL_VERSION}..."

sudo -u tomcat curl "\${DASHBOARD_MAVEN_REPO}/${version}/dashboard-dist-\${FULL_VERSION}-bin.tar.gz" -o "dashboard-dist-\${FULL_VERSION}-bin.tar.gz"
sudo -u tomcat tar xvfz "dashboard-dist-\${FULL_VERSION}-bin.tar.gz"

sudo /etc/init.d/tomcat6-low stop

echo "Backup current installation"
sudo mv \${TOMCAT_DIR}/wars/dashboard-war-* /opt/tomcat-low/backups

echo "Delete current app"
sudo rm -Rf \${TOMCAT_DIR}/work/Catalina/\${APP_NAME}
sudo -u tomcat rm -Rf \${TOMCAT_DIR}/webapps/dashboard.test.surfconext.nl/*

echo "Copy new ROOT.xml"
sudo cp \${WORK_DIR}/dashboard-dist-${version}/tomcat/conf/context/ROOT.xml \${TOMCAT_DIR}/conf/Catalina/\${APP_NAME}

echo "Copy new WAR"
sudo cp \${WORK_DIR}/dashboard-dist-${version}/tomcat/webapps/dashboard-war-${version}.war \${TOMCAT_DIR}/wars

echo "Done!"
CMD
)

ssh -t -l "${username}" t02.dev.coin.surf.net "${SSH_COMMANDS}" #'bash -s' < deploy.sh "${version}"