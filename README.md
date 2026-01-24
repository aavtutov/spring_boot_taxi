# 🚕 Taxi Telegram Bot & WebApp

A comprehensive information system for taxi management via Telegram. This project features a Spring Boot backend, a PostgreSQL database, and an Nginx Proxy Manager for secure infrastructure and SSL handling.

## 🔗 Live Demo
You can test the live prototype here: [@hop_in_taxi_bot](https://t.me/hop_in_taxi_bot)  
*The WebApp is integrated directly into the bot interface.*

> **Note:** This is a technical demonstration for educational purposes. No real trips are provided.


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

#### Fare Strategies

The application supports different fare calculation logic via the **Strategy Pattern**. You can switch strategies in your `.env` file.

| Strategy | Description | Formula |
| :--- | :--- | :--- |
| `DISTANCE_ONLY` | Standard city fare based on distance | $Price = BASE + (Dist \times FARE/KM)$ |
| `DISTANCE_AND_TIME` | Traffic-aware fare (distance + duration) | $Price = BASE + (Dist \times FARE/KM) + (Dur \times FARE/MIN)$ |

**Configuration Parameters:**

* `FARE_CALCULATION_STRATEGY`: Defines which bean to use (`DISTANCE_ONLY` or `DISTANCE_AND_TIME`).
* `FARE_BASE`: Fixed starting price (landing fee).
* `FARE_PER_KM`: Cost per each kilometer.
* `FARE_PER_MINUTE`: Cost per each minute of the trip (used only in `DISTANCE_AND_TIME`).

### 3. Deployment

Run the following command to build and start all services in detached mode:

```bash
docker-compose up -d --build
```

### 4. Performance Optimization

I noticed a slight delay (about 5 seconds) when creating the first order after the app had been idle for a while. To fix this "cold start", I added Spring Boot Actuator and set up a simple system-level cron job on the host server. It pings the `/actuator/health` endpoint every 5 minutes. This keeps the database connection active and ensures the Telegram WebApp responds instantly.

Cron example:

```bash
*/5 * * * * curl -L -s https://your-domain.com/actuator/health > /dev/null
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

	* Select "Request a new SSL Certificate" and enable "Force SSL". This is mandatory for Telegram Web App functionality.



## 🤖 Telegram Bot Configuration

After your server is up and running with SSL, you must link your WebApp to your bot:

1.  Open **[@BotFather](https://t.me/BotFather)** in Telegram.
2.  Select your bot using `/mybots`.
3.  Go to **Bot Settings** -> **Menu Button** -> **Configure Menu Button**.
4.  Enter the URL of your WebApp (e.g., `https://your-domain.com/index.html`).


## 🐋 System Architecture

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

* Create and fill `src/main/resources/application-local.properties` use `application.properties` as template.
* Ensure the database connection points to `localhost:5432` (e.g., `jdbc:postgresql://localhost:5432/postgres`).
* Use **ngrok** to get WebApp URL and create secure tunnel:

	```bash
	ngrok http 8080
	```
* Copy the HTTPS URL provided by ngrok (e.g., `https://a1b2-c3d4.ngrok-free.app`) and use as `web.app.url` in your `application-local.properties`.
* Go to **[@BotFather](https://t.me/BotFather)**, select your bot (for local development consider creating a separate bot) and set the WebApp URL to this ngrok HTTPS URL.

> **Note:** ngrok URLs may change every time you restart the free version. For persistent testing, consider a static domain in the ngrok dashboard.

**Run Options**:

* IDE: Set the active Spring profile to local (e.g., via VM options: `-Dspring.profiles.active=local`).
* CLI:

	```bash
	./mvnw spring-boot:run -Dspring-boot.run.profiles=local
	```

## 🛠 Key Engineering Challenges and Solutions

During development, I focused on solving real-world backend problems rather than just making it "work." Here is how I handled some tricky parts:

### 1. "The Double-Booking" Problem
* In a taxi app, you can't have two drivers accepting the same order. To prevent this "race condition," I used **Pessimistic Locking** (SELECT FOR UPDATE). It might be a bit slower than other methods, but for a taxi order, data integrity (one order = one driver) is the top priority.

### 2. Database Performance and N+1 Problem
* I noticed that fetching a list of orders was causing too many database requests because each order wanted to know who the client/driver was.
    * To fix it, I kept all relationships LAZY by default (to save memory).
    * For the "Order History" I used **@EntityGraph** to combine driver and client into one SQL JOIN. It made the history page load significantly faster.

### 3. Telegram WebApp Security
* Since the frontend is a Telegram WebApp I couldn't just trust every request. I built a **Custom Interceptor** that checks the **HMAC-SHA256** signature before the request reaches the controllers.
* This way the backend only talks to verified users and I keep auth. validation out of controllers.

### 4. Clean Code and Argument Resolvers
* I noticed that in controller methods I often use same method (ex. `driverService.findDriverByTelegramId(tgUser.getId())`) to get driver or client. So I decided to implement **HandlerMethodArgumentResolver** for client and driver.
* This way, Client and Driver entities are automatically injected into methods making controllers clean.

## 📂 Project Structure


 *  `src/main/java` - Spring Boot source code.
 *  `Dockerfile` - Instructions for building the application image.
 *  `docker-compose.yml` - Orchestration of the entire infrastructure.
 *  `.env.example` - Template for environment-specific settings.
 *  `.gitignore` - Prevents sensitive data from being committed.


## ⚙️️ Tech Stack

* **Java 17, Spring Boot 3**
* **Spring Security** (Admin authentication)
* **Spring Data JPA** (Hibernate ORM)
* **PostgreSQL** (DB)
* **API: Spring REST** (Integration with Telegram WebApp)
* **Telegram Integration** (WebApp SDK (Frontend) & Bot API (Backend))
* **Frontend**: JavaScript (ES6+), Thymeleaf, CSS3 (with Environment Variables).
* **Mapbox** (Directions API & GL SDK)
* **Infrastructure** (Docker, Docker Compose, Nginx)

## 🌟 Demo & Reviewer Access

To facilitate the review process, a **Demo Mode** is active when the app runs in non-production profiles. 
* On the "Waiting for Approval" screen, a **"Demo Mode: Approve as Driver"** button will appear. 
* This triggers a protected endpoint that bypasses manual admin verification, allowing you to test the driver dashboard immediately.


## 🛡 Security Note





Never share your `.env` file or commit it to version control. The `.gitignore` file included in this project is configured to protect your credentials.
