# ocr-service
A REST OCR Service that receives PDFs and OCRs them

## Technical Description
This OCR Service is a Java Spring application that receives HTTP requests from clients with PDF files to OCR

Each request is incorporated in a worker that will perform the job of processing/OCR’ing the file. These workers are submitted to a ThreadPoolExecutor where they will wait their turn to be picked up by a thread to be ran. The selection of a worker to be processed takes into account the priority of the request and their order in the queue (in the case of same priority, older requests are performed first).

The worker will create a temporary file to store the PDF and will execute (through a *Runtime.exec()* call) a third-party application to OCR the PDF, waiting for it to finish.

There is a 10-minute timeout in place to prevent some kind of block in the external tool.

In the external tool fails, or in case the timeout time is reached, the service will attempt to process the file again. If there are 3 failed attempts to process the file, the request is marked as failed. On the other hand, if the external tool completes successfully, the request is marked as successful.

The request is now finished (successfully or not) and the OCR’ed file is sent back to the client that requested the work.

**Notes:**
The OCR service is configurable so that it can call any external tool (runnable through the command line) that OCRs PDFs; it is even capable of running dockerized applications. In our case, the tool chosen to OCR the PDFs is [@virantha/pypdfocr](https://github.com/virantha/pypdfocr), and the [application.properties](src/main/resources/application.properties) is configured to use this application.
It allows configuration on the number of threads available in the ThreadPoolExecutor, which dictate the number of files processed simultaneously

### Failure prevention
Every request is saved in a database table to prevent loss of information if the application is shutdown. The information in the DB concerning each request is updated throughout the multiple steps of the OCR process.

When the application restarts, it first reads the database looking for requests with files that weren’t yet OCR’d. It recreates the workers for these requests, submitting the tasks to the queue (thus protecting against failures/crashes of an instance of itself)

Also, due to a possible high number of requests, there is no guarantee of the amount of time it will take for a request to be completed. Therefore, it is possible that, for some reason, the client that requested the work is not available to receive the now OCR’ed file. When this happens and we fail to communicate the result of a request back to the client, the request is flagged as “*uncommunicated*”. The OCR service includes a job that runs every 30 seconds to fetch from the database uncommunicated requests and send the reply back to the client until it succeeds. There is also a flag in the database that allows the administrator to stop the request from being processed/communicated by updating the entry in the DB (database column “*request_stopped*”.

## HTTP REST API
Like in every application working with REST requests and responses, there is a well defined set of properties that the requests to the OCR service must include.

Keep in mind that the OCR service will not reply immediatly with the OCR'ed PDF. OCR'ing a PDF is a process that takes time, and it's not realistic to reply immediatly with the file. Thus, the clients making requests to the OCR service must implement a endpoint to be able to receive the response from the service when the file has been processed.

The HTTP requests handled by the OCR service are **Multipart/Form-Data** requests sent to **/ocr-service/ocr/request** with the following properties:

| Property                      | Type              | Description                                                 |
| ----------------------------- |-------------------| ------------------------------------------------------------|
| requestorReference            | String            | A reference that is meaningful to the client                |
| priority                      | Short             | Number corresponding to the priority of the request         |
| callbackEndpoint              | String            | The full URL to where the response is to be sent (client)   |
| callbackMethod                | String            | The method to use when calling the endpoint (GET, POST, etc)|
| file                          | File              | The .pdf file to be OCR’ed                                  |

When you make this request the server will, in normal situations, reply with a **202 – ACCEPTED** status code, indicating that the file was added to the queue to be processed. It will include in the body a JSON string with the following properties:

| Property                      | Type              | Description                                                 |
| ----------------------------- |-------------------| ------------------------------------------------------------|
| success                       | Boolean           | true - Boolean indicating that the request was accepted     |
| message                       | String            | A human-readable message                                    |
| requestorReference            | String            | A copy of the requestorReference sent                       |
| requestId                     | Long              | The ID of the request that was registered in the system     |
| requestToken                  | String            | A 130-bit random token created by the service*              |

\* might be usefull for security purposes.

In case the server is currently active but experiencing difficulties, it will reply a **503 – SERVICE UNAVAILABLE** and include in the body a JSON string with the following properties:

| Property                      | Type              | Description                                                         |
| ----------------------------- |-------------------| --------------------------------------------------------------------|
| success                       | Boolean           | false - boolean indicating that the request could not be accepted   |
| message                       | String            | A human-readable message                                            |


When a file finishes being processed, the OCR service will inform the client (making a call using the callbackMethod and callbackEndpoint parameters passed in the request) and send a copy of the OCR’ed file. For that it sends a **Multipart/Form-Data requests** with the following properties:

| Property                      | Type              | Description                                                                    |
| ----------------------------- |-------------------| -------------------------------------------------------------------------------|
| requestId                     | Long              | The ID of the request shared with the client when the request was accepted     |
| requestorReference            | String            | The reference the client had indicated was meaningful.                         |
| requestToken                  | String            | Status code (1 – Success, -1 Request failed)                                   |
| statusCode                    | Short             | The random token that was shared with the client when the request was accepted |
| statusMessage                 | String            | A human readable message regarding the request                                 |
| file                          | File              | The OCR’ed .pdf file                                                           |


## Installation
### Machine specs
Due to the high demand of memory and CPU during the OCR process, the OCR service should be installed in a standalone server. The server characteristics depend on the amount of simultaneous files to be processed, which is particularly important when deciding the amount of RAM for the server. We found after testing thousands of files that, as a rule of thumb, you should do the calculation for the required RAM as:

**1.2 GB x T x N**

**T** = Number of simultaneous files (number of configured threads for the OCR service);

**N** = Number of tesseract instances per file (configurable in the case of the external tool pypdfocr);

Additionally a good margin of error should be left so that other applications in the server can run. Also, this calculation was based on the second to worst case we detected (as a curiosity, the worst case detected was a high resolution PDF that generated a tesseract instance allocating 2.8 GB of RAM).

If the memory recommendations are not followed, it is likely the machine will run out of memory and kill the application.

### Software pre-requisites
- MySQL database
- Java 8
- PyPDFOCR or another console OCR application. Note that not all OCR applications are compatible - for example, if the application requires user interaction after it is called, it won't be compatible. Installation instructions for PyPDFOCR are available in the developers github repository [@virantha/pypdfocr](https://github.com/virantha/pypdfocr).


## Configuration

### DB Setup
Only one database table is required to store the information used by the OCR service. This table must be named "*ocr_requests*", and a script for the creation of the table is available. See [SQL.txt file](SQL.txt).
The database configuration can be changed in the [application.properties](src/main/resources/application.properties) by modifying the spring provided configuration.

Notice that the application was tested only with MySQL databases, and most likely won't work with other DBMS systems. To make it compatible you need to modify the queries performed in the [OCRRequestRepository.java](src/main/java/com/github/mauro1855/ocrservice/repository/OCRRequestRepository).

### Application specific configuration
Application can be configured by changing the [application.properties](src/main/resources/application.properties). The following application specific properties need to be configured:

| Parameter                     | Description                                                       | Default value  |
| ----------------------------- |-------------------------------------------------------------------| ---------------|
| pool.nb.threads               | Number of threads = Number of simultaneous files                  | 3              |
| pool.queue.initial.size       | Initial size of the queue                        | 20             | 20             |
| ocr.command                   | Command to call the external OCR application (without parameters) | pypdfocr*      | 
| ocr.extra.commands            | Static arguments for the application                              | -l eng+fra     |
| ocr.output.required           | Boolean to whether to include the output path in the command      | false**        |
| ocr.output.file.prefix.command| Extra arguments before output argument                            |                |
| client.username               | Authentication username for communicating with the client         | admin***       |
| client.password               | Authentication password for communicating with the client         | admin***       |

Besides this configuration, the application.properties file includes more configuration provided by the spring framework (database, security, etc).

\* The application also accepts dockerized OCR applications. Just put "docker <container_name>" in the *ocr.command* property and the remaining arguments in the *ocr.extra.commands*.

\** some applications require that you introduce the output path/filename, however PyPDFOCR does not accept it. In this case we set this argument to *false* and we assume that the output filename is the same as the input filename + "\_ocr" located in the same folder (this seems to be the behavior of some OCR applications). If the OCR application you are using does not accept an output filename as well but the output path/filename is different that what assumed, than you're application is currently not compatible with the OCR Service. However you can easily modify the code for this by changing the [OCRRequestWorker.java](src/main/java/com/github/mauro1855/ocrservice/worker/OCRRequestWorker) (in method `processOCRRequest`, change the *targetFileName* variable)

\*** If your client doesn't use basic authentication, you can just leave this field empty... or not, I believe it should have no impact, your client will just disregard the authentication attempt and accept the call anyway.

### Compilation, packaging and execution

Maven is used as a packed manager, so just position your console in the root folder and do `mvn install`. The application is packaged in  a .jar file that can be executed by running `java -jar <generated_jar>`. Because this is a spring application, you can pass more spring specific parameters if required. The application is now ready to receive requests. By default, the application listens on port **8085** (can be changed by modifying the [application.properties](src/main/resources/application.properties) file).

