# Introduction 
Rest Proxy services exposes the various endpoints for the Communication Hub. It exposes following endpoints:
- Register Device
- Retrieve Device Registrations
- De Register Device
- Set Preferences
- Retrieve Preferences
- Send Push Message

The service also generates the micrometer metrics to monitor the service

# Getting Started.

Clone the following repository:
https://dev.azure.com/MetroBank/Payments/_git/comhub-rest-proxy

src/main/resources/application.yaml has the connection details for

- Kafka Broker and the topic it produces to & consumes the message from
- Configuration for MongoDB to retrieve the record
- Configuration to enable the management end-point for service metrics

# Getting Started
1. Checkout the project from the Azure repo
2. Do the Maven project setup in IntelliJ
3. Run mvn clean install

# Build and Test
mvn clean install

### Test APIs in Postman
1. Download the postman collection and environment JSON files from api-collections folder under the root
2. Import the collection and environment in Postman client
3. Start the service
4. Hit the API endpoint(s) and verify the response

# Contribute
Get the Azure repo access and add the API/services modules.