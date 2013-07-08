# simple launcher for the client
java -Xmx128m \
-ea \
-cp \
bin:\
./resources:\
./lib/hsqldb.jar:\
../LiquidLnF/bin:\
../LiquidLnF/src:\
../MediaManagerAm/bin:\
../Rbx1600Dcm/rbx1600clientlib-bin:\
../Rbx1600Dcm/bin:\
../Rbx1600/bin:\
../MediaOrb/bin:\
../Util/bin\
    com.streetfiresound.client.AppFramework -c configuration/application.properties

# add to log gc activity -verbose:gc \
