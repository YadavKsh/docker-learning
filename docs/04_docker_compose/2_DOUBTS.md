# 🐳 Docker — Multi-Container Applications

Answers to two of the most important questions when moving from single-container to real-world multi-service Docker setups.

---

## 📑 Table of Contents
1. [Do I need 3 separate Dockerfiles for 3 services?](#1-do-i-need-3-separate-dockerfiles-for-3-services)
2. [How does the Backend container find the DB container?](#2-how-does-the-backend-container-find-the-db-container)
3. [The Real Solution — Docker Compose](#-the-real-solution--docker-compose)

---

## ❓ Question 1

> *"If I wouldn't have docker-compose.yml, would I have to make 3 different Dockerfiles and run each one separately?"*

### ✅ Short Answer

**Partially yes — but it's more nuanced than that.**

You'd need separate Dockerfiles only for services where **you are writing the code** (like Backend and Frontend). For off-the-shelf services like a database, you simply pull a pre-built image — no Dockerfile needed at all.

---

### 🔍 Breaking It Down — Service by Service

Let's take a typical 3-service application:

```
Your Application
    ├── 🖥️  Frontend   (React / Angular / Vue)
    ├── ⚙️  Backend    (Spring Boot / Node.js / Django)
    └── 🗄️  Database   (MySQL / PostgreSQL / MongoDB)
```

Here's what you'd actually need for each:

---

#### 🗄️ Database Service — NO Dockerfile needed

The database is not code you wrote. MySQL, PostgreSQL, MongoDB — these are all published as **official pre-built images on Docker Hub**. You just pull and run them directly.

```bash
# No Dockerfile. Just pull and run.
docker run -d \
  --name my-db \
  -e MYSQL_ROOT_PASSWORD=secret \
  -e MYSQL_DATABASE=myapp \
  mysql:8.0
```

> 💡 You only write a Dockerfile for code **you built yourself**. For everything else (databases, message brokers, caches), official images already exist on Docker Hub — just configure them with environment variables.

---

#### ⚙️ Backend Service — YES, needs a Dockerfile

Your backend is custom code. You need to package it into an image. So you write a `Dockerfile` for it.

```dockerfile
# backend/Dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Build the image from the Dockerfile
docker build -t my-backend:1.0 ./backend

# Run it
docker run -d --name my-backend -p 8081:8081 my-backend:1.0
```

---

#### 🖥️ Frontend Service — YES, needs a Dockerfile

Same logic — your frontend is custom code, so it needs its own `Dockerfile`.

```dockerfile
# frontend/Dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
EXPOSE 3000
CMD ["npm", "start"]
```

```bash
# Build the image from the Dockerfile
docker build -t my-frontend:1.0 ./frontend

# Run it
docker run -d --name my-frontend -p 3000:3000 my-frontend:1.0
```

---

### 📁 So What Does Your Project Structure Look Like?

```
my-app/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile         ← ✅ You write this
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── Dockerfile         ← ✅ You write this
│
└── (no Dockerfile for DB) ← ✅ Just use mysql:8.0 from Docker Hub
```

---

### 😓 The Pain of Doing This Manually (Without Docker Compose)

Even though the number of Dockerfiles is manageable, the **manual process of running everything** becomes painful fast.

Without Docker Compose, here's what you'd have to do every single time you want to start your application:

```bash
# Step 1 — Create a shared network (so containers can talk to each other)
docker network create app-network

# Step 2 — Create a volume (so DB data isn't lost)
docker volume create db-data

# Step 3 — Start the database
docker run -d \
  --name my-db \
  --network app-network \
  -v db-data:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=secret \
  -e MYSQL_DATABASE=myapp \
  mysql:8.0

# Step 4 — Build the backend image
docker build -t my-backend:1.0 ./backend

# Step 5 — Start the backend
docker run -d \
  --name my-backend \
  --network app-network \
  -p 8081:8081 \
  -e DB_URL=jdbc:mysql://my-db:3306/myapp \
  -e DB_PASSWORD=secret \
  my-backend:1.0

# Step 6 — Build the frontend image
docker build -t my-frontend:1.0 ./frontend

# Step 7 — Start the frontend
docker run -d \
  --name my-frontend \
  --network app-network \
  -p 3000:3000 \
  -e REACT_APP_API_URL=http://my-backend:8081 \
  my-frontend:1.0
```

That's **7 commands** just to start your app. And if you want to stop everything:

```bash
docker stop my-frontend my-backend my-db
docker rm my-frontend my-backend my-db
```

And if someone else clones your project? They'd have to run all of these manually too — hoping they don't miss a flag or typo an environment variable.

> 🤯 This is exactly the problem Docker Compose solves — you define everything once in a `docker-compose.yml` and then just run `docker compose up`. One command to start everything, one command to stop everything.

---
---

## ❓ Question 2

> *"My backend is running in one container and the DB is running in another. How would the backend know to connect to the DB container?"*

This is one of the most important and commonly misunderstood things about Docker. Let's go deep.

---

### ❌ The Wrong Mental Model — `localhost`

When your backend and DB are running on the **same machine without Docker**, your `application.properties` probably looks like this:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/myapp
```

This works because `localhost` means *"this very machine"* — and both the app and MySQL are on the same machine.

Now you put them in Docker. Your first instinct is to keep `localhost` in the URL. **This is wrong and will always fail.**

```
❌ What you think happens:

  [Backend Container]  →  localhost:3306  →  [DB Container]
```

```
✅ What actually happens:

  [Backend Container]  →  localhost:3306  →  [Backend Container itself]
                                              (nothing is listening here!)
                                              CONNECTION REFUSED ❌
```

> 🧠 **Key insight:** Inside a container, `localhost` refers to **that container itself** — not your host machine, not any other container. Every container has its own isolated network namespace.

---

### 🌐 The Solution — Docker Networks

Docker has a built-in networking system. When you create a **Docker network** and attach multiple containers to it, Docker does something powerful:

> **Every container on the same network can reach every other container using its container name as a hostname.**

Docker runs an internal DNS server that automatically resolves container names to their IP addresses — but you never need to know the actual IP. You just use the name.

---

### 🔬 How It Works — Step by Step

#### Step 1 — Create a shared network

```bash
docker network create app-network
```

This creates a private virtual network inside Docker. Think of it like a private WiFi network that only your containers can join.

---

#### Step 2 — Start the DB container on that network

```bash
docker run -d \
  --name my-db \              # ← this name becomes the hostname!
  --network app-network \     # ← joined to the shared network
  -e MYSQL_ROOT_PASSWORD=secret \
  mysql:8.0
```

The moment this container joins `app-network`, Docker's internal DNS registers `my-db` as a resolvable hostname within that network.

---

#### Step 3 — Start the Backend container on the same network

```bash
docker run -d \
  --name my-backend \
  --network app-network \     # ← same network as DB!
  -e DB_URL=jdbc:mysql://my-db:3306/myapp \   # ← use container NAME, not localhost
  my-backend:1.0
```

Now inside the backend container, `my-db` resolves to the actual IP address of the DB container. The connection works. ✅

---

### 🗺️ Visualising What's Happening

```
┌─────────────────────────────────────────────────────┐
│                   app-network                       │
│                                                     │
│   ┌─────────────────┐      ┌──────────────────┐     │
│   │  my-backend     │      │     my-db        │     │
│   │  (Spring Boot)  │─────►│  (MySQL 8.0)     │     │
│   │                 │      │                  │     │
│   │  DB_URL=        │      │  port 3306       │     │
│   │  jdbc:mysql://  │      │                  │     │
│   │  my-db:3306/... │      │                  │     │
│   └─────────────────┘      └──────────────────┘     │
│                                                     │
│   ┌─────────────────┐                               │
│   │  my-frontend    │                               │
│   │  (React)        │                               │
│   └─────────────────┘                               │
│                                                     │
└─────────────────────────────────────────────────────┘

Docker's internal DNS:
  "my-db"       →  172.18.0.2  ✅
  "my-backend"  →  172.18.0.3  ✅
  "my-frontend" →  172.18.0.4  ✅
```

> 💡 You never hardcode IP addresses like `172.18.0.2`. Docker assigns these dynamically and they can change. You always use the **container name** — Docker handles the IP resolution automatically.

---

### 🔧 What Changes in Your Backend Config?

This is the only change needed in your `application.properties`:

```properties
# ❌ Before Docker — works on bare metal, fails in Docker
spring.datasource.url=jdbc:mysql://localhost:3306/myapp

# ✅ After Docker — use the DB container's name as the host
spring.datasource.url=jdbc:mysql://my-db:3306/myapp
#                                    ↑
#                          container name, not localhost!
```

Or if you're using environment variables (the correct approach):

```properties
# application.properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

```bash
# Then inject the right values at runtime
docker run -d \
  --name my-backend \
  --network app-network \
  -e DB_URL=jdbc:mysql://my-db:3306/myapp \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secret \
  my-backend:1.0
```

> ✅ Using environment variables instead of hardcoding URLs is the industry standard. It means the same Docker image can be used in development, staging, and production — just with different env vars.

---

### 🚫 What Happens If They're NOT on the Same Network?

```bash
# DB on the default network
docker run -d --name my-db mysql:8.0

# Backend on a custom network
docker run -d --name my-backend --network app-network my-backend:1.0
```

Even though both containers are running, the backend **cannot reach** the DB. They're on different networks — isolated from each other. The DNS name `my-db` won't resolve inside the backend container, and the connection will time out.

```
my-backend (app-network)  ✗  my-db (default network)
                 ↑
         Cannot cross network boundaries!
```

> ⚠️ Both containers **must** be on the **same Docker network** for name-based DNS resolution to work. Being on the same host machine is not enough.

---

### 🔗 What About Frontend → Backend Communication?

The exact same principle applies. The frontend container reaches the backend using its container name:

```bash
docker run -d \
  --name my-frontend \
  --network app-network \
  -e REACT_APP_API_URL=http://my-backend:8081 \   # ← backend container name!
  my-frontend:1.0
```

So your full communication chain inside the Docker network looks like this:

```
Browser (your laptop)
    │
    │  localhost:3000
    ▼
┌──────────────────────────────────────────────────┐
│                  app-network                     │
│                                                  │
│  my-frontend :3000  →  my-backend :8081          │
│                              │                   │
│                              ▼                   │
│                         my-db :3306              │
└──────────────────────────────────────────────────┘
```

- Your **browser** talks to `localhost:3000` (mapped to `my-frontend` via `-p`)
- `my-frontend` talks to `my-backend` using the container name
- `my-backend` talks to `my-db` using the container name
- All inter-container communication stays **inside the Docker network**

---
---

## 🚀 The Real Solution — Docker Compose

Both of these problems — **orchestrating multiple containers** and **connecting them together** — are exactly what **Docker Compose** solves.

Instead of running 7 commands manually and hoping you got every flag right, you describe your entire application in a single `docker-compose.yml` file:

```yaml
# docker-compose.yml
version: '3.8'

services:

  # 🗄️ Database — no Dockerfile, uses official image directly
  db:
    image: mysql:8.0
    container_name: my-db
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: myapp
    volumes:
      - db-data:/var/lib/mysql

  # ⚙️ Backend — built from your Dockerfile
  backend:
    build: ./backend          # ← points to the folder with your Dockerfile
    container_name: my-backend
    ports:
      - "8081:8081"
    environment:
      DB_URL: jdbc:mysql://db:3306/myapp   # ← "db" is the service name above!
      DB_USERNAME: root
      DB_PASSWORD: secret
    depends_on:
      - db                    # ← start DB before backend

  # 🖥️ Frontend — built from your Dockerfile
  frontend:
    build: ./frontend
    container_name: my-frontend
    ports:
      - "3000:3000"
    environment:
      REACT_APP_API_URL: http://backend:8081   # ← "backend" is the service name!
    depends_on:
      - backend

volumes:
  db-data:
```

### ✨ What Docker Compose handles automatically:
- **Builds** all images that have a `build:` key
- **Creates** a shared network for all services — automatically, without you running `docker network create`
- **Starts** all containers in the right order (via `depends_on`)
- **Connects** them together — service names (`db`, `backend`, `frontend`) act as hostnames just like container names do in manual Docker networking
- **Manages** volumes

### And now to run your entire application:

```bash
# Start everything — one command
docker compose up

# Start in background
docker compose up -d

# Stop everything — one command
docker compose down

# Rebuild images and start
docker compose up --build
```

> 🎯 That's it. Seven manual commands become one. Your entire infrastructure is version-controlled in a single file that any teammate can clone and run instantly.

---

## 📊 Summary

| Question | Answer |
|----------|--------|
| Do I need a Dockerfile for every service? | Only for services with **custom code** (backend, frontend). Off-the-shelf services like MySQL use pre-built images — no Dockerfile needed. |
| How does the backend find the DB container? | Put both on the **same Docker network**. Then use the **container name** (or service name in Compose) as the hostname in your DB URL — not `localhost`. |
| What manages all of this automatically? | **Docker Compose** — one file describes the entire stack, one command starts it all. |

---

## 💡 The Golden Rules

> 1. **`localhost` inside a container = that container itself.** Never use `localhost` to reach another container.

> 2. **Container names are hostnames** — but only within the same Docker network. Always put connected containers on the same network.

> 3. **Dockerfiles are for your code.** Pre-built services (MySQL, Redis, Nginx) come ready-made from Docker Hub.

> 4. **Docker Compose is not optional for real apps.** Once you have more than one container, Compose is the right tool.