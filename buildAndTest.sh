#!/bin/bash
source "$(dirname $0)"/readProjectProperties.sh
find $classesDir -name *.class -delete
groovyc -d $classesDir $sourceDir/es/osoco/transform/*.groovy && \
	groovy -cp $classesDir $sourceDir/es/osoco/transform/TestEqualsAndHashCode
