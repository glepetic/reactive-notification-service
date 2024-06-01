**Reactive Notification Service with Redis Rate Limiter Implementation**

### Stack:
- Java 21
- Spring Webflux
- Reactive Redis
- Gradle
- Docker

### How to Run:
1. **Prerequisites:**
    - Make sure you have Docker installed on your machine.

2. **Running the Application:**
    - Clone this repository to your local machine using one of the following commands:
        - HTTPS:
          ```
          git clone https://github.com/glepetic/reactive-notification-service.git
          ```
        - SSH:
          ```
          git clone git@github.com:glepetic/reactive-notification-service.git
          ```
    - Navigate to the project's root directory.
    - Start the application and Redis instances by running the following command:
      ```
      docker-compose up
      ```

3. **Accessing the Application:**
    - Once the Docker compose command has completed and both the application and Redis instances are up and running, you can access the Reactive Notification Service through the specified port.

    - **Endpoint Description:**
        - **Path:** `/notification-service/api/v1/notification`
        - **Method:** `POST`
        - **Body Received:** JSON object with the following fields:
            - `user_id`: String (UUID format) - The ID of the user receiving the notification.
            - `content`: String - The content of the notification.
            - `type`: String - The type of notification. Possible values are: "STATUS", "NEWS", and "MARKETING".

    - **Example Request (using cURL):**
      ```bash
      curl --location 'http://localhost:8081/notification-service/api/v1/notification' \
      --header 'Content-Type: application/json' \
      --data '{
          "user_id": "ca75aa44-4acd-4774-8f82-87f6ffc4c6a7",
          "content": "hello!",
          "type": "STATUS"
      }'
      ```

4. **Stopping the Application:**
    - To stop the application and Redis instances, you can use the following command:
      ```
      docker-compose down
      ```

5. **Additional Notes:**
    - Ensure that no other service is using the ports specified in the Docker Compose file to avoid conflicts.
    - You may need to adjust network configurations or firewall settings to ensure proper communication between Docker containers if you encounter any issues.

This setup allows you to quickly deploy the Reactive Notification Service with Redis Rate Limiter on your local machine using Docker containers.
