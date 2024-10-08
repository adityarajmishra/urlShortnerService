URL Shortener Service
=====================

This is a simple URL shortener service built with Java and Spring Boot. It provides REST APIs for shortening URLs, redirecting shortened URLs, and retrieving statistics about the most frequently shortened domains.

Features
--------

-   Shorten long URLs to unique short URLs
-   Redirect short URLs to their original long URLs
-   In-memory caching for improved performance
-   Metrics API to retrieve top 3 most shortened domains
-   Persistence using H2 database
-   Docker support for easy deployment

Prerequisites
-------------

-   Java 17 or higher
-   Maven 3.6 or higher
-   Docker (optional, for containerization)

Running the Application Locally
-------------------------------

1.  Clone the repository:

    Copy

    `git clone https://github.com/yourusername/url-shortener.git
    cd url-shortener`

2.  Build the project:

    Copy

    `mvn clean install`

3.  Run the application:

    Copy

    `java -jar target/urlshortener-0.0.1-SNAPSHOT.jar`

The application will start on `http://localhost:8080`.

API Endpoints
-------------

### Shorten URL

**POST** `/api/shorten`

Shortens the provided URL and returns a shortened version.

**Request:**

`curl --location 'http://localhost:8080/api/shorten'\
--header 'Content-Type: application/json'\
--data '"https://www.linkedin.com/in/rahul-mishra-dev/"'`

**Response:**

`"Imh0dHBz"`

### Redirect to the Original URL

**GET** `/api/r/{shortUrl}`

Redirects to the original URL corresponding to the provided shortened URL.

**Request:**

`curl --location 'http://localhost:8080/api/r/Imh0dHBz'`

**Note:**

-   In Postman, ensure that "Automatically follow redirects" is turned **off** to see the `302 Found` response. You can find this setting in the Postman settings under the "General" tab.
-   If you forget to turn off automatic redirection in Postman or if you are running the service locally, check the console logs. The redirection URL will be logged, allowing you to see where the short URL points.

### Get Top Domains
   -   GET `/api/metrics/top-domains`
   -   Returns the top 3 domains that have been shortened the most

Running with Docker
-------------------

1.  Build the Docker image:

    Copy

    `docker build -t url-shortener .`

2.  Run the Docker container:

    Copy

    `docker run -p 8080:8080 url-shortener`

The application will be accessible at `http://localhost:8080`.
## Running with Docker

### Pull the Docker Image

To pull the Docker image from Docker Hub, use the following command:

```sh
docker pull adityarajmishra/url-shortener

docker run -p 8080:8080 adityarajmishra/url-shortener

Access the Application
Once the container is running, the application will be accessible at:

http://localhost:8080


```



Running Tests
-------------

Execute the following command to run the tests:

Copy

`mvn test`

Contributing
------------

1.  Fork the repository
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

License
-------

This project is licensed under the MIT License - see the <LICENSE.md> file for details.