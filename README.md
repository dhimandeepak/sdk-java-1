#CyberSource Simple Order API for Java

[![Build Status](https://travis-ci.org/CyberSource/cybersource-sdk-java.png?branch=master)]
(https://travis-ci.org/CyberSource/cybersource-sdk-java)

##Package Managers

### Maven
To install the cybersource-sdk-java from central repository,add dependency to your application pom.xml as below.
````
        <dependency>
            <groupId>com.cybersource</groupId>
            <artifactId>cybersource-sdk-java</artifactId>
            <version>6.0.1</version>
        </dependency> 
````
 Run mvn install, to install dependency

### Grails/Gradle
Add the dependency to your build.gradle
````
dependencies {
    compile 'com.cybersource:cybersource-sdk-java:6.0.1'
    }
````
##Requirements


1. Java SDK 1.6 and later  
2. Maven 3 and later  
3. Unlimited Strength Jurisdiction Policy files from Oracle® (US_export_policy.jar and local_policy.jar), available at http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
	
##Prerequisites


######A CyberSource Evaluation account. 

Sign up here:  <http://www.cybersource.com/register>

* Complete your Evaluation account creation by following the instructions in the Registration email

######Transaction Security Keys

* Create security keys in the Enterprise Business Center (ebctest) after you've created your Merchant Admin account. 
Refer to our Developer's Guide for details <http://apps.cybersource.com/library/documentation/dev_guides/security_keys/html/wwhelp/wwhimpl/js/html/wwhelp.htm#href=securityKeys_SO_API.4.2.html>.

######JCE Unlimited Strength Jars

  Replace your Java installation’s existing security policy files with the new ones you downloaded from the Oracle site:
  
* 	Locate your existing US_export_policy.jar and local_policy.jar files in the $JAVA_HOME/jre/lib/security directory.  
* 	Rename or move your existing files to another directory.  
* 	Copy the new US_export_policy.jar and local_policy.jar that you downloaded from Oracle to the $JAVA_HOME/jre/lib/security directory.  
	


##Installing the SDK 

You do not need to download and build the source to use the SDK but if you want to do that, follow these steps:

1. Clone this repository.

2. Go to the sdk-java-master directory.

3. To run the integration tests, edit the test_cybs.properties and make the following changes:

    a. Set merchantID, keyAlias and keyPassword to your merchantID.  Please note that it is case-sensitive.
    
    b. Set keysDirectory to the directory where your key resides.  Use forward-slashes for the directory separator, even on Windows.
	   For example, "c:/keys"
	   
	(Optional Additional Changes)
    c. Set targetAPIVersion to the latest version displayed at: https://<cybersource-host>/commerce/1.x/transactionProcessor/
	   By default, it is set to the latest version when the package was created.
	   	
    d. Modify the logging properties as appropriate. Use forward-slashes for the directory separator in the logDirectory value, even on Windows. The directory you specify must already exist.
	   
    e. Please refer to the accompanying documentation for the other optional properties that you may wish to specify.
	   
	NOTE:  sendToProduction is initially set to false.  Set it to true only
	       when you are ready to send live transactions.
	
4. Build this project using Maven.

        a. mvn clean  // Cleans the Project
        
        b. mvn install 
           // Builds the project and creates a jar file of client SDK
           // Includes running all unit tests and integration tests

        c. mvn test
           // Runs unit tests

        d. mvn failsafe:integration-test
           // Runs unit and integration tests. Note that integration tests require proper setup of test_cybs.properties

  

##Documentation

For more information about CyberSource services, see <http://www.cybersource.com/developers/documentation>

For all other support needs, see <http://www.cybersource.com/support>





