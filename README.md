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

1.  Shorten URL
   -   POST `/api/shorten`
   -   Request body: The long URL as plain text
   -   Response: The shortened URL
2.  Redirect to Original URL
   -   GET `/api/r/{shortUrl}`
   -   Redirects to the original long URL
3.  Get Top Domains
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