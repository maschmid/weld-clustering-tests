Weld clustering tests, based on Weld examples

Instructions:

1. Prepare two AS7/EAP6 installations, node1 and node2

2. Edit node{1,2}/standalone/configuration/standalone-ha.xml, to configure on two loopback adresses, you may use, e,g:

node1:

    <interfaces>
        <interface name="management">
            <loopback-address value="127.0.1.1"/>
        </interface>
        <interface name="public">
            <loopback-address value="127.0.1.1"/>
        </interface>
        <interface name="unsecure">
            <loopback-address value="127.0.1.1"/>
        </interface>
    </interfaces>

node2:

    <interfaces>
        <interface name="management">
            <loopback-address value="127.0.2.1"/>
        </interface>
        <interface name="public">
            <loopback-address value="127.0.2.1"/>
        </interface>
        <interface name="unsecure">
            <loopback-address value="127.0.2.1"/>
        </interface>
    </interfaces>


3. run

    mvn clean verify -Darquillian=jbossas-cluster-7 \
        -Dnode1.jbossHome=/path/to/node1/jboss-eap-6.0 \
        -Dnode2.jbossHome=/path/to/node2/jboss-eap-6.0 \
        -Djboss.as7.version=7.1.3.Final
        -Dnode1.contextPath="http://127.0.1.1:8080/weld-clustering-tests" \
        -Dnode2.contextPath="http://127.0.2.1:8080/weld-clustering-tests" \
        -Dnode1.managementAddress=127.0.1.1 \
        -Dnode2.managementAddress=127.0.2.1


