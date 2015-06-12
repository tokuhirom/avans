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
perl -i -pe 's/sample/\${artifactId}/g' README.md bower.json src/main/java/freemarker_implicit.ftl
rm -rf .idea/ *.iml

# generate archetype templates from the code!
mvn archetype:create-from-project

# And copy it to the destination directory.
rsync --delete -avz target/generated-sources/archetype/src/main/resources/ $DST/src/main/resources/

popd
rm -rf src/main/resources/archetype-resources/.idea/
rm -rf $TMP

# Enable filters for top level resources.
perl <<'EOP'
use strict;
use autodie;
my $content = do {
    open my $fh, '<', 'src/main/resources/META-INF/maven/archetype-metadata.xml';
    local $/; <$fh>
};
$content =~ s!(<fileSet) (encoding="UTF-8">\s*?<directory></directory>)!$1 filtered="true" $2!s;
open my $fh, '>', 'src/main/resources/META-INF/maven/archetype-metadata.xml';
print {$fh} $content;
EOP

mvn clean package
mvn archetype:update-local-catalog
mvn archetype:jar archetype:integration-test -DskipTests
mvn install -Dgpg.skip=true

echo mvn archetype:generate -DarchetypeCatalog=local -DarchetypeGroupId=me.geso.avans -DarchetypeArtifactId=avans-setup -DgroupId=com.example.myapp -DartifactId=MyWebApp -Dversion=0.0.1-SNAPSHOT -DinteractiveMode=false

