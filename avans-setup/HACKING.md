# HACKING

## HOW DO I TEST THE BEHAVIOUR?

Install it to local repo.

    mvn install -Dgpg.skip=true

Use it from local repo.

    rm -rf MyWebApp/ ;  mvn archetype:generate -DarchetypeGroupId=me.geso.avans -DarchetypeArtifactId=avans-setup -DarchetypeVersion=0.37.2-SNAPSHOT -DgroupId=com.example.myapp -DartifactId=MyWebApp -Dversion=0.0.1-SNAPSHOT -DinteractiveMode=false; tree MyWebApp/
    pushd MyWebApp
    mvn test

## Clear cache

    mvn archetype:update-local-catalog

## SEE ALSO

http://maven.apache.org/archetype/maven-archetype-plugin/

