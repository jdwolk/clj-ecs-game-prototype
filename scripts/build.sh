lein uberjar
jarsigner -keystore scripts/coppercompass.jsk -storepass infinite_gnow13dgE -keypass coppercomp@\$\$ "target/ecs-test-0.1.0-SNAPSHOT-standalone.jar" coppercompass
cp "target/ecs-test-0.1.0-SNAPSHOT-standalone.jar" ecs-test.jar
