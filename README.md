# Stream Combiner
## Description

Stream Combiner is an application that allows you to create a combined stream. This stream combines / merges entries from all (original) individual stream producers.

## Configuration

To configure Stream combiner you need to create yml file with the following configuration:

  1. consumerReadTimeout, consumerReadTimeoutUnits - the maximum time waiting for a response from consumer. If consumer won't answer it will be deleted from list. 
  
  2. xmlConsumerConfigs - list of configurations with following properties:
      * host - host of server that consumer will connect
      * port - port on the server that consumer will connect
      * queueSize  - count of messages that will be stored in application memory for every consumer
 
 #### [Example of configuration file](src/main/resources/config.yml)
 
 ## How To Run
 
 To run the application you need to call:
 
    gradle run --args='$configurationPath' --> where $configurationPath is path to your yml config file
      
      or
    
    gradle clean jar --> this will create jar file in root directory of project
    java -jar stream-combiner.jar $configurationPath --> where $configurationPath is path to your yml config file
