#!/bin/bash
set -e
set -x

# Generate skelton from example code.

DST=$PWD
TMP=$PWD/tmp/
SRC=$PWD/../examples/sample/

rm -rf $TMP
mkdir -p $TMP
# copy things to temporary directory
rsync --delete -avz $SRC $TMP
pushd $TMP

# Replace sample code by regexp!
perl -i -pe 's/sample/\${artifactId}/g' README.md bower.json
rm sample.iml

# generate archetype templates from the code!
mvn archetype:create-from-project

# And copy it to the destination directory.
rsync --delete -avz target/generated-sources/archetype/src/main/resources/archetype-resources/ $DST/src/main/resources/archetype-resources/

popd
rm -rf src/main/resources/archetype-resources/.idea/
rm -rf $TMP

