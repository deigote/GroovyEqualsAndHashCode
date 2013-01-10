#!/bin/bash
find classes -name *.class -delete
groovyc -d classes es/osoco/transform/*.groovy && groovy -cp classes es/osoco/transform/TestEqualsAndHashCode
