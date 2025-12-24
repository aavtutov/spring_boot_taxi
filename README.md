# 🚕 Taxi Telegram Bot & WebApp

A comprehensive information system for taxi management via Telegram. This project features a Spring Boot backend, a PostgreSQL database, and an Nginx Proxy Manager for secure infrastructure and SSL handling.

## 🔗 Live Demo
You can test the bot live here: [@hop_in_taxi_bot](https://t.me/hop_in_taxi_bot)  
*The WebApp is integrated directly into the bot interface.*


## 🚀 Quick Start (Docker)

### 1. Prerequisites

Clone the repository and prepare the environment configuration:

```bash
    git clone https://github.com/aavtutov/spring_boot_taxi
    cd spring_boot_taxi
    cp .env.example .env
```

### 2. Configure Environment Variables

Edit the `.env` file with your actual credentials (bot token, Mapbox keys, and database passwords).

### 3. Deployment

Run the following command to build and start all services in detached mode:

```bash
docker-compose up -d --build
```



## ⚙️ Nginx Proxy Manager Setup

Once the containers are running, follow these steps to connect your domain:


1.  **Access the Admin UI:** `http://your-server-ip:81`

    * Default Login: `admin@example.com`
    * Default Password: `changeme`
    
2.  **Create a Proxy Host:**

    * Domain Names: `your-domain.com (WebApp url)`
    * Scheme: `http`
    * Forward Hostname: `taxi_app` (Matches the service name in docker-compose.yml)
    * Forward Port: `8080`

3.  **SSL Tab:**

	* Select "Request a new SSL Certificate" and enable "Force SSL". (This is mandatory for Telegram Web App functionality.)



## 🤖 Telegram Bot Configuration

After your server is up and running with SSL, you must link your WebApp to your bot:

1.  Open **[@BotFather](https://t.me/BotFather)** in Telegram.
2.  Select your bot using `/mybots`.
3.  Go to **Bot Settings** -> **Menu Button** -> **Configure Menu Button**.
4.  Enter the URL of your WebApp (e.g., `https://your-domain.com/index.html`).


## 🛠 System Architecture

The project runs within a private Docker network named `taxi-network`:

* **Nginx Proxy Manager** (Ports 80, 443, 81): The entry point for all traffic, handling reverse proxying and SSL certificates.
* **Spring Boot App** (Port 8080 internal): The core business logic and Telegram bot engine.
* **PostgreSQL** (Port 5432 internal): Data storage.


## 💻 Local Development

If you want to run the application locally (e.g., for debugging in your IDE):

**Database**: You can still use Docker to run only the database:
   
```bash
docker-compose up -d db
```

**Configuration**:

* Open `src/main/resources/application-local.properties`.
* Ensure the database connection points to `localhost:5432`.
* Important: Fill in your `MAPBOX_ACCESS_TOKEN` and Telegram credentials in the properties file to enable full functionality.

Run Options:

* IDE: Set the active Spring profile to local (e.g., via VM options: `-Dspring.profiles.active=local`).
* CLI:

	```bash
	./mvnw spring-boot:run -Dspring-boot.run.profiles=local
	```

> **Note on Mapbox:** To ensure the map displays correctly during local development, make sure to provide a valid `MAPBOX_ACCESS_TOKEN` in your `src/main/resources/application-local.properties` or set it as an environment variable in your IDE.


## 📂 Project Structure


 *  `src/main/java` - Spring Boot source code.
 *  `Dockerfile` - Instructions for building the application image.
 *  `docker-compose.yml` - Orchestration of the entire infrastructure.
 *  `.env.example` - Template for environment-specific settings.
 *  `.gitignore` - Prevents sensitive data from being committed.


## 🛠 Tech Stack

* **Java 17, Spring Boot 3**
* **Spring Security** (Admin authentication)
* **Spring Data JPA** (ORM)
* **Telegram Integration** (WebApp SDK (Frontend) & Bot API (Backend))
* **PostgreSQL** (DB)
* **Frontend**: JavaScript (ES6+), Thymeleaf, CSS3 (with Environment Variables).
* **Mapbox** (Directions API & GL SDK)
* **Infrastructure** (Docker, Docker Compose, Nginx)

## 🌟 Demo & Reviewer Access

To facilitate the review process, a **Demo Mode** is active when the app runs in non-production profiles. 
* On the "Waiting for Approval" screen, a **"Demo Mode: Approve as Driver"** button will appear. 
* This triggers a protected endpoint that bypasses manual admin verification, allowing you to test the driver dashboard immediately.


## 🛡 Security Note

Never share your `.env` file or commit it to version control. The `.gitignore` file included in this project is configured to protect your credentials.